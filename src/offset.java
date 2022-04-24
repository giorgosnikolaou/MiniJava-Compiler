class Main
{
    public static void main(String[] args)
    {

    }
}

class A{
    int i;
    boolean flag;
    int j;
    public int foo() {return 1; }
    public boolean fa() {return true;}
}

class B extends A{
    A type;
    int k;
    public int foo() {return 2;}
    public boolean bla() {return false;}
}

class D extends A{
    A type;
    int k;
    public int foo1() {return 2;}
    public boolean bla() {return false;}
}

class C extends D{
    A type;
    int k;
    public int foo() {return 2;}
    public boolean bla2() {return false;}
}