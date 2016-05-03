import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
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
		int n = sc.nextInt();
		int k = sc.nextInt();
		
		// This problem can be also represented as an equals sum partitioning problem
		// We first create an array containing the height change between every pair of km's

		long heights[] = new long[n];
		for (int i=0; i<n; i++) {
			heights[i] = sc.nextInt();
		}

		long heightChanges[] = new long[n-1];
		long totalChanges = 0;

		for (int i=0; i<heightChanges.length; i++) {
			heightChanges[i] = Math.abs(heights[i] - heights[i+1]);
			totalChanges += heightChanges[i];
		}
		
		//
		// We will recursively assign cities to every day
		// We start with the last day and give it as much km's as we can,
		// so we always have the lexicografically-smaller solution
		//
		// Of course, we will first check for the allowed height changes per day
		// and the allowed km's per day.
		//
		long solution[] = new long[k];

		// We start with the smaller possible parameters
		// If there is no solution available, but it is possible to assign
		// another km to a day without changing the maximum height change,
		// we increase kmPerDay. Otherwise, we will increase heightPerDay.
		long heightPerDay = (totalChanges+k-1)/k;
		long kmPerDay = (n-1 + k-1)/k;

		while(true) {
			minNextKm = Integer.MAX_VALUE;
			Arrays.fill(solution, 0);
			
			boolean ok = solveRecursive(k-1, heightChanges.length-1, heightPerDay, kmPerDay,
					totalChanges, heightChanges, solution);

			if (ok) {
				break;
			} else {
				if (minNextKm <= heightPerDay) {
					// there was a possible group with totalHeight < heightPerDay
					kmPerDay++;
				} else {
					heightPerDay++;
					kmPerDay = (n-1 + k-1)/k;
				}
			}
		}
		
		for (int i=0; i<k; i++) {
			if (i != 0) write(" ");
			write(solution[i]+"");
		}
	}
	
	private static long minNextKm;

	private static boolean solveRecursive(int dayOffset, int offset, long heightPerDay, long kmPerDay,
			long heightLeft, long[] heightChanges, long[] solution) {
		
		if (dayOffset == -1 && offset == -1) {
			// No days nor km's left. Perfect!
			return true;
			
		} else if (dayOffset == -1 || offset == -1) {
			// Day without km's or km's without day, invalid.
			return false;

		} else {

			long daysLeft = dayOffset+1;
			
			long minHeightPerDay = (heightLeft + daysLeft-1)/(daysLeft); 

			
			if (minHeightPerDay > heightPerDay) {
				return false;
			}
			
			long dayHeight = 0;

			// try to get as much distance as height changes as possible,
			// while obeying the limits
			while(offset >= dayOffset && solution[dayOffset] < kmPerDay
					&& dayHeight + heightChanges[offset] <= heightPerDay) {
				dayHeight += heightChanges[offset];
				solution[dayOffset]++;
				offset--;
			}
			
			// store the possible minimum total height if kmPerDay was incremented by 1
			if (offset >= dayOffset) {
				minNextKm = Math.min(minNextKm, dayHeight+heightChanges[offset]);
			}

			return solveRecursive(dayOffset-1, offset, heightPerDay, kmPerDay,
					heightLeft - dayHeight, heightChanges, solution);
		}
	}
}
