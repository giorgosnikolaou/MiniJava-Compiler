class Main {
    public static void main(String[] args) {
        int a;
        boolean b;
        int[] c;
        boolean[] d;
        
        a = (((1 - 2) + 3) - 4) * 2;
        System.out.println(a);

        b = true;
        c = new int[10];
        d = new boolean[a+10];

        c[1] = 6;
        c[a+11] = c[1];
        c[(c[(a+11)])] = 101;
        System.out.println((c[1]));
        System.out.println((c[6]));
        System.out.println((c[7]));

    }
}
