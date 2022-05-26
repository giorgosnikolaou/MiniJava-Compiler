import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;
import java.util.stream.BaseStream;

import SymbolTable.*;
import SymbolTable.Class;

/**
 * Provides default methods which visit each node in the tree in depth-first
 * order.  Your visitors may extend this class.
 */
public class IRVisitor extends GJDepthFirst<String,String> {
   //
   // User-generated visitor methods below
   //

    SymbolTable st;
    private int label_counter = 0;
    private int reg_counter = 0;
	private int tab_count = 0;
	Map<String, String> classes_vt_size = new HashMap<String, String>();
    private String last_label = null;

    public IRVisitor(SymbolTable st) 
    {
		this.st = st;
	}

    private void print_error(String str)
    {
        System.err.println(str);
    }
	
    private void emit_pure(String text)
    {
        // write text to file 
        System.out.print(text);
    }

	private void emit_tabs()
	{
		emit_pure("\t".repeat(tab_count));	
	}

    private void emit(String text)
    {
        emit_tabs();
        emit_pure(text);
    }

    public void emit_label(String label)
    {
        last_label = label;
        emit_pure(label + ":\n");
    }

    private String new_label()
    {
        return "L" + label_counter++;
    }

    private String new_reg()
    {
        return "_" + reg_counter++;
    }

    private String get_size(String type)
    {
        return  type.equals("boolean") ? "i1" :
                type.equals("int") ? "i32" :
                type.equals("boolean[]") ? "%_BooleanArray*" :
                type.equals("int[]") ? "%_IntegerArray*" : "i8*";
    }

    private void emit_vtable_functions(Class _class)
    {
        int i = _class.vtable.size();

        if (_class.vtable.size() > 1)
            emit_pure("\n\t");

        for (Map.Entry<String, String> entry : _class.vtable.entrySet())
        {   

            String fun = entry.getKey();
            String cl = entry.getValue();

            Function _fun = st.get_class(cl).get_function(fun);

            emit_pure("i8* bitcast (");
            emit(get_size(_fun.type()) + " (i8*"); // return type, "this" argument

            for (Variable arg : _fun.get_arg_info())
                emit_pure(", " + get_size(arg.type()));
            
            emit_pure(")* @" + cl + "." + fun + " to i8*)");
            
            if (--i > 0)
                emit_pure(",\n\t");
        }

        if (_class.vtable.size() > 1)
            emit_pure("\n");
        
    }

    private void emit_vtable()
    {
        for (Map.Entry<String, Class> entry : st.classes.entrySet())
        {   
            Class _class = entry.getValue();
			String dim = "[" + _class.vtable.size() + " x i8*]";
			classes_vt_size.put(_class.name(), dim);

            emit_pure("@." + _class.name() + "_vtable = global " + dim + " [");
            emit_vtable_functions(_class);
            emit_pure("]\n");
        }
        emit_pure("\n");
    }

    private boolean is_literal(String[] info)
    {
        return info[2].equals("literal");
    }

    private String str_reg_cons(String var)
    {
        String[] info = get_variable_info(var);
        return (is_literal(info) ? " " : " %") + info[0];
    }

	private void emit_variable(String size, String reg_cons, String name)
	{
		emit("store " + size + str_reg_cons(reg_cons) + 
                        ", " + size + "* %" + get_variable_info(name)[0] + "\n\n");
	}

	private String get_dim(String _class)
	{
		return classes_vt_size.get(_class);
	}

	private String init_local(String type, String name)
	{
		return init_local(type, name, get_size(type).contains("*") ? "null" : "0");
	}

	private String init_local(String type, String name, String initial)
	{
		String size = get_size(type);

		// Allocate memory for local variable
		String _ret = "\t%" + name + " = alloca " + size + "\n";

		// Initialize with 0 or null
		_ret += "\tstore " + size + " " + initial + ", " + size + "* %" + name + "\n\n"; 

		return _ret;
	}

	private String load_field(String name, String _class, boolean rvlaue)
	{
		Class cl = st.get_class(_class);
		Variable var = cl.get_variable(name);

		int offset = var.offset() + 8; // add vtable 8
		String size = get_size(var.type());

		String _h1 = new_reg();
		String _h2 = new_reg();

		emit("%" + _h1 + " = getelementptr i8, i8* %this, i32 " + offset + "\n");

		emit("%" + _h2 + " = bitcast i8* %" + _h1 + " to " + size + "*\n");

		if (rvlaue)
		{
			String _ret = new_reg();
			emit("%" + _ret + " = load " + size + ", " + size + "* %" + _h2 + "\n\n");

			return variable_info(_ret, size, var.type());
		}

		return variable_info(_h2, size, var.type());
	}

	private String load_local(String name, String scope) throws Exception
    {
        String _ret = new_reg();
        String type = st.resolve_var_type(name, scope);
        String size = get_size(type);

        emit("%" + _ret + " = load " + size + ", " + size + "* %" + name + "\n");

        return variable_info(_ret, size, type);
    }

    private String[] get_variable_info(String var)
    {
        return var.split(st.class_delimiter);
    }

    private String emit_binary_expr(String left, String right, String code)
    {
        String _ret = new_reg();

        emit("%" + _ret + " = " + code + " i32" + 
                    str_reg_cons(left) + "," +
                    str_reg_cons(right) + "\n\n");
            
        return variable_info(_ret, "i32", "int");
    }
    
    private String variable_info(String name, String size, String type)
    {
        return name + st.class_delimiter + size + st.class_delimiter + type;
    }

    private String get_class(String scope)
    {
        return scope.split(st.class_delimiter)[0];
    }

    private String get_function(String scope) throws Exception
    {
        if (!scope.contains(st.class_delimiter))
            throw new Exception(scope + " does not contain a function!");

        return scope.split(st.class_delimiter)[1];
    }

    
    //  ------------------------------------------------------------------------------
    //  ------------------------------------------------------------------------------


    /**
        * f0 -> MainClass()
        * f1 -> ( TypeDeclaration() )*
        * f2 -> <EOF>
    */
    @Override
    public String visit(Goal n, String argu) throws Exception 
    {
		emit_pure("%_BooleanArray = type { i32, i1* }\n");
		emit_pure("%_IntegerArray = type { i32, i32* }\n\n");

        // emit vtables
        // emit vtables
        // .<class name>_vtable = global [<number of functions> x i8* ] 
        //      [i8* bitcast (<ret type> (i8*<,> <argument types>)* @<class name>.<function name> to i8*), ...]
        
        emit_vtable();

        // emit boilerplate
        emit_pure("declare i8* @calloc(i32, i32)\n");
        emit_pure("declare i32 @printf(i8*, ...)\n");
        emit_pure("declare void @exit(i32)\n\n");

        emit_pure("@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n");
        emit_pure("@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n\n");

        emit_pure("define void @print_int(i32 %i) {\n");
        emit_pure("\t%_str = bitcast [4 x i8]* @_cint to i8*\n");
        emit_pure("\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n");
        emit_pure("\tret void\n");
        emit_pure("}\n\n");

        emit_pure("define void @throw_oob() {\n");
        emit_pure("\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n");
        emit_pure("\tcall i32 (i8*, ...) @printf(i8* %_str)\n");
        emit_pure("\tcall void @exit(i32 1)\n");
        emit_pure("\tret void\n");
        emit_pure("}\n\n");
        

        // Continue
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);

        return null;
    }


    /**
        * f0 -> "class"
        * f1 -> Identifier()
        * f2 -> "{"
        * f3 -> "public"
        * f4 -> "static"
        * f5 -> "void"
        * f6 -> "main"
        * f7 -> "("
        * f8 -> "String"
        * f9 -> "["
        * f10 -> "]"
        * f11 -> Identifier()
        * f12 -> ")"
        * f13 -> "{"
        * f14 -> ( VarDeclaration() )*
        * f15 -> ( Statement() )*
        * f16 -> "}"
        * f17 -> "}"
    */
    @Override
   	public String visit(MainClass n, String argu) throws Exception 
   {    

        String class_name = n.f1.accept(this, argu);
        
        // emit main boilerplate
        emit_pure("define i32 @main() {\n");

		tab_count++;
        n.f14.accept(this, class_name + st.class_delimiter + "main");
        n.f15.accept(this, class_name + st.class_delimiter + "main");
        
        emit_pure("\n\tret i32 0\n}\n\n");
		
		tab_count--;


        return null;
   }


    /**
     * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
    @Override
    public String visit(ClassDeclaration n, String argu) throws Exception
    {
        String class_name = n.f1.accept(this, argu);
        n.f4.accept(this, class_name);

        return null;
    }


    /**
     * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
    @Override
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception
    {
        String class_name = n.f1.accept(this, argu);
        n.f6.accept(this, class_name);
		
        return null;
    }


    /**
     * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public String visit(VarDeclaration n, String argu) throws Exception
    {
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);
        
		emit_pure(init_local(type, name));

        return null;
    }


    /**
     * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
    @Override
    public String visit(MethodDeclaration n, String argu) throws Exception
    {
		tab_count++;

		// argu should have the class name 
        String type = n.f1.accept(this, argu);
        String name = n.f2.accept(this, argu);
		
		String size = get_size(type);
		emit_pure("define " + size + " @" + argu +  "." + name + "(i8* %this");
		
		String initialize = n.f4.present() ? n.f4.accept(this, argu) : "";
		emit_pure(") {\n" + initialize);


        n.f7.accept(this, argu + st.class_delimiter + name);
        n.f8.accept(this, argu + st.class_delimiter + name);


        String _ret = n.f10.accept(this, argu + st.class_delimiter + name);

        emit_pure("\n");
		emit("ret " + size + str_reg_cons(_ret) + "\n");
		emit_pure("}\n\n");
		tab_count--;

        return null;
    }


    /**
     * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */
    @Override
    public String visit(FormalParameterList n, String argu) throws Exception
    {
        String ret = n.f0.accept(this, argu);

        return n.f1 == null ? ret : ret + n.f1.accept(this, argu);
    }


    /**
     * f0 -> Type()
    * f1 -> Identifier()
    */
    @Override
    public String visit(FormalParameter n, String argu) throws Exception
    {
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);

		emit_pure(", " + get_size(type) + " %." + name);

        return init_local(type, name, "%." + name);
    }


    /**
     * f0 -> ( FormalParameterTerm() )*
    */
    @Override
    public String visit(FormalParameterTail n, String argu) throws Exception
    {
		String _ret = "";

        for (Node node : n.f0.nodes) 
			_ret += node.accept(this, argu);

        return _ret;
    }


    /**
     * f0 -> ","
    * f1 -> FormalParameter()
    */
    @Override
    public String visit(FormalParameterTerm n, String argu) throws Exception
    {
        return n.f1.accept(this, argu);
    }


    @Override
    public String visit(BooleanArrayType n, String argu) throws Exception
    {
        return "boolean[]";
    }


    @Override
    public String visit(IntegerArrayType n, String argu) throws Exception
    { 
		return "int[]";
    }


    @Override
    public String visit(BooleanType n, String argu) throws Exception
    {
        return "boolean";
    }


    @Override
    public String visit(IntegerType n, String argu) throws Exception
    {
        return "int";
    }


    /**
     * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    @Override
    public String visit(AssignmentStatement n, String argu) throws Exception
    {
		// argu should be the scope we're in
        String name = n.f0.accept(this, argu);
		String type = st.resolve_var_type(name, argu);

        String reg_cons = n.f2.accept(this, argu);
		
		String reg = st.is_field(name, argu) ? 
                    load_field(name, get_class(argu), false) : 
                    variable_info(name, get_size(type), type);

		emit_variable(get_size(type), reg_cons, reg);

        return null;
    }


    /**
     * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
    @Override
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception
    {
        String name = n.f0.accept(this, argu);
        String type = st.resolve_var_type(name, argu);
        
        String reg1 = st.is_field(name, argu) ? 
                    load_field(name, get_class(argu), false) : 
                    load_local(name, argu);

        String reg2 = n.f2.accept(this, argu);

        bounds_check(reg1, reg2);

        // here we need to get the pointer to that location
        String _h1 = get_arr_pos(reg1, reg2);
        
        String reg_cons = n.f5.accept(this, argu);

        String base_size = type.contains("int") ? "i32" : "i1";
        
        emit("store " + base_size + str_reg_cons(reg_cons) + ", " + base_size +
             "* %" + _h1 + "\n");

        return null;
    }


    /**
     * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    @Override
    public String visit(IfStatement n, String argu) throws Exception
    {
        
        String cond_reg = n.f2.accept(this, argu);
        String cond = str_reg_cons(cond_reg);
        
        String then_label = new_label(); 
        String else_label = new_label(); 
        String cont_label = new_label(); 


        emit("br i1" + cond + ", label %" + then_label + ", label %" + else_label + "\n\n");

        emit_label(then_label);
        n.f4.accept(this, argu);
        emit("br label %" + cont_label + "\n\n");

        emit_label(else_label);
        n.f6.accept(this, argu);
        emit("br label %" + cont_label + "\n\n");

        emit_label(cont_label);

        return null;
    }


    /**
     * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    @Override
    public String visit(WhileStatement n, String argu) throws Exception
    {
        String start_label = new_label();
        String loop_label = new_label(); 
        String cont_label = new_label(); 


        emit("br label %" + start_label + "\n");
        emit_label(start_label);

        String cond_reg = n.f2.accept(this, argu);       

        emit("br i1" + str_reg_cons(cond_reg) + ", label %" + loop_label +
             ", label %" + cont_label + "\n\n");
        
             
        emit_label(loop_label);
        n.f4.accept(this, argu);

        emit("br label %" + start_label + "\n\n");

        emit_label(cont_label);

        return null;
    }


    /**
     * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    @Override
    public String visit(PrintStatement n, String argu) throws Exception
    {
        String reg = n.f2.accept(this, argu);

		emit("call void (i32) @print_int(i32 " + str_reg_cons(reg) + ")\n\n");

		return null;
    }


    /**
     * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    @Override
    public String visit(AndExpression n, String argu) throws Exception
    {
        String reg1 = n.f0.accept(this, argu);
        String reg1_n = str_reg_cons(reg1);

        String is_true_label = new_label();
        String is_false_label = new_label();
        String cont_label = new_label();

        emit("br i1" + reg1_n + ", label %" + is_true_label + ", label %" + is_false_label + "\n\n");

        emit_label(is_false_label);
        emit("br label %" + cont_label + "\n\n");

        emit_label(is_true_label);
        String reg2 = n.f2.accept(this, argu);
        emit("br label %" + cont_label + "\n\n");


        String prev_label = last_label;
        emit_label(cont_label);

        String _ret = new_reg();
        emit("%" + _ret + " = phi i1 [" + str_reg_cons(reg2) + ", %" + prev_label + 
             " ], [ 0, %" + is_false_label + " ]\n");

        

        return variable_info(_ret, "i1", "boolean");
    }


    /**
     * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(CompareExpression n, String argu) throws Exception
    {
        String reg1 = n.f0.accept(this, argu);
        String reg2 = n.f2.accept(this, argu);

        String _ret = new_reg();
        emit("%" + _ret + " = icmp slt i32" + str_reg_cons(reg1) + ", " + str_reg_cons(reg2) + "\n");   

        return variable_info(_ret, "i1", "boolean");
    }


    /**
     * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(PlusExpression n, String argu) throws Exception 
    {
        String reg_const1 = n.f0.accept(this, argu);
        String reg_const2 = n.f2.accept(this, argu);

        return emit_binary_expr(reg_const1, reg_const2, "add");
    }


    /**
     * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(MinusExpression n, String argu) throws Exception
    {
        String reg_const1 = n.f0.accept(this, argu);
        String reg_const2 = n.f2.accept(this, argu);

        return emit_binary_expr(reg_const1, reg_const2, "sub");
    }


    /**
     * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(TimesExpression n, String argu) throws Exception
    {
        String reg_const1 = n.f0.accept(this, argu);
        String reg_const2 = n.f2.accept(this, argu);

        return emit_binary_expr(reg_const1, reg_const2, "mul");
    }


    private String get_array_length(String reg)
    {
        String[] info = get_variable_info(reg);
        String base_type = info[1].contains("Int") ? "%_IntegerArray" : "%_BooleanArray";

        String _h = new_reg();
        String _ret = new_reg();

        
        // get the first field of the struct, the array's length
        emit("%" + _h + " = getelementptr " + base_type + ", " + info[1] 
                        + " %" + info[0] + ", i32 0, i32 0\n");
        
        emit("%" + _ret + " = load i32, i32* %" + _h + "\n\n");

        return _ret;
    }

    private void bounds_check(String reg1, String reg2)
    {
        String size = str_reg_cons(reg2);

        String length_reg = get_array_length(reg1);

        // Possibly replace with binary and that will be implemented
        String neg_check = new_reg();
        emit("%" + neg_check + " = icmp sge i32" + size + ", 0\n");   

        String length_check = new_reg();
        emit("%" + length_check + " = icmp slt i32" + size + ", %" + length_reg + "\n");   

        String cond = new_reg();
        emit("%" + cond + " = and i1 %" + neg_check + ", %" + length_check + "\n");

        String error_label = new_label();
        String cont_label = new_label();

        emit("br i1 %" + cond + ", label %" + cont_label + ", label %" + error_label + "\n\n");


        emit_label(error_label);

        emit("\n\tcall void @throw_oob()\n");
        emit("br label %" + cont_label + "\n\n");


        // Within bounds
        emit_label(cont_label);
    }

    private String get_arr_pos(String reg1, String reg2)
    {
        String[] info = get_variable_info(reg1);
        String base_type = info[1].contains("Int") ? "%_IntegerArray" : "%_BooleanArray";
        String size = info[1].contains("Int") ? "i32" : "i1";

        String _h1 = new_reg();
        emit("%" + _h1 + " = getelementptr " + base_type + ", " + base_type + "* %" +
            info[0] + ", i32 0, i32 1\n");

        String _h2 = new_reg();
        emit("%" + _h2 + " = load " + size + "*, " + size + "** %" + _h1 + "\n");

        String _h3 = new_reg();
        emit("%" + _h3 + " = getelementptr " + size + ", " + size + "* %" + _h2 + ", i32" 
            + str_reg_cons(reg2) + "\n");

        return _h3;
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    @Override
    public String visit(ArrayLookup n, String argu) throws Exception
    {
        String reg1 = n.f0.accept(this, argu);
        String reg2 = n.f2.accept(this, argu);
        
        bounds_check(reg1, reg2);

        String _h3 = get_arr_pos(reg1, reg2);


        String type = get_variable_info(reg1)[1].contains("Int") ? "int" : "boolean";
        String size = get_size(type);

        String _ret = new_reg();
        emit("%" + _ret + " = load " + size + ", " + size + "* %" + _h3 + "\n");
        
        return variable_info(_ret, size, type);
    }


    /**
     * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    @Override
    public String visit(ArrayLength n, String argu) throws Exception
    {
        String reg = n.f0.accept(this, argu);
        
        return variable_info(get_array_length(reg), "i32", "int");
    }

    
    /**
     * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
    @Override
    public String visit(MessageSend n, String argu) throws Exception
    {
        // String object_reg = n.f0.accept(this, argu);
        
        // String fun_name = n.f2.accept(this, argu);
        
        // String expr_list_regs = n.f4.accept(this, argu);
        
        // // %_3 = bitcast i8* %_0 to i8***
        // // %_4 = load i8**, i8*** %_3
        // // %_5 = getelementptr i8*, i8** %_4, i32 0
        // // %_6 = load i8*, i8** %_5
        // // %_7 = bitcast i8* %_6 to i32 (i8*)*
        // // %_8 = call i32 %_7(i8* %_0)

        // // offset on getelementptr is the asscending number of the function
        // // ie its the 5'th declared function of the class
        // String num = "";
        // // int _num = 0; // offset / 8

        // String ret_size = ""; // function return size

        // String _h1 = new_reg();
        // String _h2 = new_reg();
        // String _h3 = new_reg();
        // String _h4 = new_reg();
        // String _h5 = new_reg();
        // String _ret = new_reg();

        // emit(_h1 + " = bitcast i8* %" + object_reg + " to i8***\n");
        // emit(_h2 + " = load i8**, i8*** %" + _h1 + "\n");
        // emit(_h3 + " = getelementptr, i8** %" + _h2 + ", i32" + num + "\n");
        // emit(_h4 + " = load i8**, i8*** %" + _h3 + " to i8***\n");
        // emit(_h5 + " = bitcast i8* %" + _h4 + " to ι32 (i8*)*\n");
        // emit(_ret + " = call " + ret_size + " %" + _h5 + "(" + expr_list_regs + ")\n");
        

        // return _ret;
        return null;
    }


    /**
     * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    @Override
    public String visit(ExpressionList n, String argu) throws Exception
    {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }


    /**
     * f0 -> ( ExpressionTerm() )*
    */
    @Override
    public String visit(ExpressionTail n, String argu) throws Exception
    {
        return n.f0.accept(this, argu);
    }


    /**
     * f0 -> ","
    * f1 -> Expression()
    */
    @Override
    public String visit(ExpressionTerm n, String argu) throws Exception
    {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }


    /**
     * f0 -> NotExpression()
    *       | PrimaryExpression()
    */
    @Override
    public String visit(Clause n, String argu) throws Exception
    {
        return n.f0.accept(this, argu);
    }


    /**
     * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | BracketExpression()
    */
    @Override
    public String visit(PrimaryExpression n, String argu) throws Exception
    {
        String _ret = n.f0.accept(this, argu);
		
        if (n.f0.which != 3)
            return _ret;

		return st.is_field(_ret, argu) ? load_field(_ret, get_class(argu), true) : load_local(_ret, argu);
	
    }


    /**
     * f0 -> <INTEGER_LITERAL>
    */
    @Override
    public String visit(IntegerLiteral n, String argu) throws Exception
    {
        return variable_info(n.f0.toString(), "i32", "literal");
    }


    /**
     * f0 -> "true"
    */
    @Override
    public String visit(TrueLiteral n, String argu) throws Exception
    {
        return variable_info("1", "i1", "literal");
    }


    /**
     * f0 -> "false"
    */
    @Override
    public String visit(FalseLiteral n, String argu) throws Exception
    {
        return variable_info("0", "i1", "literal");
    }


    /**
     * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, String argu) throws Exception
    {
        return n.f0.toString();
    }


    /**
     * f0 -> "this"
    */
    @Override
    public String visit(ThisExpression n, String argu) throws Exception
    {
        return variable_info("this", "i8*", get_class(argu));
    }


    private String allocate_arr(String reg, String size, String type)
    {
        String value_size = size.equals("_BooleanArray") ? "1" : "4";

        // check value of reg > 0
        String _cond = new_reg();
        emit("%" + _cond + " = icmp sge i32" + size + ", 0\n");   

        String error_label = new_label();
        String cont_label = new_label();

        emit("br i1 %" + _cond + ", label %" + cont_label + ", label %" + error_label + "\n\n");

        emit_label(error_label);

        emit("\n\tcall void @throw_oob()\n");
        emit("br label %" + cont_label + "\n\n");


        // Allocate space
        emit_label(cont_label);

        String _h = new_reg();
        String _h1 = new_reg();
        String _ret = new_reg();

        // create array struct
        emit("%" + _h + " = call i8* @calloc(i32 1, i32 12)\n");
        emit("%" + _ret + " = bitcast i8* %" + _h + " to %" + type + "*\n");

        // get the first field of the struct, the array's length
        emit("%" + _h1 + " = getelementptr %" + type + ", %" + type + "* %" 
                        + _ret + ", i32 0, i32 0\n");
        // Store length on that address
        emit("store i32" + size + ", i32* %" + _h1 + "\n");


        String _h2 = new_reg();
        String _h3 = new_reg();
        String _h4 = new_reg();


        String size_type = type.contains("Int") ? "i32" : "i1";

        // calloc array
        emit("%" + _h2 + " = call i8* @calloc(i32 " + value_size + ", i32 " + size + ")\n");
        emit("%" + _h3 + " = bitcast i8* %" + _h2 + " to " + size_type + "*\n");
        // Get pointer to the field of the struct that represents the array
        emit("%" + _h4 + " = getelementptr %" + type + ", %" + type + "* %" +
                        _ret + ", i32 0, i32 1\n");

        emit("store " + size_type + "* %" + _h3 + ", " + size_type + "** %" + _h4 + "\n\n");

        return _ret;
    }

    /**
     * f0 -> "new"
    * f1 -> "boolean"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    @Override
    public String visit(BooleanArrayAllocationExpression n, String argu) throws Exception
    {
        String reg = n.f3.accept(this, argu);
        String size = str_reg_cons(reg);

        return variable_info(allocate_arr(reg, size, "_BooleanArray"), "_BooleanArray*", "boolean[]");
    }


    /**
     * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    @Override
    public String visit(IntegerArrayAllocationExpression n, String argu) throws Exception
    {
        String reg = n.f3.accept(this, argu);
        String size = str_reg_cons(reg);
        

        return variable_info(allocate_arr(reg, size, "_IntegerArray"), "_IntegerArray*", "int[]");

    }


    /**
     * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    @Override
    public String visit(AllocationExpression n, String argu) throws Exception
    {
        
        String type = n.f1.accept(this, argu);
		String dim = get_dim(type);
        Class _class = st.get_class(type);
        String size = Integer.toString(_class.get_size());

		String _ret = new_reg();
		String _h1 = new_reg();
		String _h2 = new_reg();

		emit("%" + _ret + " = call i8* @calloc(i32 1, i32 " + size + ")\n");
		
		emit("%" + _h1 + " = bitcast i8* %" + _ret + " to i8***\n");

		emit("%" + _h2 + " = getelementptr " + dim + ", " + dim + "* @." + type + "_vtable, i32 0, i32 0\n");

		emit("store i8** %" + _h2 + ", i8*** %" + _h1 + "\n\n");


		// return register where the calloc was saved in
        return variable_info(_ret, "i8*", type); 
    }

    
    /**
     * f0 -> "!"
    * f1 -> Clause()
    */
    @Override
    public String visit(NotExpression n, String argu) throws Exception
    {
        String reg = n.f1.accept(this, argu);

        // negation of bool is just a xor 
        String _ret = new_reg();
        emit("%" + _ret + " = xor i1 1," + str_reg_cons(reg) + "\n\n");

        return variable_info(_ret, "i1", "boolean");
    }


    /**
     * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    @Override
    public String visit(BracketExpression n, String argu) throws Exception
    {
        return n.f1.accept(this, argu);
    }

}

