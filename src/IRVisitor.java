import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;
import java.io.FileWriter;
import SymbolTable.*;
import SymbolTable.Class;

public class IRVisitor extends GJDepthFirst<String,String> {
   
    
    SymbolTable st;
	private FileWriter writer;
	Map<String, String> classes_vt_size = new HashMap<String, String>();

    private int label_counter = 0;
    private String last_label = null;
    private int reg_counter = 0;
	private int tab_count = 0;

    public IRVisitor(SymbolTable st, FileWriter writer) 
    {
		this.st = st;
		this.writer = writer;
	}

    //  ------------------------------------------------------------------------------
    //  |       Basic functions                                                      |
    //  ------------------------------------------------------------------------------

    private String new_label()
    {
        return "L" + label_counter++;
    }

    private String new_reg()
    {
        return "_" + reg_counter++;
    }

    private void emit_pure(String text) throws Exception
    {
        // write text to file 
        // System.err.print(text);
        writer.write(text);
    }


    //  ------------------------------------------------------------------------------
    //  |       LLVM instruction functions                                           |
    //  ------------------------------------------------------------------------------

    private void store(String size, String reg_cons, String reg) throws Exception
    {
        emit("store " + size + str_reg_cons(reg_cons) + ", " + size + "*" + str_reg(reg));
    }

    private void br(String label) throws Exception
    {
        emit("br label %" + label + "\n");
    }

    private void br(String reg_cons, String true_label, String false_label) throws Exception
    {
        emit("br i1" + str_reg_cons(reg_cons) + ", label %" + true_label + ", label %" + false_label + "\n");
    }

    private void label(String label) throws Exception
    {
        last_label = label;
        emit_pure(label + ":\n");
    }

    private String phi(String reg_cons1, String label1, String reg_cons2, String label2) throws Exception
    {
        String _ret = new_reg();
        emit("%" + _ret + " = phi i1 [" + str_reg_cons(reg_cons1) + ", %" + label1 + " ], [" + str_reg_cons(reg_cons2) + ", %" + label2 + " ]");
        return _ret;
    }
    
    private String binary(String op, String type, String reg_cons1, String reg_cons2) throws Exception
    {
        String _ret = new_reg();
        emit("%" + _ret + " = " + op + " " + get_size(type) + str_reg_cons(reg_cons1) + "," + str_reg_cons(reg_cons2) + "\n");
        return _ret;
    }
    
    private String icmp(String op, String reg_cons1, String reg_cons2) throws Exception
    {
        return binary("icmp " + op, "int", reg_cons1, reg_cons2);
    }

    private String bitcast(String reg_cons, String type_from, String type_to) throws Exception
    {
        String _ret = new_reg();
        emit("%" + _ret + " = bitcast " + type_from + str_reg_cons(reg_cons) + " to " + type_to);
        return _ret;
    }

    private String load(String type, String reg) throws Exception
    {
        String _ret = new_reg();
        emit("%" + _ret + " = load " + type + ", " + type + "*" + str_reg(reg));
        return _ret;
    }

    private String getelementptr(String type, String reg, String reg_cons) throws Exception
    {
        String _ret = new_reg();
        emit("%" + _ret + " = getelementptr " + type + ", " + type + "*" + str_reg(reg) + ", i32" + str_reg_cons(reg_cons));
        return _ret;
    }

    private String getelementptr(String type, String reg, String reg_cons1, String reg_cons2) throws Exception
    {
        String _ret = new_reg();
        emit("%" + _ret + " = getelementptr " + type + ", " + type + "*" + str_reg(reg) + 
            ", i32" + str_reg_cons(reg_cons1) + ", i32" + str_reg_cons(reg_cons2));
        return _ret;
    }
    

    //  ------------------------------------------------------------------------------
    //  |       Helpers                                                              |
    //  ------------------------------------------------------------------------------

    private void emit(String text) throws Exception
    {
        emit_pure("\t".repeat(tab_count));	
        emit_pure(text + "\n");
    }

    private void emit_vtable_functions(Class _class) throws Exception
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
            emit_pure(get_size(_fun.type()) + " (i8*"); // return type, "this" argument

            for (Variable arg : _fun.get_arg_info())
                emit_pure(", " + get_size(arg.type()));
            
            emit_pure(")* @" + cl + "." + fun + " to i8*)");
            
            if (--i > 0)
                emit_pure(",\n\t");
        }

        if (_class.vtable.size() > 1)
            emit_pure("\n");
        
    }

    private void emit_vtable() throws Exception
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


	private String init_local(String type, String name)
	{
		return init_local(type, name, get_size(type).contains("*") ? "null" : "0");
	}

	private String init_local(String type, String name, String initial)
	{
		String size = get_size(type);

		String _ret = "\t%" + name + " = alloca " + size + "\n";
		_ret += "\tstore " + size + " " + initial + ", " + size + "* %" + name + "\n\n"; 

		return _ret;
	}

	private String load_field(String name, String _class, boolean rvlaue) throws Exception
	{
		Variable var = st.resolve_var(name, _class);

		String size = get_size(var.type());

		String _h1 = getelementptr("i8", "this", "" + (var.offset() + 8));
		String _h2 = bitcast("%" + _h1, "i8*", size + "*");

		if (rvlaue)
			return variable_info(load(size, _h2), size, var.type());
		
        
        return variable_info(_h2, size + '*', var.type());
	}

	private String load_local(String name, String scope) throws Exception
    {
        String type = st.resolve_var_type(name, scope);
        String size = get_size(type);

        return variable_info(load(size, name), size, type);
    }


    private String get_size(String type)
    {
        return  type.equals("boolean") ? "i1" :
                type.equals("int") ? "i32" :
                type.equals("boolean[]") ? "%_BooleanArray*" :
                type.equals("int[]") ? "%_IntegerArray*" : "i8*";
    }

	private String get_dim(String _class)
	{
		return classes_vt_size.get(_class);
	}

    private boolean is_literal(String[] info)
    {
        return info.length < 3 || info[2].equals("literal");
    }

    private String str_reg_cons(String var)
    {
        String[] info = get_variable_info(var);
        return (is_literal(info) ? " " : " %") + info[0];
    }

    private String str_reg(String reg)
    {
        String[] info = get_variable_info(reg);
        assert(!is_literal(info));
        return (info[0].startsWith("@") ? " " : " %") + info[0];
    }

    private String[] get_variable_info(String var)
    {
        return var.split(st.class_delimiter);
    }

    private String variable_info(String name, String size, String type)
    {
        return name + st.class_delimiter + size + st.class_delimiter + type;
    }

    private String get_size_addr(String reg)
    {
        String[] info = get_variable_info(reg);
        return info[1] + str_reg_cons(reg);
    }
    
    private String get_class(String scope)
    {
        return scope.split(st.class_delimiter)[0];
    }

    private int find_vtable_offset(String _class, String _func) throws Exception
    {
        int i = 0;
        for (Map.Entry<String, String> entry : st.get_class(_class).vtable.entrySet())
        {
            String fun = entry.getKey();
            if (fun.equals(_func))

                return i * 8;
            i++;
        }
        
        throw new Exception("SHOULD NEVER GET HERE");
    } 
    

    private String get_array_length(String reg) throws Exception
    {
        String[] info = get_variable_info(reg);
        String base_type = info[1].contains("Int") ? "%_IntegerArray" : "%_BooleanArray";

        String _h = getelementptr(base_type, info[0], "0", "0");
        String _ret = load("i32", _h);

        return variable_info(_ret, "i32", "int");
    }

    private String bounds_check(String reg1, String reg2) throws Exception
    {
        // Possibly replace with sc_and
        String neg_check = icmp("sge", reg2, "0");
        String length_check = icmp("slt", reg2, get_array_length(reg1));
        String cond = binary("and", "boolean", "%" + neg_check, "%" + length_check);

        String error_label = new_label();
        String cont_label = new_label();

        br("%" + cond, cont_label, error_label);

        label(error_label);
        emit("\n\tcall void @throw_oob()");
        br(cont_label);

        label(cont_label);

        String[] info = get_variable_info(reg1);
        String base_type = info[1].contains("Int") ? "%_IntegerArray" : "%_BooleanArray";
        String size = info[1].contains("Int") ? "i32" : "i1";

        String _h1 = getelementptr(base_type, info[0], "0", "1");
        String _h2 = load(size + "*", _h1);
        
        return getelementptr(size, _h2, reg2);
    }

    private String allocate_arr(String reg, String size, String type) throws Exception
    {
        String value_size = size.equals("_BooleanArray") ? "1" : "4";

        String _cond = icmp("sge", size, "0");

        String error_label = new_label();
        String cont_label = new_label();

        br("%" + _cond, cont_label, error_label);

        label(error_label);

        emit("\n\tcall void @throw_oob()");
        br(cont_label);

        // Allocate space
        label(cont_label);

        // create array struct
        String _h = new_reg();
        emit("%" + _h + " = call i8* @calloc(i32 1, i32 12)");
        String _ret = bitcast("%" + _h, "i8*", "%" + type + "*");
        String _h1 = getelementptr("%" + type, _ret, "0", "0");

        // Store length on that address
        store("i32", size, _h1);
        
        String size_type = type.contains("Int") ? "i32" : "i1";

        String _h2 = new_reg();
        // calloc array
        emit("%" + _h2 + " = call i8* @calloc(i32 " + value_size + ", i32 " + size + ")");
        String _h3 = bitcast("%" + _h2, "i8*", size_type + "*");
        
        // Get pointer to the field of the struct that represents the array
        String _h4 = getelementptr("%" + type, _ret, "0", "1");
        store(size_type + "*", "%" + _h3, _h4);

        return _ret;
    }

    
    //  ------------------------------------------------------------------------------
    //  |       Visitor Implementation                                               |
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
        reg_counter = 0;
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
		emit("ret " + size + str_reg_cons(_ret));
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
		
		String reg = st.is_field(name, argu) ? 
                    load_field(name, get_class(argu), false) : 
                    variable_info(name, get_size(type), type);
        

        String reg_cons = n.f2.accept(this, argu);

        store(get_size(type), reg_cons, reg);
        
        emit("");
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

        if (st.is_field(name, argu))
            reg1 = variable_info(load(get_size(type), reg1), get_size(type), type);

        
        String reg2 = n.f2.accept(this, argu);

        String _h = bounds_check(reg1, reg2);

        String reg_cons = n.f5.accept(this, argu);
        store(type.contains("int") ? "i32" : "i1", reg_cons, _h);

        emit("");
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
        
        String then_label = new_label(); 
        String else_label = new_label(); 
        String cont_label = new_label(); 

        br(cond_reg, then_label, else_label);

        label(then_label);
        n.f4.accept(this, argu);
        br(cont_label);

        label(else_label);
        n.f6.accept(this, argu);
        br(cont_label);

        label(cont_label);

        emit("");
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

        br(start_label);
        label(start_label);

        String cond_reg = n.f2.accept(this, argu);       
        br(cond_reg, loop_label, cont_label);
             
        label(loop_label);
        n.f4.accept(this, argu);
        br(start_label);

        label(cont_label);

        emit("");
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

		emit("call void (i32) @print_int(i32 " + str_reg_cons(reg) + ")\n");
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

        String is_true_label = new_label();
        String is_false_label = new_label();
        String cont_label = new_label();

        br(reg1, is_true_label, is_false_label);

        label(is_false_label);
        br(cont_label);

        label(is_true_label);
        String reg2 = n.f2.accept(this, argu);
        br(cont_label);


        String prev_label = last_label;
        label(cont_label);

        String _ret = phi(reg2, prev_label, "0", is_false_label);

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

        String _ret = icmp("slt", reg1, reg2); 
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
        String _ret = binary("add", "int", reg_const1, reg_const2);
        return variable_info(_ret, "i32", "int");
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
        String _ret = binary("sub", "int", reg_const1, reg_const2);
        return variable_info(_ret, "i32", "int");
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
        String _ret = binary("mul", "int", reg_const1, reg_const2);
        return variable_info(_ret, "i32", "int");
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
        
        String _h = bounds_check(reg1, reg2);

        String type = get_variable_info(reg1)[1].contains("Int") ? "int" : "boolean";
        String size = get_size(type);

        return variable_info(load(get_size(type), _h), size, type);
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
        
        return get_array_length(reg);
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
        String object_reg = n.f0.accept(this, argu);
        String[] info = get_variable_info(object_reg);
        
        String fun_name = n.f2.accept(this, argu);
        
        String expr_list_regs = n.f4.present() ? n.f4.accept(this, argu) : "";
        
        Function fun = st.get_function(info[2], fun_name);
        String num = "" + find_vtable_offset(info[2], fun_name) / 8;
        String ret_size = get_size(fun.type()); 

        // Get vtable
        String _h1 = bitcast(object_reg, "i8*", "i8***");
        String _h2 = load("i8**", _h1);
        String _h3 = getelementptr("i8*", _h2, num);
        String _h4 = load("i8*", _h3);
        

        String ar = fun.get_arguments_types();
        String[] args = ar.length() > 0 ? ar.split(",") : new String[0];

        String new_args = "i8*";
        for (String arg : args)
            new_args += ", " + get_size(arg);
        

        String _h5 = bitcast("%" + _h4, "i8*", ret_size + " (" + new_args + ")*");
        String _ret = new_reg();
        emit("%" + _ret + " = call " + ret_size + " %" + _h5 + "(i8*" + str_reg_cons(object_reg) + 
            (expr_list_regs.length() != 0 ? "," + expr_list_regs : "") + ")");


        return variable_info(_ret, ret_size, fun.type());
    }

    /**
     * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    @Override
    public String visit(ExpressionList n, String argu) throws Exception
    {
        String reg = get_size_addr(n.f0.accept(this, argu));

        if (n.f1 != null) 
            reg += n.f1.accept(this, argu);

        return reg;
    }


    /**
     * f0 -> ( ExpressionTerm() )*
    */
    @Override
    public String visit(ExpressionTail n, String argu) throws Exception
    {
        String ret = "";
        
        for (Node node : n.f0.nodes) 
            ret += ", " + get_size_addr(node.accept(this, argu));
        
        return ret;
    }


    /**
     * f0 -> ","
    * f1 -> Expression()
    */
    @Override
    public String visit(ExpressionTerm n, String argu) throws Exception
    {
        return n.f1.accept(this, argu);
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

        return variable_info(allocate_arr(reg, size, "_BooleanArray"), "%_BooleanArray*", "boolean[]");
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
        

        return variable_info(allocate_arr(reg, size, "_IntegerArray"), "%_IntegerArray*", "int[]");

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

		String _ret = new_reg();

		emit("%" + _ret + " = call i8* @calloc(i32 1, i32 " + st.get_class(type).get_size() + ")");
		
		String _h1 = bitcast("%" + _ret, "i8*", "i8***");

		String _h2 = getelementptr(get_dim(type), "@." + type + "_vtable", "0", "0");

		emit("store i8** %" + _h2 + ", i8*** %" + _h1 + "\n");
        store("i8**", "%" + _h2, _h1);

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
        return variable_info(binary("xor", "boolean", "1", reg), "i1", "boolean");
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

