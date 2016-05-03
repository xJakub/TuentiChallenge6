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
		int n = sc.nextInt();
		int m = sc.nextInt();

		// Our matrix will have double the rows
		// and columns, to allow rectangles that
		// occupy multiple tiles.
		int matrix[][] = new int[n*2][m*2];

		for (int i=0; i<n; i++) {
			String str = sc.next();

			for (int j=0; j<str.length(); j++) {
				char c = str.charAt(j);
				if (c == '.') {
					matrix[i][j] = 0;
				} else if (c >= 'a') {
					matrix[i][j] = -(c-'a' + 1);
				} else {
					matrix[i][j] = c - 'A' + 1; 
				}
				matrix[i][j+m] = matrix[i][j];
				matrix[i+n][j] = matrix[i][j];
				matrix[i+n][j+m] = matrix[i][j];
			}
		}

		int best = 0;


		// If the sum of all elements in a row
		// or a column is greater than 0, then
		// we will output INFINITY.
		for (int i=0; i<n; i++) {
			int sum = 0;
			for (int j=0; j<m; j++) {
				sum += matrix[i][j];
			}
			if (sum > 0) { best = Integer.MAX_VALUE; }
		}

		for (int j=0; j<m; j++) {
			int sum = 0;
			for (int i=0; i<n; i++) {
				sum += matrix[i][j];
			}
			if (sum > 0) { best = Integer.MAX_VALUE; }
		}


		// We try every top-bottom combination of rows,
		// store their sums into temp[] and then use
		// Kadane's algorithm for finding the best
		// rectangle within these rows.
		if (best < Integer.MAX_VALUE) {
			for (int top=0; top<n; top++) {
				int temp[] = new int[m*2];
				
				for (int bottom=top; bottom<matrix.length; bottom++) {

					// if the best rectangle has n or more columns,
					// then the result is INFINITY, so no need to check
					// it inside this loop.
					if (bottom-top+1 >= n) {
						continue;
					}

					for (int x=0; x<temp.length; x++) {
						temp[x] += matrix[bottom][x];
					}

					// Kadane's algorithm for finding the maximum sum subarray
					int max_to_x = 0;

					int max = 0;

					for (int x=0; x<temp.length; x++) {
						max_to_x += temp[x];

						if (max_to_x < 0) {
							max_to_x = 0;
						}

						if (max_to_x > max) {
							max = max_to_x;
						}
					}

					if (max > best) {
						best = max;
					}
				}
			}
		}

		if (best == Integer.MAX_VALUE) {
			write("INFINITY");
		} else {
			write(best + "");
		}
	}
}
