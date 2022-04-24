package SymbolTable;
import java.util.*;


public class Class {
    // Class name
    private final String name;
    // Superclass name, null if class doesn't inherit
    private final String super_name;
    
    private int offset_var = 0;
    private int offset_func = 0;

    // name, info
    private Map<String, Function> functions = new LinkedHashMap<String, Function>();

    // name, info
    private Map<String, Variable> variables = new LinkedHashMap<String, Variable>();

    Class(String _class)
    {
        name = _class;
        super_name = null; 
    }

    Class(String _class, Class _super)
    {
        name = _class;
        super_name = _super.name; 
        offset_var = _super.offset_var;
        offset_func = _super.offset_func;
        // System.err.println(_class + " " + offset_var + " " + offset_func + "\n");
    }

    public String name()
    {
        return name;
    }

    public String superclass()
    {
        return super_name;
    }

    public void add_function(String _name, String _type) throws Exception 
    {
        if (functions.containsKey(_name))
            throw new Exception("Duplicate function name: " + _name);
        
        functions.put(_name, new Function(_name, _type));
    }

    public void add_function(String _name, String _type, boolean overwriting) throws Exception 
    {
        if (overwriting == true)
        {
            add_function(_name, _type);
            return ;
        }

        if (functions.containsKey(_name))
            throw new Exception("Duplicate function name: " + _name);
        
        
        functions.put(_name, new Function(_name, _type, offset_func));
        offset_func += 8;
    }

    public Function get_function(String _name)
    {
        return functions.containsKey(_name) ? functions.get(_name) : null; 
    }

    public void add_variable(String _name, String _type) throws Exception 
    {
        if (variables.containsKey(_name))
            throw new Exception("Duplicate variable name: " + _name);

        variables.put(_name, new Variable(_name, _type, offset_var));
        offset_var += _type.equals("int") ? 4 : _type.equals("boolean") ? 1 : 8;
    }

    public Variable get_variable(String _name)
    {
        return variables.containsKey(_name) ? variables.get(_name) : null; 
    }

    public void print()
    {
        System.out.println(super_name != null ? " extends " + super_name : "");
        
        for (Map.Entry<String, Function> entry : functions.entrySet())
            entry.getValue().print("\t");
        

        System.out.println();
        for (Map.Entry<String, Variable> entry : variables.entrySet())
            entry.getValue().print("\t");

    }

    public void print_offsets(Class _super)
    {

        for (Map.Entry<String, Variable> entry : variables.entrySet())
        {
            Variable var = entry.getValue();

            int offset = var.offset();

            if (offset != -1)
                System.out.println(name + "." + var.name() + " : " + offset);
        }

        for (Map.Entry<String, Function> entry : functions.entrySet())
        {
            Function func = entry.getValue();

            int offset = func.offset();

            if (offset != -1)
                System.out.println(name + "." + func.name() + " : " + offset);
        }
        



    }

}
