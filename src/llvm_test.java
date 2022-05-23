class llvm_test{
    public static void main(String[] args){
        
    }
}



class A
{
    int x;
    int[] a;

    public int foo()
    {
        return 0;
    }

    public int[] bar()
    {
        return a;
    }

}

class B extends A
{
    int y;
    public int foo()
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
    public int tez()
    {
        return 1;
    }
}