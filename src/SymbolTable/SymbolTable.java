package SymbolTable;
import java.util.*;

public class SymbolTable {

    public final String class_delimiter = "::";
    public Map<String, Class> classes = new LinkedHashMap<String, Class>();
    private Class main_class;

    public void add_class(String _class, String _super) throws Exception 
    {
        if (classes.containsKey(_class))
            throw new Exception("Duplicate class name: " + _class);
        
        if (_super != null && !classes.containsKey(_super))
            throw new Exception("Class " + _super + " does not exists, hence cannot inherit from it");
        
        classes.put(_class, _super == null ? new Class(_class) : new Class(_class, get_class(_super)));

    }

    public void add_class(String _class) throws Exception 
    {
        if (classes.containsKey(_class))
            throw new Exception("Duplicate class name: " + _class);

        main_class = new Class(_class);
        classes.put(_class, main_class);
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

    // Checks if _class is a superclass of _subclass
    // Returns false if _class == _subclass 
    public boolean is_subclass(String _class, String _subclass)
    {
        if (_subclass == null)
            return false;

        Class info = classes.get(_subclass);
        String super_name = info.superclass();
        return _class.equals(super_name) ? true : is_subclass(_class, super_name);

    }


    public Function get_function(String class_name, String name)
    {
        if (class_name == null)
            return null;
        
        Class _class = classes.get(class_name);
        Function _ret = _class.get_function(name);

        return  _ret == null ? get_function(_class.superclass(), name) : _ret; 
    }
    
    
    // Finds variable in scope 'scope' (recursively for inheritence and function of class) and returns the type of variable with name 'name' 
    // If variable doesn't exists within the scope throws Exception
    public String resolve_var_type(String name, String scope) throws Exception
    {
        if (scope == null)
            throw new Exception("Couldn't resolve " + name);
        

        String[] terms = scope.split(class_delimiter);

        Class _class = get_class(terms[0]);

        if (terms.length == 2)
        {
            Function fun = _class.get_function(terms[1]);

            Variable var = fun.get_variable(name);

            if (var != null)
                return var.type();
        }

        Variable var = _class.get_variable(name);

        return var != null ? var.type() : resolve_var_type(name, _class.superclass());
    }


    // Returns the class which '_class' extends
    // Throws Exception if '_class' doesn't exists 
    public String superclass(String _class) throws Exception
    {
        Class cl = get_class(_class);

        if (cl == null)
            throw new Exception("Class " + _class + " hasn't been declared");
        
        return cl.superclass();
    }
    

    public void print_offsets()
    {
        for (Map.Entry<String, Class> entry : classes.entrySet()) 
        {
            Class _class = entry.getValue(); 
            
            if (_class == main_class)
                continue;
            
            System.out.println("-----------Class " + _class.name() + "-----------");
            
            _class.print_offsets(get_class(_class.superclass()));

            System.out.println();
        }
    }

}
