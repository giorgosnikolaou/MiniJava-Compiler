class BrackeExpressionTest{
    public static void main(String[] a){
        int n;
        n = (new A()).foo(10, 15);
        System.out.println(n);
        n = (1+ 23);
        System.out.println(n);
    }
}
class A
{
	public int foo(int i, int j)
	{
		return i + j;
	}
}