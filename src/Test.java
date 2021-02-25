public class Test
{

	public static void main(String[] args)
	{
		Encoder testEncoder = new Encoder("aaaaaaaaaaaabeeeeeeeoooyyyyy");
		System.out.println("\n" + testEncoder.getEncodingTable());
		System.out.println(testEncoder.getBitString());
	}
}
