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
		// The first table allows up to 4 diners
		// every new table we join increments by two
		// this limit

		// So:
		// - If we have no people, the answer is 0 
		// - If we have 1 to 4 people, the answer is 1
		// - If we have more than 4 people, the answer is
		//   1 + ceil((n-4)/2) = 1 + (n-3)/2 = (n-1)/2
		
		int n = sc.nextInt();
		
		if (n == 0) {
			write("0");
		} else if (n <= 4) {
			write("1");
		} else {
			int s = (n-1)/2;
			write(s + "");
		}
	}
}
