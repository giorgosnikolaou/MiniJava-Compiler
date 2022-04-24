class InheritanceTest {
    public static void main(String[] a){
        int n;
        A A;
        A = new B();
        n = A.foo(2);
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

class B extends A
{
    int j;
    public int foo(int i)
    {
        return i + j;
    }
}
