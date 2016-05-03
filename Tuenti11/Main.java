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
		openFiles("submitInput.sql", "output.txt");

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
		long m = sc.nextLong();
		long k = sc.nextLong();

		long remaining = k - n*m;
		
		if (remaining < 0 || remaining % m != 0) {
			// if we have more toasts than our desired number
			// or we want a number not divisible by m,
			// there is no possible solution
			write("IMPOSSIBLE");
			
		} else {
			
			long max = m;
			long seconds = 0;
			
			// we work with only one pile
			// if it won't be needed anymore, we duplicate its size
			while(remaining != 0) {
				remaining -= max;
				if (remaining % (max*2) == 0) {
					max *= 2;
				}
				seconds++;
			}
			
			write(seconds+"");
		}
	}
}
