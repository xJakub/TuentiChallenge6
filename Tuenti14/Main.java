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
			processCase(i);
			writeLine("");
		}

		closeFiles();
	}

	public static void processCase(int caseNumber) throws Exception {
		int mapHeight = sc.nextInt();
		int mapWidth = sc.nextInt();
		
		SpeedRunner runner = new SpeedRunner(mapWidth, mapHeight);
		
		// get the map data
		for (int y=0; y<mapHeight; y++) {
			String word = sc.next();

			for (int x=0; x<mapWidth; x++) {
				char c = word.charAt(x);
				runner.setCharacter(x, y, c);
			}
		}

		// Search for the best solution. This is divided into two parts:
		// -- 1. Convert the map into an asymmetric TSP problem
		// -- 2. Use the Held-Karp algorithm to obtain the best solution
		runner.playLevel();

		if (!runner.isPossible()) {
			write("IMPOSSIBLE");
		} else {
			// if there is a solution, show the frames count and keys pressed
			write(runner.getFrames() + " " + runner.getMoves());
		}
	}
}
