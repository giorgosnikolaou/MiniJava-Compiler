class Main {
    public static void main(String[] args) {
        int[] a;
        int i;
        a = new int[10];
        i = 0;

        while (i < (a.length))
        {
            a[i] = i;
            i = i + 1;
        }
        
        i = 0;
        while (i < (a.length))
        {
            System.out.println(a[i]);
            i = i + 1;
        }
        
        a[2] = 10;
        a[3] = 5;
        
        if (((a[2]) < 1) && ((a[3]) < 1))
            System.out.println(1);
        else
            System.out.println(a[2]);

    }
  }