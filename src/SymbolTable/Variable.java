package SymbolTable;

// Represents a variable named 'name' and of type 'type'
// 'offset' has the value of the offset as per the assignment 

public class Variable {
    private final String name;
    private final String type;
    private final int offset;

    Variable(String _name, String _type)
    {
        name = _name;
        type = _type;
        offset = -1;
    }
    
    Variable(String _name, String _type, int _offset)
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

    // Debugging purposes
    public void print()
    {
        print("");
    }
    
    // Debugging purposes
    public void print(String pref)
    {
        System.out.println(pref + "V: " + type + " " + name);
    }

}
