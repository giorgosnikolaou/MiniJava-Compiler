class Main {
    public static void main(String[] a){
        A b;
        b = new A();
        if (b.foo())
            System.out.println(1);
        else
            System.out.println(0);

    }
}

class A
{

    public boolean foo()
    {
        return true;
    }

    public boolean bar()
    {
        return false;
    }

    public boolean foo2(A a)
    {
        boolean d;
        if (a.bar())
            d = false;
        else 
            d = true;
        return d;
    }
}
