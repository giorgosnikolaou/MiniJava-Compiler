import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;

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

    public IRVisitor(SymbolTable st) 
    {
		this.st = st;
	}

	private void emit_tabs(int tabs)
	{
		emit("\t".repeat(tabs));	
	}
	
	private void emit_tabs()
	{
		emit_tabs(tab_count);
	}

    private void emit(String text)
    {
        // write text to file 
        System.out.print(text);
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

    public Function fun_scope(String _class, String _fun)
    {   
        return st.get_class(_class).get_function(_fun);
    }

    private void emit_vtable_function(String _class, String _name)
    {

        Function _fun = fun_scope(_class, _name);

        emit("i8* bitcast (");
        emit(get_size(_fun.type()) + "(i8*"); // return type, "this" argument

        for (Variable arg : _fun.get_arg_info())
            emit(", " + get_size(arg.type()));
        
        emit(")* @" + _class + "." + _name + " to i8*)");
        
    }

    private void emit_vtable_functions(Class _class)
    {
        int i = _class.vtable.size();

        if (_class.vtable.size() > 1)
            emit("\n\t");

        for (Map.Entry<String, String> entry : _class.vtable.entrySet())
        {   

            String fun = entry.getKey();
            String cl = entry.getValue();

            emit_vtable_function(cl, fun);
            
            if (--i > 0)
                emit(",\n\t");
        }

        if (_class.vtable.size() > 1)
            emit("\n");
        
    }

    private void emit_vtable()
    {
        for (Map.Entry<String, Class> entry : st.classes.entrySet())
        {   
            Class _class = entry.getValue();
			String dim = "[" + _class.vtable.size() + " x i8*]";
			classes_vt_size.put(_class.name(), dim);

            emit("@." + _class.name() + "_vtable = global " + dim + " [");
            emit_vtable_functions(_class);
            emit("]\n");
        }
        emit("\n");
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

    /**
        * f0 -> MainClass()
        * f1 -> ( TypeDeclaration() )*
        * f2 -> <EOF>
    */
    @Override
    public String visit(Goal n, String argu) throws Exception 
    {
        // emit("%_BooleanArray = type { i32, i8* }\n");
        // emit("%_IntegerArray = type { i32, i8* }\n\n");
		emit("%_BooleanArray = type { i32, [0 x i1] }\n");
		emit("%_IntegerArray = type { i32, [0 x i32] }\n\n");

        // emit vtables
        // emit vtables
        // .<class name>_vtable = global [<number of functions> x i8* ] 
        //      [i8* bitcast (<ret type> (i8*<,> <argument types>)* @<class name>.<function name> to i8*), ...]
        
        emit_vtable();

        // emit boilerplate
        emit("declare i8* @calloc(i32, i32)\n");
        emit("declare i32 @printf(i8*, ...)\n");
        emit("declare void @exit(i32)\n\n");

        emit("@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n");
        emit("@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n");
        emit("define void @print_int(i32 %i) {\n");
        emit("\t%_str = bitcast [4 x i8]* @_cint to i8*\n");
        emit("\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n");
        emit("\tret void\n");
        emit("}\n\n");

        emit("define void @throw_oob() {\n");
        emit("\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n");
        emit("\tcall i32 (i8*, ...) @printf(i8* %_str)\n");
        emit("\tcall void @exit(i32 1)\n");
        emit("\tret void\n");
        emit("}\n\n");
        

        // Continue
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);

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
        emit("define i32 @main() {\n");

		tab_count++;
        n.f14.accept(this, class_name + st.class_delimiter + "main");
        n.f15.accept(this, class_name + st.class_delimiter + "main");
        
        emit("\n\tret i32 0\n}\n\n");
		
		tab_count--;


        return null;
   }

    /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
    @Override
   	public String visit(TypeDeclaration n, String argu) throws Exception {
        return n.f0.accept(this, argu);
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
    public String visit(ClassDeclaration n, String argu) throws Exception {
        
		
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
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        
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
    public String visit(VarDeclaration n, String argu) throws Exception {
        
		
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);
        
		emit(init_local(type, name));

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
    public String visit(MethodDeclaration n, String argu) throws Exception {
		tab_count++;
		// argu should have the class name 
        String type = n.f1.accept(this, argu);
        String name = n.f2.accept(this, argu);
		
		String size = get_size(type);

		emit("define " + size + " @" + argu +  "." + name + "(i8* %this");
		
		String initialize = n.f4.present() ? n.f4.accept(this, argu) : "";
			
		emit(") {\n" + initialize);


        n.f7.accept(this, argu + st.class_delimiter + name);
        n.f8.accept(this, argu + st.class_delimiter + name);

        // ret reg below
        n.f10.accept(this, argu);
		
		emit("\n}\n\n");
		tab_count--;
        return null;
    }

    /**
     * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */
    @Override
    public String visit(FormalParameterList n, String argu) throws Exception {
        String ret = n.f0.accept(this, argu);

        if (n.f1 != null) 
            ret += n.f1.accept(this, argu);

        return ret;
    }

    /**
     * f0 -> Type()
    * f1 -> Identifier()
    */
    @Override
    public String visit(FormalParameter n, String argu) throws Exception {
        
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);

		String size = get_size(type);

		emit(", " + size + " %." + name);

        return init_local(type, name, "%." + name);
    }

    /**
     * f0 -> ( FormalParameterTerm() )*
    */
    @Override
    public String visit(FormalParameterTail n, String argu) throws Exception {
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
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    @Override
    public String visit(Type n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> BooleanArrayType()
    *       | IntegerArrayType()
    */
    @Override
    public String visit(ArrayType n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "boolean"
    * f1 -> "["
    * f2 -> "]"
    */
    @Override
    public String visit(BooleanArrayType n, String argu) throws Exception {
        
        return "boolean[]";
    }

    /**
     * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
    @Override
    public String visit(IntegerArrayType n, String argu) throws Exception {
        
		return "int[]";
    }

    /**
     * f0 -> "boolean"
    */
    @Override
    public String visit(BooleanType n, String argu) throws Exception {
        return "boolean";
    }

    /**
     * f0 -> "int"
    */
    @Override
    public String visit(IntegerType n, String argu) throws Exception {
        return "int";
    }

	private String load_field(String name, String _class)
	{
		Class cl = st.get_class(_class);
		Variable var = cl.get_variable(name);
		int offset = var.offset() + 8; // add vtable 8
		String size = get_size(var.type());

		String _h1 = new_reg();
		String _ret = new_reg();

		emit_tabs();
		emit("%" + _h1 + " = getelementptr i8, i8* %this, " + size + " " + offset + "\n");

		emit_tabs();
		emit("%" + _ret + " = bitcast i8* %" + _h1 + " to " + size + "*\n");

		return _ret;
	}

    /**
     * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    @Override
    public String visit(AssignmentStatement n, String argu) throws Exception {
        
		// argu should be the scope we in
		// ie <class name> <class delimiter> <function name>
        String name = n.f0.accept(this, argu);
		String type = st.resolve_var_type(name, argu);
		String size = get_size(type);

		
        String reg_cons = n.f2.accept(this, argu);
		
		// emit_tabs();
		// emit(argu + "\n");
		if (!st.is_field(name, argu))
		{
			emit_tabs();
			emit("store " + size + (reg_cons.startsWith("_") ? " %" : " ") + 
				 reg_cons + ", " + size + "* %" + name + "\n\n");

			return null;
		}

		// case that lvalue is a field
		
		String reg = load_field(name, argu.split(st.class_delimiter)[0]);
		emit_tabs();
		emit("store " + size + (reg_cons.startsWith("_") ? " %" : " ") + 
				reg_cons + ", " + size + "* %" + reg + "\n\n");



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
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        return _ret;
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
    public String visit(IfStatement n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    @Override
    public String visit(WhileStatement n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    @Override
    public String visit(PrintStatement n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> AndExpression()
    *       | CompareExpression()
    *       | PlusExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | Clause()
    */
    @Override
    public String visit(Expression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    @Override
    public String visit(AndExpression n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(CompareExpression n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(PlusExpression n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(MinusExpression n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(TimesExpression n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    @Override
    public String visit(ArrayLookup n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    @Override
    public String visit(ArrayLength n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
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
    public String visit(MessageSend n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    @Override
    public String visit(ExpressionList n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
    */
    @Override
    public String visit(ExpressionTail n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> ","
    * f1 -> Expression()
    */
    @Override
    public String visit(ExpressionTerm n, String argu) throws Exception {
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
    public String visit(Clause n, String argu) throws Exception {
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
    public String visit(PrimaryExpression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
    */
    @Override
    public String visit(IntegerLiteral n, String argu) throws Exception {
        return n.f0.toString();
    }

    /**
     * f0 -> "true"
    */
    @Override
    public String visit(TrueLiteral n, String argu) throws Exception {
        return "1";
    }

    /**
     * f0 -> "false"
    */
    @Override
    public String visit(FalseLiteral n, String argu) throws Exception {
        return "0";
    }

    /**
     * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, String argu) throws Exception {
        return n.f0.toString();
    }

    /**
     * f0 -> "this"
    */
    @Override
    public String visit(ThisExpression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    
    /**
     * f0 -> "new"
    * f1 -> "boolean"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    @Override
    public String visit(BooleanArrayAllocationExpression n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    @Override
    public String visit(IntegerArrayAllocationExpression n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    @Override
    public String visit(AllocationExpression n, String argu) throws Exception {
        
        String type = n.f1.accept(this, argu);
		String dim = get_dim(type);

		String _ret = new_reg();
		String _h1 = new_reg();
		String _h2 = new_reg();

		emit_tabs();
		emit("%" + _ret + " = call i8* @calloc(i32 1, i32 38)\n");
		
		emit_tabs();
		emit("%" + _h1 + " = bitcast i8* %" + _ret + " to i8***\n");

		emit_tabs();
		emit("%" + _h2 + " = getelementptr " + dim + ", " + dim + "* @." + type + "_vtable, i32 0, i32 0\n");

		emit_tabs();
		emit("store i8** %" + _h2 + ", i8*** %" + _h1 + "\n");


		// return register where the calloc was done
        return _ret; 
    }

    /**
     * f0 -> "!"
    * f1 -> Clause()
    */
    @Override
    public String visit(NotExpression n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    @Override
    public String visit(BracketExpression n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }

}
