import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeMap;

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

	
	// counters
	private static int variablesCount;
	private static int ledgesCount;
	
	// tables indicating if a bridge of certain toughness can be
	// crossed a certain amount of times
	private static boolean[] toughnessIsOdd;
	private static boolean[] toughnessIsEven;
	
	// map indicating the adjacent ledges of a ledge
	private static TreeMap<Integer, TreeMap<Integer, Boolean>> adjacentLedges;
	
	// coefficient of a ledge / degree of the node
	private static TreeMap<Integer, Boolean> ledgeCoefficients;
	
	// ledge variables. If a bridge can be crossed both an odd and an even number of times,
	// then we will represent it as a variable in a linear equation system modulo 2
	private static TreeMap<Integer, ArrayList<Integer>> ledgeVariables;
	
	// map for ledge'sinputId -> index
	private static TreeMap<Integer, Integer> ledgeIds;

	// The function called for every case
	public static void processCase(int caseNumber) throws Exception {
		// initialize tables
		adjacentLedges = new TreeMap<>();
		ledgeCoefficients = new TreeMap<>();
		ledgeVariables = new TreeMap<>();
		ledgeIds = new TreeMap<>();
		ledgesCount = 0;
		variablesCount = 0;
		boolean ok = true;

		// read the stepforces
		int s = sc.nextInt();
		int stepForces[] = new int[s];

		for (int i=0; i<s; i++) {
			stepForces[i] = sc.nextInt();
		}

		// we only want to know if a toughness' bridges can be crossed an odd
		// or even amount of times
		createToughnessTables(stepForces);

		// read the bridges
		int b = sc.nextInt();

		for (int i=0; i<b; i++) {
			int fromOrig = sc.nextInt();
			int toOrig = sc.nextInt();
			int toughness = sc.nextInt();

			int from = getLedgeId(fromOrig);
			int to = getLedgeId(toOrig);

			boolean isOdd = toughnessIsOdd[toughness];
			boolean isEven = toughnessIsEven[toughness];

			if (!isOdd && !isEven) {
				// If we can't apply the exact amount of stepforce to a bridge,
				// then the level is impossible
				ok = false;
			} else if (!isEven) {
				// If we can only cross the bridge an odd number of times,
				// we change the degree of the node/ledge
				ledgeCoefficients.put(from, !ledgeCoefficients.get(from));
				ledgeCoefficients.put(to, !ledgeCoefficients.get(to));
			} else if (isOdd && isEven) {
				// Odd and even: new variable for the linear system
				ArrayList<Integer> list1 = ledgeVariables.get(from);
				list1.add(variablesCount);
				ArrayList<Integer> list2 = ledgeVariables.get(to);
				list2.add(variablesCount);
				variablesCount++;
			}

			// Mark the nodes as adjacent
			adjacentLedges.get(from).put(to, true);
			adjacentLedges.get(to).put(from, true);
		}


		if (ok) {
			// Create the adjacency tables
			boolean[][] adjacentTable = new boolean[ledgesCount][ledgesCount];

			for (int i=0; i<ledgesCount; i++) {
				TreeMap<Integer, Boolean> adjacentMap = adjacentLedges.get(i); 
				for (int to : adjacentMap.keySet()) {
					adjacentTable[i][to] = true;
				}
			}

			// Check if the ledges are connected
			// If not, then this level is impossible
			ok = graphIsConnected(adjacentTable);
		}

		if (!ok) {
			write("GOODBYE CRUEL WORLD");
			return;
		}
		
		// Check for solutions
		trySolutions();
	}

	
	// Gets the internal id of a ledge, based on the id we get from the input
	// If we haven't seen this ledge before, we initialize its lists
	private static int getLedgeId(int origId) {
		if (!ledgeIds.containsKey(origId)) {
			ledgeVariables.put(ledgesCount, new ArrayList<>());
			adjacentLedges.put(ledgesCount, new TreeMap<>());
			ledgeCoefficients.put(ledgesCount, false);
			ledgeIds.put(origId, ledgesCount++);
		}
		return ledgeIds.get(origId);
	}

	
	// Constructs the equation system's matrix and coefficients array 
	private static void trySolutions() throws IOException {

		boolean[][] equations = new boolean[ledgesCount][variablesCount];
		boolean[] coefficients = new boolean[ledgesCount];

		for (int i=0; i<ledgesCount; i++) {
			// A row is true when a ledge is connected to a variable's bridge
			ArrayList<Integer> variablesList = ledgeVariables.get(i);
			for (int variable : variablesList) {
				equations[i][variable] = true;
			}
			coefficients[i] = ledgeCoefficients.get(i);
		}
		
		// Apply Gaussian elimination to the system
		GaussianElimination ge = new GaussianElimination(equations, coefficients);

		// Loop the original node ids in ascending order
		// (Since we use a TreeSet, they are sorted automatically)
		for (int startOrig : ledgeIds.keySet()) {
			int start = ledgeIds.get(startOrig);

			for (int endOrig : ledgeIds.keySet()) {
				if (endOrig < startOrig) continue;

				int end = ledgeIds.get(endOrig);

				// Try the solution
				if (ge.isSolution(start, end)) {
					write("I TOLD YOU SO from " + startOrig + " to " + endOrig);	
					return;
				}
			}
		}
		
		// No solution
		write("GOODBYE CRUEL WORLD");
	}
	
	
	// Checks if all the nodes from the graph / ledges from the level
	// are connected.
	private static boolean graphIsConnected(boolean[][] adjacentLedges) {

		boolean connected = true;
		boolean connectedTo[] = new boolean[adjacentLedges.length];

		Stack<Integer> stack = new Stack<>();
		
		// Start from ledge 0 
		connectedTo[0] = true;
		stack.add(0);

		while(!stack.isEmpty()) {
			int ledge = stack.pop();
			for (int i=0; i<adjacentLedges.length; i++) {
				// Mark the adjacent nodes as connected to the first
				if (!connectedTo[i] && adjacentLedges[i][ledge]) {
					connectedTo[i] = true;
					stack.add(i);
				}
			}
		}

		// If all the ledges are connected, we will have visited them all
		for (int i=0; i<adjacentLedges.length; i++) {
			if (!connectedTo[i]) {
				connected = false;
				break;
			}
		}
		return connected;
	}


	public static void createToughnessTables(int stepForces[]) {
		//
		// possible[i][s] <-> possible to do s stepforce in i moves  
		//
		toughnessIsOdd = new boolean[1001]; // Maximum stepforce is 1000
		toughnessIsEven = new boolean[1001];
		boolean possible[][] = new boolean[1001][1001];
		possible[0][0] = true;

		// Create the table using dynamic programming
		for (int i=1; i<=1000; i++) {
			for (int s=0; s<stepForces.length; s++) {
				int stepForce = stepForces[s];
				for (int t=stepForce; t<=1000; t+=1) {
					if (possible[i-1][t-stepForce]) {
						possible[i][t] = true;
						// Indicate if we can destroy the bridge
						// with an even or odd amount of crosses
						if (i % 2 == 0) {
							toughnessIsEven[t] = true;
						} else {
							toughnessIsOdd[t] = true;
						}
					}
				}
			}
		}
	}
}
