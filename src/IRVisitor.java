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

    public IRVisitor(SymbolTable st) 
    {
		this.st = st;
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
            emit("@." + _class.name() + "_vtable = global [" + _class.vtable.size() + " x i8*] [");
            emit_vtable_functions(_class);
            emit("]\n");
        }
        emit("\n");
    }

    /**
        * f0 -> MainClass()
        * f1 -> ( TypeDeclaration() )*
        * f2 -> <EOF>
    */
    public String visit(Goal n, String argu) throws Exception 
    {
        emit("%_BooleanArray = type { i32, i8* }\n");
        emit("%_IntegerArray = type { i32, i8* }\n\n");

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

        n.f14.accept(this, class_name + st.class_delimiter + "main");
        n.f15.accept(this, class_name + st.class_delimiter + "main");
        
        emit("\n\tret i32 0\n}\n");

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
    // @Override
    // public String visit(ClassDeclaration n, String argu) throws Exception 
    // {
    //     String class_name = n.f1.accept(this, argu);

    //     n.f3.accept(this, class_name);
    //     n.f4.accept(this, class_name);

    //     return null;
    // }

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
    // @Override
    // public String visit(ClassExtendsDeclaration n, String argu) throws Exception 
    // {
    //     String class_name = n.f1.accept(this, argu);

    //     n.f5.accept(this, class_name);
    //     n.f6.accept(this, class_name);

    //     return null;
    // }

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
    // @Override
    // public String visit(MethodDeclaration n, String argu) throws Exception 
    // {
    //     String name = n.f2.accept(this, argu);

    //     Class _class = st.get_class(argu);
        
    //     Function new_function = _class.get_function(name);
    //     Function inherited = st.get_function(_class.superclass(), name);

    //     if (inherited != null)
    //     {
    //         String fun_args = new_function.get_arguments();
    //         String inh_args = inherited.get_arguments();

    //         if (inherited.type() != new_function.type() ||
    //             !fun_args.equals(inh_args))
    //             throw new Exception("Overloading function is not allowed");
           
    //     }

    //     n.f7.accept(this, argu + st.class_delimiter + name);
    //     n.f8.accept(this, argu + st.class_delimiter + name);

    //     String ret_type = n.f10.accept(this, argu + st.class_delimiter + name);
        

    //     return null;
    // }


    /**
        * f0 -> Type()
        * f1 -> Identifier()
        * f2 -> ";"
    */
    // @Override
    // public String visit(VarDeclaration n, String argu) throws Exception 
    // {
    //     String type = n.f0.accept(this, argu);
        

    //     return null;
    // }

}

