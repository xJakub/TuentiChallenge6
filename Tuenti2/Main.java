import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
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
		FileReader frCorpus = new FileReader("corpus.txt");
		Scanner scCorpus = new Scanner(frCorpus);
		
		int a = sc.nextInt();
		int b = sc.nextInt();
		
		HashMap<String, Integer> map = new HashMap<>();
		
		// store all the frequencies in a hashmap
		for (int pos=1; pos<=b; pos++) {
			String word = scCorpus.next();
			if (pos < a) continue;
			
			if (!map.containsKey(word)) {
				map.put(word, 1);
			} else {
				Integer i = map.get(word);
				map.put(word, i+1);
			}
		}
		
		// Iterate 3 times the hashmap's entries, showing the most frequent word
		// its frequency. maxValue is updated on each iteration to forbid repetition.
		int maxValue = Integer.MAX_VALUE;
		for (int i=0; i<3; i++) {
			if (i != 0) {
				write(",");
			}
			
			int bestValue = 0;
			String bestWord = null;
			
			for (Entry<String, Integer> entry : map.entrySet()) {
				if (entry.getValue() > bestValue && entry.getValue() <= maxValue) {
					bestWord = entry.getKey();
					bestValue = entry.getValue();
				}
			}
			
			write(bestWord + " " + bestValue);
			maxValue = bestValue-1;
		}
		
		scCorpus.close();
		frCorpus.close();
	}
}
