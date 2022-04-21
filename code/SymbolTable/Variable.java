package SymbolTable;

public class Variable {
    private final String name;
    private final String type;

    Variable(String _name, String _type)
    {
        name = _name;
        type = _type;
    }

    public String name()
    {
        return name;
    }

    public String type()
    {
        return type;
    }

    public void print()
    {
        print("");
    }

    public void print(String pref)
    {
        System.out.println(pref + "V: " + type + " " + name);
    }

}
