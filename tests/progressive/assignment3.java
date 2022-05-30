class Main {
    public static void main(String[] args) {
        int a;
        boolean b;
        int[] c;
        boolean[] d;
        A e;
        a = (((1 - 2) + 3) - 4) * 2;
        System.out.println(a);

        b = true;
        c = new int[10];
        e = new A();
        d = new boolean[a];
    }
}

class A
{
    int a;
    int c;

    public int[] foo(boolean[] a)
    {
        int[] b;
        b = new int[(a.length)];
        return b;
    }
}