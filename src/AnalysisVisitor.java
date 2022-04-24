//
// Generated by JTB 1.3.2 DIT@UoA patched
//

import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.*;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import SymbolTable.*;
import SymbolTable.Class;

/**
 * Provides default methods which visit each node in the tree in depth-first
 * order.  Your visitors may extend this class.
 */
public class AnalysisVisitor extends GJDepthFirst<String,String> {
   //
   // User-generated visitor methods below
   //

   SymbolTable st;

    public AnalysisVisitor(SymbolTable st) 
    {
		this.st = st;
	}

    private boolean is_int(String type)
    {
        return type.equals("int");
    }

    private boolean is_int_arr(String type)
    {
        return type.equals("int[]");
    }

    private boolean is_boolean(String type)
    {
        return type.equals("boolean");
    }

    private boolean is_boolean_arr(String type)
    {
        return type.equals("boolean[]");
    }

    private boolean int_type(String type)
    {
        return is_int(type) || is_int_arr(type);
    }

    private boolean boolean_type(String type)
    {
        return is_boolean(type) || is_boolean_arr(type);
    }

    private boolean is_array(String type)
    {
        return type.endsWith("[]");
    }

    private String get_array_type(String type)
    {
        return type.replace("[]", "");
    }

    private boolean match_array_el(String arr_type, String el_type)
    {
        return el_type.equals(get_array_type(arr_type));
    }
  
    private boolean is_class(String type)
    {
        return st.is_defined_class(type);
    }

    private boolean same_type(String type1, String type2)
    {
        return type1.equals(type2) && 
                (   is_class(type1) || 
                    int_type(type1) || 
                    boolean_type(type1)
                );
    }

    private boolean is_subclass(String _class, String sub)
    {
        if (!is_class(_class) || !is_class(sub))
            return false;

        return st.is_subclass(_class, sub);
    }

    private boolean type_matching(String type1, String type2)
    {
        if (same_type(type1, type2) || is_subclass(type1, type2))
            return true;
            
        return false;
    }
    
    private boolean compatable_types(String call, String prototype)
    {
        if (call.equals(prototype))
            return true;
        
        String[] tokens1 = call.split(",");
        String[] tokens2 = prototype.split(",");

        if (tokens1.length != tokens2.length)
            return false;

        for (int i = 0; i < tokens1.length; i++)
        {
            if (!type_matching(tokens2[i], tokens1[i]))
                return false;
        }

        return true;
    }


    @Override
	public String visit(IntegerLiteral n, String argu) throws Exception
    {
        String num = n.f0.tokenImage;
        
        try
        {
            Integer.parseInt(num);
        }
        catch (Exception e) 
        {
            throw new Exception("Number " + num + " is outside of integer range");
        }
        
		return "int";
	}

	@Override
	public String visit(TrueLiteral n, String argu) 
    {
		return "boolean";
	}

	@Override
	public String visit(FalseLiteral n, String argu) 
    {
		return "boolean";
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
    public String visit(BooleanType n, String argu) 
    {
        return "boolean";
    }

    @Override
    public String visit(IntegerType n, String argu) 
    {
        return "int";
    }

    @Override
    public String visit(Identifier n, String argu) throws Exception
    {
        return n.f0.toString();
        // return st.resolve_var_type(n.f0.toString(), argu);
    }

    @Override
    public String visit(PrimaryExpression n, String argu) throws Exception 
    {
        String _ret = n.f0.accept(this, argu);
        return n.f0.which == 3 ? st.resolve_var_type(_ret, argu) : _ret;
    }

    @Override
    public String visit(ThisExpression n, String argu)
    {
        String[] scope = argu.split(st.class_delimiter);
        Class _class = st.get_class(scope[0]);

        return _class.name();
    }

    @Override
    public String visit(BooleanArrayAllocationExpression n, String argu) throws Exception 
    {
        String type = n.f3.accept(this, argu);

        if (!is_int(type))
            throw new Exception("Size on allocation of array should be an integer");

        return "boolean[]";
    }

    @Override
    public String visit(IntegerArrayAllocationExpression n, String argu) throws Exception 
    {
        String type = n.f3.accept(this, argu);

        if (!is_int(type))
            throw new Exception("Size on allocation of array should be an integer");

        return "int[]";
    }

    @Override
    public String visit(AllocationExpression n, String argu) throws Exception 
    {
        
        String name = n.f1.accept(this, argu);
        
        if (st.get_class(name) == null)
            throw new Exception("Class " + name + " does not exist");

        return name;
    }

    @Override
    public String visit(NotExpression n, String argu) throws Exception
    {
        String type = n.f1.accept(this, argu);

        if (!is_boolean(type))
            throw new Exception("Can only negate boolean values");

        return "boolean";
    }

    @Override
    public String visit(BracketExpression n, String argu) throws Exception 
    {
        return n.f1.accept(this, argu);
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
        
        n.f14.accept(this, class_name + st.class_delimiter + "main");
        n.f15.accept(this, class_name + st.class_delimiter + "main");
        
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

        n.f3.accept(this, class_name);
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

        n.f5.accept(this, class_name);
        n.f6.accept(this, class_name);

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
        String name = n.f2.accept(this, argu);

        Class _class = st.get_class(argu);
        
        Function new_function = _class.get_function(name);
        Function inherited = st.get_function(_class.superclass(), name);

        if (inherited != null)
        {
            String fun_args = new_function.get_arguments();
            String inh_args = inherited.get_arguments();

            if (inherited.type() != new_function.type() ||
                !fun_args.equals(inh_args))
                throw new Exception("Overloading function is not allowed");
           
        }

        n.f7.accept(this, argu + st.class_delimiter + name);
        n.f8.accept(this, argu + st.class_delimiter + name);

        String ret_type = n.f10.accept(this, argu + st.class_delimiter + name);

        if (!type_matching(new_function.type(), ret_type))
            throw new Exception("Return type doesn't match the type of the value being returned");

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
        
        if (!int_type(type) && !boolean_type(type) && !is_class(type))
            throw new Exception("Type " + type + " is not recognised");

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
    public String visit(AssignmentStatement n, String argu) throws Exception 
    {
        String type = st.resolve_var_type(n.f0.accept(this, argu), argu);
        String expr_type = n.f2.accept(this, argu);
        
        if (!type_matching(type, expr_type))
            throw new Exception("Types on assignment don't match");

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
        String type = st.resolve_var_type(n.f0.accept(this, argu), argu);
        String index_type = n.f2.accept(this, argu);
        String expr_type = n.f5.accept(this, argu);

        if (!is_int(index_type))
            throw new Exception("Array index must be integer");

        if (!is_array(type) || !match_array_el(type, expr_type))
            throw new Exception("Types on array assignment don't match");

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
        
        String type = n.f2.accept(this, argu);
        
        if (!is_boolean(type))
            throw new Exception("Excpression inside if must be a evaluated to boolean");

        n.f4.accept(this, argu);
        n.f6.accept(this, argu);

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
        
        String type = n.f2.accept(this, argu);
        
        if (!is_boolean(type))
            throw new Exception("Excpression inside while must be a evaluated to boolean");

        n.f4.accept(this, argu);

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
        
        String type = n.f2.accept(this, argu);
        
        if (!is_int(type))
            throw new Exception("Expression printed must be a evaluated to int");

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
        
        String type1 = n.f0.accept(this, argu);
        String type2 = n.f2.accept(this, argu);

        if (!is_boolean(type1) || !is_boolean(type2))
            throw new Exception("Clauses of logical \"and\" must be of type boolean");

        return "boolean";
    }


    /**
        * f0 -> PrimaryExpression()
        * f1 -> "<"
        * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(CompareExpression n, String argu) throws Exception 
    {
        
        String type1 = n.f0.accept(this, argu);
        String type2 = n.f2.accept(this, argu);

        if (!is_int(type1) || !is_int(type2))
            throw new Exception("Comparison is only done with inetger exressions");

        return "boolean";
    }


    /**
        * f0 -> PrimaryExpression()
        * f1 -> "+"
        * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(PlusExpression n, String argu) throws Exception 
    {
        
        String type1 = n.f0.accept(this, argu);
        String type2 = n.f2.accept(this, argu);



        if (!is_int(type1) || !is_int(type2))
            throw new Exception("Addition is only done with inetger exressions");

        return "int";
    }


    /**
        * f0 -> PrimaryExpression()
        * f1 -> "-"
        * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(MinusExpression n, String argu) throws Exception 
    {
        String type1 = n.f0.accept(this, argu);
        String type2 = n.f2.accept(this, argu);

        if (!is_int(type1) || !is_int(type2))
            throw new Exception("Substraction is only done with inetger exressions");

        return "int";
    }


    /**
        * f0 -> PrimaryExpression()
        * f1 -> "*"
        * f2 -> PrimaryExpression()
    */
    @Override
    public String visit(TimesExpression n, String argu) throws Exception 
    {
        
        String type1 = n.f0.accept(this, argu);
        String type2 = n.f2.accept(this, argu);

        if (!is_int(type1) || !is_int(type2))
            throw new Exception("Multiplication is only done with inetger exressions");

        return "int";
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
        String type1 = n.f0.accept(this, argu);
        String type2 = n.f2.accept(this, argu);


        if (!is_array(type1) || !is_int(type2))
            throw new Exception("Accessing non array type or index is not an integer");


        return get_array_type(type1);
    }


    /**
        * f0 -> PrimaryExpression()
        * f1 -> "."
        * f2 -> "length"
    */
    @Override
    public String visit(ArrayLength n, String argu) throws Exception 
    {
        
        String type = n.f0.accept(this, argu);

        if (!is_array(type))
            throw new Exception("Cannot take length of non array type");

        return "int";
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
        String type = n.f0.accept(this, argu);
        
        String fun = n.f2.accept(this, argu);
        
        String args = n.f4.present() ? n.f4.accept(this, argu) : "";        

        Function f = st.get_function(type, fun);

        if (f == null)
            throw new Exception("Function " + fun + " doesn't exist on the scope of clas " + type);

        if (!compatable_types(args, f.get_arguments_types()))
            throw new Exception("Calling function with mismached arguments\n" + args + "\n");

        return f.type();
    }



    /**
        * f0 -> Expression()
        * f1 -> ExpressionTail()
    */
    @Override
    public String visit(ExpressionList n, String argu) throws Exception 
    {
        String ret = n.f0.accept(this, argu);

        if (n.f1 != null) 
            ret += n.f1.accept(this, argu);

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
        * f0 -> ( ExpressionTerm() )*
    */
    @Override
    public String visit(ExpressionTail n, String argu) throws Exception 
    {
        String ret = "";
        
        for (Node node : n.f0.nodes) 
            ret += "," + node.accept(this, argu);
        
        return ret;
    }


    
}