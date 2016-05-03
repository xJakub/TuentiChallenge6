import java.awt.Point;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class SpeedRunner {

	private int mapWidth;
	private int mapHeight;
	private Point[] keysArray;
	private int keysCount = 0;
	private Point startPoint = null;
	private boolean isWall[][] = null;
	private boolean isLadder[][] = null;
	private int[][] positionIds = null;
	private State[] pathTo = null;
	private HeldKarp solver = null;

	class State {
		Point position;
		int frames;
		String moves;

		@Override
		public int hashCode() {
			int result = 0;
			result += position.x;
			result *= mapHeight;
			result += position.y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			State otherState = (State) obj;
			return (
					position.equals(otherState.position)
					);
		}

		// get a cloned state and make a move
		public State getCopy(int xDiff, int yDiff, char move) {

			int newX = (position.x + xDiff + mapWidth) % mapWidth;
			int newY = position.y + yDiff;

			if (newY >= 0 && newY < mapHeight && !isWall[newX][newY]) {

				State copy = new State();
				copy.position = new Point(newX, newY);
				copy.frames = frames + 1;
				copy.moves = moves + move;

				copy.checkPosition();

				return copy;
			}
			else {
				return null;
			}
		}

		// get a cloned state without making a move
		public State getClone() {
			State copy = new State();
			copy.position = new Point(position.x, position.y);
			copy.frames = frames;
			copy.moves = moves;
			return copy;
		}

		// Check if we have a new key or are falling
		void checkPosition() {
			int positionId = positionIds[position.x][position.y];
			if (positionId != 0) {
				if (pathTo[positionId-1] == null
						|| isBetterThan(pathTo[positionId-1])
						) {
					pathTo[positionId-1] = getClone();
				}
			}

			if (
					!isWall[position.x][position.y+1] &&
					!isLadder[position.x][position.y+1] &&
					!isLadder[position.x][position.y]
					) {
				position = new Point(position.x, position.y+1);
				frames++;
				checkPosition();
			}
		}

		// we can move down if we are on or above a ladder
		public boolean canMoveDown() {
			return isLadder[position.x][position.y] ||
					isLadder[position.x][position.y+1];
		}

		public boolean isBetterThan(State other) {
			boolean better = false;
			if (other == null) {
				better = true;
			} else if (other.frames > frames) {
				better = true;
			} else if (other.frames == frames) {
				if (other.moves.length() > moves.length()) { 
					better = true;
				} else if (other.moves.length() == moves.length()) {
					if (other.moves.compareTo(moves) >= 1) {
						better = true;
					}
				}
			}
			return better;
		}
	}

	public SpeedRunner(int mapWidth, int mapHeight) {
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;

		isWall = new boolean[mapWidth][mapHeight];
		isLadder = new boolean[mapWidth][mapHeight];

		keysArray = new Point[16];
		positionIds = new int[mapWidth][mapHeight];
	}

	public void setCharacter(int x, int y, char c) {

		if (c == 'S') {
			startPoint = new Point(x, y);
		} else if (c == 'E') {
			positionIds[x][y] = 1;
		} else if (c == '#') {
			isWall[x][y] = true;
		} else if (c == 'K') {
			Point key = new Point(x, y);
			keysArray[keysCount] = key;
			positionIds[x][y] = keysCount + 2;
			keysCount++;
		} else if (c == 'H') {
			isLadder[x][y] = true;
		}

	}

	public State[] getPathsFrom(Point origin) {

		pathTo = new State[keysCount+1];
		
		State rootState = new State();
		rootState.frames = 0;
		rootState.moves = "";
		rootState.position = origin;
		rootState.checkPosition();

		PriorityQueue<State> queue = new PriorityQueue<>(new Comparator<State>() {

			@Override
			public int compare(State o1, State o2) {
				return o1.isBetterThan(o2) ? -1 : 1;
			}
		});

		HashMap<State, State> visited = new HashMap<>();

		queue.add(rootState);

		while(!queue.isEmpty()) {
			State state = queue.poll();

			if (!visited.containsKey(state) || state.isBetterThan(visited.get(state))) {
				visited.put(state, state);

				
				// Create new substates
				State leftState = state.getCopy(-1, 0, 'A');
				if (leftState != null) {
					queue.add(leftState);
				}
				State rightState = state.getCopy(1, 0, 'D');
				if (rightState != null) {
					queue.add(rightState);
				}

				if (state.canMoveDown()) {
					State downState = state.getCopy(0, 1, 'S');
					if (downState != null) {
						queue.add(downState);
					}
				}

				State upState = state.getCopy(0, -1, 'W');
				if (upState != null) {
					queue.add(upState);
				}

			}
		}
		
		return pathTo;
	}

	public int getFrames() {
		// these results are given by the Held-Karp class
		return solver.getFrames();
	}

	public String getMoves() {
		return solver.getMoves();
	}

	public boolean isPossible() {
		return solver.isPossible();
	}

	public void findPaths() {
		positionIds = new int[mapWidth][mapHeight];

		for (int k=0; k<keysCount; k++) {
			positionIds[keysArray[k].x][keysArray[k].y] = k+1;
		}
	}

	public void playLevel() {
		State[][] pathsFromOrigin = new State[keysCount+1][];
		
		// first, find the paths from the origin to every key + the exit
		pathsFromOrigin[0] = getPathsFrom(startPoint);
		
		// then, find the path from every key to the rest of the keys + the exit
		for (int i=0; i<keysCount; i++) {
			pathsFromOrigin[i+1] = getPathsFrom(keysArray[i]);
		}
		
		// now that we have all paths for a aTSP problem, let's use
		// Held-Karp to get the optimal path
		solver = new HeldKarp(keysCount+1);
		
		for (int i=0; i<pathsFromOrigin.length; i++) {
			for (int j=0; j<pathsFromOrigin.length; j++) {
				if (pathsFromOrigin[i][j] != null) {
					solver.setCost(i, j, pathsFromOrigin[i][j].frames, pathsFromOrigin[i][j].moves);
				}
			}
		}
		solver.solve();
	}

}
