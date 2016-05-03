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
		
		// We store the combos' and breaks' moves
		// We ignore the large ones, since their last moves
		// represent another combo.
		String combos[] = new String[]{
				// "L-LD-D-RD-R-P",
				"-D-RD-R-P-",
				"-R-D-RD-P-",
				"-D-LD-L-K-",
				// "R-RD-D-LD-L-K"
		};
		String breaks[] = new String[]{
				// "L-LD-D-RD-R-P",
				"-D-RD-R-",
				"-R-D-RD-",
				"-D-LD-L-",
				// "R-RD-D-LD-L-K"
		};

		// We append and prepend '-' to the moves list, so
		// there is a - before and after every move
		String moves = "-" + sc.next() + "-";
		
		String last10 = "";
		int length = moves.length();
		int offset = 0;
		int breaksCount = 0;
		
		// Will be true whenever we could have made a combo
		// If it was true but we didn't make a combo, then
		// we increment the breaks count.
		boolean hasComboPossibility = false;

		while(offset < length) {
			String move = moves.substring(offset, offset+2);
			offset += 2;

			if (move.charAt(1) != '-') {
				move += '-';
				offset++;
			}
			
			last10 += move;

			int currentLength = last10.length();
			if (currentLength > 10) {
				last10 = last10.substring(currentLength-10);
				currentLength = 10;
			}

			boolean hasCombo = false;

			for (String s : combos) {
				if (s.equals(last10)) {
					hasCombo = true;
					break;
				}
			}
			
			if (!hasCombo && hasComboPossibility) {
				breaksCount++;
			}
			hasComboPossibility = false;

			// every combo possibility is 8 characters long,
			// so we just check the last 8 characters
			if (currentLength >= 8) {
				for (String s : breaks) {
					if (s.equals(last10.substring(currentLength - 8))) {
						hasComboPossibility = true;
						break;
					}
					
				}
			}
		}
		
		
		if (hasComboPossibility) {
			breaksCount++;
		}
		
		write(breaksCount + "");
	}
}
