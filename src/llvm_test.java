class llvm_test{
    public static void main(String[] args){
        int a;
        A b;
        int[] c;
        b = new B();
        
    }
}



class A
{
    int x;
    int[] a;

    public int foo(int y)
    {
        return x + y;
    }

    public int[] bar(A b)
    {
        b = new A();
        return a;
    }

}

class B extends A
{
    int y;
    public int foo(int y)
    {
        return 1;
    }
    public int foo2()
    {
        return y;
    }

}


class C
{
    A c;
    int k;
    boolean kk;
    public int tez(int a , int b, A d)
    {
        return a + b;
    }
    public boolean zet(int a, boolean l)
    {
        int b;
        b =12;
        c = new B(); 
        k = 0;
        k = b;
        k = a;
        
        kk = false;
        kk = l;

        return false;
    }
}