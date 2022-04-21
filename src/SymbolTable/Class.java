package SymbolTable;
import java.util.*;


public class Class {
    // Class name
    private final String name;
    // Superclass name, null if class doesn't inherit
    private final String super_name;
    
    // name, info
    private Map<String, Function> functions = new HashMap<String, Function>();

    // name, type
    private Map<String, Variable> variables = new HashMap<String, Variable>();

    Class(String _class, String _super)
    {
        name = _class;
        super_name = _super; 
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

    public Function get_function(String _name)
    {
        return functions.containsKey(_name) ? functions.get(_name) : null; 
    }

    public void add_variable(String _name, String _type) throws Exception 
    {
        if (variables.containsKey(_name))
            throw new Exception("Duplicate variable name: " + _name);
        
        variables.put(_name, new Variable(_name, _type));
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

}
