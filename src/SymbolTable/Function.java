package SymbolTable;
import java.util.*;

public class Function {
    // Function name
    private final String name;
    private final String type;
    private final int offset;

    
    private List<Variable> arguments = new ArrayList<Variable>();

    // name, obj
    private Map<String, Variable> variables = new HashMap<String, Variable>();

    Function(String _name, String _type)
    {
        name = _name;
        type = _type;
        offset = -1;
    }

    Function(String _name, String _type, int _offset)
    {
        name = _name;
        type = _type;
        offset = _offset;
    }

    public String name()
    {
        return name;
    }

    public String type()
    {
        return type;
    }

    public int offset()
    {
        return offset;
    }

    public int args_size()
    {
        return arguments.size();
    }

    private String get_str(Variable var)
    {
        return var.type() + " " + var.name();
    }

    public String get_arguments_types()
    {
        String _ret = arguments.size() >= 1 
                    ? arguments.get(0).type() : "";
        
        for (int i = 1; i < arguments.size(); i++)
            _ret += "," + arguments.get(i).type();
        
        return _ret;
    }

    public String get_arguments()
    {
        String _ret = arguments.size() >= 1 
                    ? get_str(arguments.get(0)) : "";
        
        for (int i = 1; i < arguments.size(); i++)
            _ret += "," + get_str(arguments.get(i));
        
        return _ret;
    }

    public void add_argument(String _name, String _type) throws Exception 
    {
        Variable arg = new Variable(_name, _type);

        if (arguments.contains(arg))
            throw new Exception("Duplicate variable name: " + _name);
        
        arguments.add(arg);
        add_variable(_name, _type);
    }

    public void add_variable(String _name, String _type) throws Exception 
    {
        if (variables.containsKey(_name))
            throw new Exception("Duplicate variable name: " + _name);
        
        variables.put(_name, new Variable(_name, _type));
    }

    public Variable get_variable(String _name)
    {
        return variables.get(_name);
    }

    public void print()
    {
        print("");
    }
    
    public void print(String pref)
    {
        System.out.print(pref + "F: " + type + " " + name + "( ");

        for (Variable arg : arguments)
            System.out.print(arg.type() + " " + arg.name() + " ");

        System.out.println(")");

        for (Map.Entry<String, Variable> entry : variables.entrySet())
            entry.getValue().print("\t\t");

    }

}
