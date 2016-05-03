import java.util.Arrays;

public class HeldKarp {

	int[][] travelFrames;
	String[][] travelMoves;
	private int nodesCount;

	private int entryBestParents[][] = null;
	private int entryCosts[][] = null;
	private int bestFrames = 0;
	private String bestMoves = null;

	public HeldKarp(int nodesCount) {
		this.nodesCount = nodesCount;
		travelFrames = new int[nodesCount][nodesCount];
		travelMoves = new String[nodesCount][nodesCount];
	}

	public void setCost(int from, int to, int frames, String moves) {
		travelFrames[from][to] = frames;
		travelMoves[from][to] = moves;
	}
	
	private boolean isBetterStringThan(String a, String b) {
		if (b == null) return true;
		if (a.length() < b.length()) return true;
		if (a.length() == b.length()) {
			return a.compareTo(b) <= 1;
		}
		return false;
	}

	public void solve() {
		int otherNodesCount = nodesCount-1;
		int entryCount = (int)(Math.pow(2, otherNodesCount));

		entryBestParents = new int[otherNodesCount][entryCount];
		entryCosts = new int[otherNodesCount][entryCount];

		for (int i=0; i<otherNodesCount; i++) {
			Arrays.fill(entryCosts[i], Integer.MAX_VALUE);
		}

		if (nodesCount >= 2) {
			for (int i=1; i<nodesCount; i++) {
				solveEntryWithoutParents(i);
			}
			solveIncremental(0);
			solveFinal();
		} else {
			// If there's only one node, then the map has no keys
			// and the solution is the path from Start to Exit
			if (travelMoves[0][0] != null) {
				bestMoves = travelMoves[0][0];
				bestFrames = travelFrames[0][0];
			}
		}
	}

	private void solveIncremental(int mask) {
		// Mask indicates the bits that the calling function
		// used as parents. We can now add a new parent
		// until using all the available nodes.
		
		for (int newParent=1; newParent<nodesCount; newParent++) {
			int newMask = 1 << (newParent-1);
			if ((mask & newMask) != 0) {
				break;
			} else {
				mask ^= newMask;

				for (int dest=1; dest<nodesCount; dest++) {
					int destMask = 1 << (dest-1);
					if ((mask & destMask) == 0) {
						solveEntry(dest, mask);
					}
				}

				solveIncremental(mask);
				mask ^= newMask;
			}
		}
	}

	private void solveEntryWithoutParents(int dest) {
		if (travelFrames[0][dest] != 0) {
			entryCosts[dest-1][0] = travelFrames[0][dest];
		}
	}

	private void solveEntry(int dest, int mask) {
		// Obtain the best path from 0 to dest, using
		// the parents indicated in mask (a bitmask)
		
		int entryOffset = mask;

		int min = Integer.MAX_VALUE;
		int bestParents = 0;

		for (int parent=1; parent<nodesCount; parent++) {
			int parentMask = 1 << (parent-1);
			if ((mask & parentMask) == 0) {
				// not one of the parents
				continue;
			}

			int parentEntryOffset = mask-parentMask;

			if (entryCosts[parent-1][parentEntryOffset] != Integer.MAX_VALUE && travelFrames[parent][dest] != 0) {
				int cost = entryCosts[parent-1][parentEntryOffset] + travelFrames[parent][dest];

				if (cost < min) {
					min = cost;
					bestParents = parentMask;
				}
				else if (cost == min) {
					min = cost;
					bestParents |= parentMask;
				}
			}
		}

		entryCosts[dest-1][entryOffset] = min;
		entryBestParents[dest-1][entryOffset] = bestParents;
	}

	private void solveFinal() {
		// We have obtained all the best paths for all the cities
		// All that's left is to calculate the best path to the exit (0)
		
		int mask = (1 << (nodesCount-1))-1;
		int min = Integer.MAX_VALUE;
		int bestParents = 0;

		for (int parent=1; parent<nodesCount; parent++) {
			int parentMask = 1 << (parent-1);
			if ((mask & parentMask) == 0) {
				// not one of the parents
				continue;
			}

			int parentEntryOffset = (mask-parentMask);
			if (entryCosts[parent-1][parentEntryOffset] != Integer.MAX_VALUE && travelFrames[parent][0] != 0) {
				int cost = entryCosts[parent-1][parentEntryOffset] + travelFrames[parent][0];
				if (cost < min) {
					min = cost;
					bestParents = parentMask;
				}
				else if (cost == min) {
					bestParents |= parentMask;
				}
			}
		}

		// Now that the best paths are known, let's calculate
		// the best keys combination
		for (int parent=1; parent<nodesCount; parent++) {
			int parentMask = 1 << (parent-1);
			if ((parentMask & bestParents) != 0) {
				String moves = getBestMove(parent, mask-parentMask);
				moves += travelMoves[parent][0];

				if (isBetterStringThan(moves, bestMoves)) {
					bestMoves = moves;
				}
			}
		}

		bestFrames = min;
	}

	private String getBestMove(int dest, int mask) {
		int bestParents = entryBestParents[dest-1][mask];
		String localBestMoves = null;

		if (mask == 0) {
			return travelMoves[0][dest];
		}

		for (int parent=1; parent<nodesCount; parent++) {
			int parentMask = 1 << (parent-1);
			if ((parentMask & bestParents) != 0) {
				String moves = getBestMove(parent, mask-parentMask);
				moves += travelMoves[parent][dest];
				if (isBetterStringThan(moves, localBestMoves)) {
					localBestMoves = moves;
				}
			}
		}

		return localBestMoves;
	}

	public int getFrames() {
		return bestFrames;
	}

	public String getMoves() {
		return bestMoves;
	}

	public boolean isPossible() {
		return bestMoves != null;
	}
}
