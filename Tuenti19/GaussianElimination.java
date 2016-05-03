
public class GaussianElimination {
	private int rowsCount;
	private int columnsCount;
	private boolean[] coefficients;
	private int diagonalLength;
	private boolean[][] degreeCoefficients;
	private boolean[][] variables;
	private int firstEmptyRow;

	public GaussianElimination(boolean oldVariables[][], boolean oldCoefficients[]) {
		// initialize variables and store tables
		this.rowsCount = oldVariables.length;
		this.columnsCount = oldVariables[0].length;
		this.diagonalLength = Math.min(columnsCount, rowsCount);
		this.coefficients = oldCoefficients.clone();
		this.degreeCoefficients = new boolean[rowsCount][rowsCount];
		this.variables = new boolean[rowsCount][];
		
		for (int i=0; i<rowsCount; i++) {
			variables[i] = oldVariables[i].clone();
			
			// This will indicate if a row's coefficient depends
			// on a ledge's degrees. At the beginning, each row
			// depends on its own ledge
			degreeCoefficients[i][i] = true;
		}
		
		eliminate();
	}
	
	public boolean isSolution(int start, int end) {

		// The rows with variables can have any valid coefficient
		// So we will only check the empty ones
		
		for (int i=firstEmptyRow; i<rowsCount; i++) {
			
			// The coefficient if the ledge had an even degree
			boolean coefficient = coefficients[i];

			// If the ledge is the starting or ending ledge (only one),
			// then its degree is toggled
			
			if (degreeCoefficients[i][start]) {
				coefficient = !coefficient;
			}
			if (degreeCoefficients[i][end]) {
				coefficient = !coefficient;
			}
			
			// If we have 0 = 1 (false = true), then the solution
			// is invalid
			if (coefficient) {
				return false;
			}
		}
		
		return true;
	}
	
	
	//
	// Apply Gaussian elimination to the matrices
	//
	public void eliminate() {

		int discardedColumns = 0;
		
		// Loop through the diagonal
		for (int i=0; i<diagonalLength; i++) {

			// While we don't have a 1 in the current diagonal's cell,
			// we will swap rows or columns
			while (!variables[i][i]) {
				boolean found = false;

				// cell empty, look for another in the same column
				for (int row=i+1; row<rowsCount; row++) {
					if (variables[row][i]) {
						boolean[] currentRow = variables[i];
						boolean[] newRow = variables[row];

						variables[i] = newRow;
						variables[row] = currentRow;
						
						boolean[] currentDegrees = degreeCoefficients[i];
						degreeCoefficients[i] = degreeCoefficients[row];
						degreeCoefficients[row] = currentDegrees;

						boolean tmp = coefficients[i];
						coefficients[i] = coefficients[row];
						coefficients[row] = tmp;
						found = true;
						break;						
					}
				}

				// column empty, swap with another column and mark as discarded
				if (!found) {
					discardedColumns++;
					if (discardedColumns+i >= columnsCount) {
						break;
					}

					int newColumn = columnsCount-discardedColumns;
					for (int row=0; row<rowsCount; row++) {
						boolean tmp = variables[row][i];
						variables[row][i] = variables[row][newColumn];
						variables[row][newColumn] = tmp;
					}
				}
			}

			if (variables[i][i]) {
				// We got a 1, so the row is not empty
				// Empty the rest of the column's cells using
				// linear operations
				for (int row=0; row<rowsCount; row++) {
					if (row != i && variables[row][i]) {
						for (int col=i; col<columnsCount; col++) {
							variables[row][col] ^= variables[i][col];
						}
						for (int col=0; col<rowsCount; col++) {
							degreeCoefficients[row][col] ^= degreeCoefficients[i][col];
						}
						coefficients[row] ^= coefficients[i];
					}
				}
				
				// Mark the next row as the first empty, for now
				firstEmptyRow = i+1;
			}
		}
		
	}
}
