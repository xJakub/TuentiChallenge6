import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.PriorityQueue;
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

	// Our POPs
	private static POP[] POPs;
	// The total stars granted by our users
	private static int totalStars;
	// A map indicating the expected stars for each call
	private static HashMap<Call, Integer> callStars;

	public static void main(String[] args) throws Exception {
		// Try multiple times, until getting the desired score
		while (processInput() < 34972);
	}
	
	// process the input
	public static int processInput() throws IOException {
		openFiles("testInput.sql", "output.txt");

		// get the number of POPs and calls
		int POPCount = sc.nextInt();
		int callsCount = sc.nextInt();
		POPs = new POP[POPCount];
		callStars = new HashMap<>();
		totalStars = 0;

		/*
		 * The queue will automatically order its elements by stars/duration,
		 * since we are interested in getting as much stars/second as possible.
		 */
		PriorityQueue<Call> queue = new PriorityQueue<>(new Comparator<Call>() {
			@Override
			public int compare(Call c1, Call c2) {
				double r1 = callStars.get(c1)*1.0/c1.getDuration();
				double r2 = callStars.get(c2)*1.0/c2.getDuration();
				int result = Double.compare(r2, r1);
				if (result == 0) {
					// In case of tie, we can just use a random tiebreaker
					// This might give us a better score than before,
					// so we just run the program multiple times
					// until getting a score that satisfies us.
					result = Math.random() > 0.5 ? 1 : -1;
				}
				return result;
			}
		});

		// read all POPs
		for (int i=0; i<POPCount; i++) {
			int x = sc.nextInt();
			int y = sc.nextInt();
			int capacity = sc.nextInt();
			POP pop = new POP(i, x, y, capacity);
			POPs[i] = pop;
		}

		// and add all calls to the first queue
		for (int i=0; i<callsCount; i++) {
			int x = sc.nextInt();
			int y = sc.nextInt();
			int time = sc.nextInt();
			int duration = sc.nextInt();
			Call call = new Call(i, x, y, time, duration);
			updateStars(call);
			queue.add(call);
		}

		// First queue
		while(!queue.isEmpty()) {
			Call call = queue.poll();
			POP pop = getBestPOP(call);
			int oldStars = callStars.get(call);

			// If we don't have a valid POP anymore, then we can't get
			// any star from this call, even if we delay it
			if (pop != null) {
				int delay = pop.getStoreDelay(call);
				int stars = pop.getCallStars(call);
				stars -= (delay+9)/10;
				if (stars == oldStars) {
					// If we get as many stars as we expected, then store the call
					if (stars > 0) {
						storeCall(call, pop);
					}
				} else {
					// ... otherwise, update the expected stars and readd the call
					// to the queue
					updateStars(call);
					queue.add(call);
				}
			}
		}

		// Show statistics about the stars/calls for every POP (just informative)
		for (POP pop : POPs) {
			System.out.println("POP#"+pop.getId()+" -> "+pop.getTotalStars() +
					" stars, " + pop.getCallsCount() + " calls");
		}

		// Show the total amount of stars we got
		System.out.println("" + totalStars + " total stars.");
		closeFiles();
		
		return totalStars;
	}

	// Store a call in a POP
	private static void storeCall(Call call, POP pop) throws IOException {
		int stars = pop.getCallStars(call);
		int delay = pop.storeCall(call);
		if (delay == -1) { return; }
		stars -= (delay + 9)/10;

		// update the total stars and write the data to the output,
		// since we will need to upload it later
		totalStars += stars;
		writeLine(call.getId() + " " + pop.getId() + " " + (call.getTime()+delay));
	}

	// update the expected stars for a call
	private static void updateStars(Call call) {
		POP pop = getBestPOP(call);

		if (pop != null) {
			int stars = pop.getCallStars(call);
			stars -= (pop.getStoreDelay(call)+9)/10;
			callStars.put(call, stars);
		} else {
			// if no POP, we expect no stars
			callStars.put(call, 0);	
		}
	}

	// Get the best POP (the one that gives the most stars) for a call
	private static POP getBestPOP(Call call) {
		// We start with no POP and no stars
		POP best = null;
		int bestStars = 0;

		for (POP pop : POPs) {
			// If we are more than 50m away, then we won't get any stars,
			// so we don't make any other calculations
			if (pop.getCallDistance(call) < 50) {
				int stars = pop.getCallStars(call);
				int delay = pop.getStoreDelay(call);
				if (delay != -1) {
					stars -= (delay+9)/10;
					// If we can store the call in this POP, check
					// if we give more or equal stars than the best POP.
					// (Trial and error shows that >= is better than >)
					if (stars >= bestStars) {
						best = pop;
						bestStars = stars;
					}
				}
			}
		}

		return best;
	}


}
