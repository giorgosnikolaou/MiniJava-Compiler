package SymbolTable;
import java.util.*;

public class SymbolTable {

    public final String class_delimiter = "::";
    private Map<String, Class> classes = new HashMap<String, Class>();

    public void add_class(String _class, String _super) throws Exception 
    {
        if (classes.containsKey(_class))
            throw new Exception("Duplicate class name: " + _class);
        
        if (_super != null && !classes.containsKey(_super))
            throw new Exception("Class " + _super + " does not exists, hence cannot inherit from it");
        
        classes.put(_class, new Class(_class, _super));

    }

    // Returns the Class object with name _class or null if one does not exist
    public Class get_class(String _class)
    {
        return classes.containsKey(_class) ? classes.get(_class) : null;
    }

    // Prints symbol table, used for debugging
    public void print()
    {
        for (Map.Entry<String, Class> entry : classes.entrySet())
        {
            System.out.print("Class name: " + entry.getKey());
            entry.getValue().print();
            System.out.println();
        }
    }

    // Checks if a given String is the name of a class that is already inserted
    public boolean is_defined_class(String name)
    {
        return classes.containsKey(name);
    }

    // Checks if cl is a superclass of sub
    // Returns false if cl == sub 
    public boolean is_subclass(String cl, String sub)
    {
        if (sub == null)
            return false;

        Class info = classes.get(sub);
        String super_name = info.superclass();
        return cl.equals(super_name) ? true : is_subclass(cl, super_name);

    }


    public Function get_function(String class_name, String name)
    {
        if (class_name == null)
            return null;
        
        Class cl = classes.get(class_name);
        Function _ret = cl.get_function(name);

        return  _ret == null 
                ? get_function(cl.superclass(), name) : _ret; 
    }
    
    
    public String resolve_type(String name, String scope) throws Exception
    {
        // System.out.println("Searching " + name + " on " + scope);
        if (scope == null)
            throw new Exception("Couldn't resolve " + name);
        

        String[] terms = scope.split(class_delimiter);

        Class cl = get_class(terms[0]);


        if (terms.length == 2)
        {
            Function fun = cl.get_function(terms[1]);

            Variable var = fun.get_variable(name);

            if (var != null)
                return var.type();
        }

        Variable var = cl.get_variable(name);

        return var != null ? var.type() : resolve_type(name, cl.superclass());
    }

    

}
