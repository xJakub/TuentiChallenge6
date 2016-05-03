
public class POP {
	// basic data from the POP
	private int x;
	private int y;
	private int capacity;
	private int id;
	
	// table indicating the number of calls active at a certain time,
	// it is used to check if a certain call can be stored
	private int[] occupationTable;
	
	// fields used just for informative purposes
	private int totalStars;
	private int totalCalls;
	private int totalDuration;

	public POP(int id, int x, int y, int capacity) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.capacity = capacity;
		this.totalDuration = 0;
		this.totalStars = 0;
		this.totalCalls = 0;
		
		// T<=30000 and D<=30000, so with 60000 positions we will be fine
		this.occupationTable = new int[60000];
	}

	/**
	 * Gets the Euclidean distance between the caller and the POP
	 * @param call the call
	 * @return the distance
	 */
	public double getCallDistance(Call call) {
		return Math.sqrt(Math.pow(call.getX()-x, 2) + Math.pow(call.getY()-y, 2));
	}

	/**
	 * Stores a call in the POP, minimizing its delay
	 * @param call the call
	 * @return the delay, or -1 if failed to store
	 */
	public int storeCall(Call call) {
		// Calculate the maximum delay based on the stars count
		int time = call.getTime();
		int initialStars = getCallStars(call);
		int maxDelay = (initialStars-1)*10;
		
		// Find an empty block for the call
		int startTime = findEmptyOccupation(time, maxDelay, call.getDuration());
		int stars = initialStars - (startTime - time + 9)/10;
		
		// If we won't get any star for this call, there's no reason to store it
		// (Poor customers)
		if (stars >= 1) {
			fillOccupation(startTime, call.getDuration());
			totalStars += stars;
			totalCalls++;
			totalDuration += call.getDuration();
			
			// Return the delay
			return startTime-time;
		} else {
			return -1;
		}
	}

	/**
	 * Updates the occupation table with a new established call
	 * @param time the time at which we established the call
	 * @param duration the duration of the call
	 */
	private void fillOccupation(int time, int duration) {
		for (int i=0; i<duration; i++) {
			occupationTable[time+i]++;
		}
	}

	/**
	 * Finds an empty block for establishing a call
	 * @param time The time the customer started the call
	 * @param maxDelay The maximum delay for the call
	 * @param duration The duration of the call
	 * @return The block's starting time, or -1 if no block was found
	 */
	private int findEmptyOccupation(int time, int maxDelay, int duration) {
		int chunkSize = 0;
		int maxStart = Math.min(occupationTable.length-1-duration, time+maxDelay);
		
		for (int i=time; i<maxStart+duration; i++) {
			if (occupationTable[i] < capacity) {
				chunkSize++;
				if (chunkSize == duration) {
					return i-(duration-1);
				}
			} else {
				chunkSize = 0;
			}
		}
		
		return -1;
	}

	/**
	 * Gets the POP id
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Calculates the stars given for a call, if it had no delays.
	 * @param call the call
	 * @return the stars
	 */
	public int getCallStars(Call call) {
		// We just substract 1 star for every 10m of distance
		double dist = this.getCallDistance(call);
		int stars = 5 - (int)(dist/10.0);
		stars = Math.max(stars, 0);
		return stars;
	}
	
	/**
	 * Calculates the delay between a call's start and its establishment
	 * @param call the call
	 * @return the delay
	 */
	public int getStoreDelay(Call call) {
		// Calculate the maximum delay, after which we get no stars for the call
		int time = call.getTime();
		int initialStars = getCallStars(call);
		int maxDelay = (initialStars-1)*10;
		
		// Find the earliest block available for this call
		int startTime = findEmptyOccupation(time, maxDelay, call.getDuration());
		
		// If a block was found, return the establishment delay
		if (startTime != -1) {
			return startTime - time;
		} else {
			return -1;
		}
	}
	
	/**
	 * Gets the stars given to this POP's calls
	 * @return the stars count
	 */
	public int getTotalStars() {
		return totalStars;
	}

	/**
	 * Gets the amount of calls established through this POP
	 * @return the calls' count
	 */
	public int getCallsCount() {
		return totalCalls;
	}
	
	/**
	 * Gets the total duration of this POP's calls
	 * @return the calls' duration
	 */
	public int getTotalDuration() {
		return totalDuration;
	}
}
