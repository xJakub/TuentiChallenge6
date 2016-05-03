import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public class Main {

	static FileReader fr;
	static Scanner sc;
	static FileWriter fw;

	public static void openFiles(String inFile, String outFile) throws IOException {	
		fr = new FileReader(inFile);
		fw = new FileWriter(outFile);
		sc = new Scanner(fr);
		sc.useLocale(Locale.US);
	}
	public static void closeFiles() throws IOException {
		sc.close();
		fw.close();
		fr.close();
	}
	public static void writeLine(String line) throws IOException {
		write(line + "\n");
	}
	public static void write(String str) throws IOException {
		System.out.print(str);
		fw.write(str);
	}


	public static void main(String[] args) throws Exception {
		openFiles("testInput.sql", "output.txt");

		int t = sc.nextInt();
		sc.nextLine();

		// launch processCase(t) for every test case
		for (int i=1; i<=t; i++) {
			write("Case #"+i+": ");
			processCase(t);
			writeLine("");
		}

		closeFiles();
	}


	public static void processCase(int caseNumber) throws Exception {
		long n = sc.nextLong();
		long n2 = n;
		long fact5 = 0;
		long fact2 = 0;

		// the factors 2 and 5 are satisfied using zeros
		while(n2 % 2 == 0) {
			fact2++;
			n2 /= 2;
		}
		while(n2 % 5 == 0) {
			fact5++;
			n2 /= 5;
		}
		long zeros = Math.max(fact2, fact5);

		// the rest of the factors are satisfied using ones
		long ones = 1;
		long mod = 1 % n2;
		
		// add ones until the remainder is 0
		// we work on modulo n to drastically
		// speed up the calculation
		while(mod != 0) {
			mod = (10*mod + 1) % n2;
			ones++;
		}
		
		write(ones + " " + zeros);
	}
}
