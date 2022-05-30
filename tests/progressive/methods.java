class Main {
    public static void main(String[] args) {
        A e;
        int a;

        e = new A();
        
        if (e.bar((new int[10]), (new boolean[20])))
            System.out.println(e.foo(10, e));
        else
            System.out.println(0);
        
        System.out.println(e.get_a());
        System.out.println(e.get_b());

        if (e.get_bool())
            System.out.println(1);
        else
            System.out.println(0);
            

        

    }
  }

class A
{
    int a;
    int b;
    boolean bool;
    
    public int foo(int c, A d)
    {
        a = c;
        bool = d.bar((new int[a]), (new boolean[20]));
        return b;
    }
    
    public boolean bar(int[] a, boolean[] c)
    {
        b = (a.length )+ (c.length);
        return true;
    }

    public int get_a()
    {
        return a;
    }

    public int get_b()
    {
        return b;
    }

    public boolean get_bool()
    {
        return bool;
    }
} 