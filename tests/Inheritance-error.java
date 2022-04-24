class InheritanceError {
    public static void main(String[] a){
        int n;
        n = 1;
    }
}

class B extends A
{
    int j;
    public int foo(int i)
    {
        return i + j;
    }
}

class A
{
    int i;

    public int foo(int i)
    {
        return i + 5;
    }
}

