public class ZTest {
	public static void main(String[] args) {
		int c=0;
		for(int i=3; i<30; i++){
			c+= i-2;
			System.out.println(i + "\t: " + c);
			
		}
		
		// Long-Check
		long l = 3;
		System.out.println(l + " / " + ((int) l));
	}
}