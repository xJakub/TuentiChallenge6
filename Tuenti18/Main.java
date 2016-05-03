import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
	

	static char[] cypherText = null;
	static int textLength;
	static boolean[] validChars = null;

	public static void main(String[] args) throws Exception {
		openFiles("cases.txt", "output.txt");
		
		// Convert input to char[]
		String hex = sc.next();
		textLength = hex.length() / 2;
		cypherText = new char[textLength];

		for (int i=0; i<textLength; i++) {
			cypherText[i] = (char) Integer.parseInt(hex.substring(i*2, i*2+2), 16);
		}

		// Fill the valid letters (writable English characters, symbols and line-breaks)
		validChars = new boolean[256];
		validChars['\n'] = true;
		for (int i=32; i<=126; i++) {
			validChars[i] = true;
		}

		// Read all the words from words.txt (the same file as in Ch5)
		String[] words = readWords();
		
		// Begin with an empty solution
		char[] solution = new char[textLength];
		
		// Find a solution by using any 3-words combination at the beginning,
		// as long as the 3 words+spaces have more than 10 characters.
		//
		// This exploits the fact that k=l//2 when i=0, which means that
		// we can check whether a beginning is invalid to reduce the
		// number of possible combinations.
		//
		// Also, about a half of the letters are ciphered using a k which is
		// between l//2-1 and l//2+1, so guessing the rest of the phrase
		// is a lot easier once you have the correct beginning.
		//
		tryWords(solution, 0, 3, 10, words);
		
		closeFiles();
	}

	// Read the words from words.txt and return them in a String[]
	private static String[] readWords() throws IOException {
		ArrayList<String> result = new ArrayList<>();

		FileReader wordsFr = new FileReader("words.txt");
		Scanner wordsSc = new Scanner(wordsFr);
		
		while (wordsSc.hasNext()) {
			String word = wordsSc.next();
			result.add(word.trim());
		}
		
		wordsSc.close();
		wordsFr.close();
		return result.toArray(new String[result.size()]);
	}

	/**
	 * Find a solution by using a words dictionary
	 * @param oldSolution The current solution
	 * @param offset The position at which we will add a new word
	 * @param wordsLeft The number of words we can still add
	 * @param minLength The minimum length required for all the n words we use + spaces
	 * @param words An array containing the words to try.
	 * @throws IOException 
	 */
	private static void tryWords(char[] oldSolution, int offset, int wordsLeft, int minLength, String[] words) throws IOException {
		for (String word : words) {
			char[] solution = oldSolution.clone();
			
			// words.txt uses upper-case words, so we convert them to lower-case.
			// If this is the first word, we have a capital letter.
			word = word.toLowerCase();
			if (offset == 0) {
				word = word.substring(0, 1).toUpperCase() + word.substring(1);
			}
			
			int newOffset = word.length()+offset+1;
			
			// If there's enough space word the word, try it 
			if (newOffset <= solution.length) {
				
				for (int i=0; i<word.length(); i++) {
					solution[offset+i] = word.charAt(i);
				}
				solution[offset+word.length()] = ' ';

				// Start guessing!
				if (checkAndSolve(solution, true)) {
					if (wordsLeft >= 1) {
						tryWords(solution, newOffset, wordsLeft-1, minLength, words);
					} else if (newOffset >= minLength) {
						checkAndSolve(solution, false);
					}
				}
			}
		}
	}
	
	/**
	 * Tries all the valid characters in a position
	 * @param offset The position
	 * @param solution The current solution
	 * @throws IOException
	 */
	private static void guessLetter(int offset, char[] solution) throws IOException {
		for (char c=0; c<256; c++) {
			if (validChars[c]) {
				solution[offset] = c;
				checkAndSolve(solution, false);
			}
		}
		solution[offset] = 0;
	}
	
	/**
	 * Checks if the current partial solution is valid. If justCheck is false, it
	 * recursively guesses and checks solutions, writing the correct ones.
	 * @param oldSolution The current solution
	 * @param justCheck If true, only checks if the partial solution is valid,
	 * without searching for a correct one.
	 * @return True if the current solution is valid.
	 * @throws IOException
	 */
	private static boolean checkAndSolve(char[] oldSolution, boolean justCheck) throws IOException {
		// Clone the old solution, we don't want to mess with our callers' data
		char solution[] = oldSolution.clone();

		int k = -1;
		int firstMissing = -1;
		boolean changed = true;

		// Re-check the solution every time we make a change
		while (changed) {
			firstMissing = -1;
			changed = false;

			for (int i=0; i<textLength; i++) {
				if (i == 0) {
					k = textLength / 2;
				}

				// If p[i] or p[k] is set for some position:
				// - If only one is set, deduce the another.
				// - If both are set, check if they are valid.
				if (k != -1) {
					if (solution[i] == 0 || solution[k] == 0) {
						if (solution[i] != 0) {
							// no p[k]
							solution[k] = (char) (cypherText[i] ^ solution[i]);
							changed = true;

							if (!validChars[solution[k]]) {
								return false;
							}
						} else if (solution[k] != 0) {
							// no p[i]
							solution[i] = (char) (cypherText[i] ^ solution[k]);
							changed = true;

							if (!validChars[solution[i]]) {
								return false;
							}
						}
					} else {
						if ((solution[i] ^ solution[k]) != cypherText[i]) {
							return false;
						}
					}
				}

				if (solution[i] == 0 & firstMissing == -1) {
					// The next letter will be guessed at this position
					firstMissing = i;
				}

				// Predict the next k, if possible
				if (k == -1 && solution[i] != 0) {
					if (solution[i] % 3 == 2) {
						k = textLength/2;
					}
				} else if (solution[i] != 0) {
					if (solution[i] % 3 == 2) {
						k = textLength/2;
					} else if (solution[i] % 3 == 0) {
						k = (k + 1) % textLength;
					} else {
						k = (k - 1 + textLength) % textLength;
					}
				} else {
					k = -1;
				}
			}
		}

		// Unless the caller wants us to only check the solution, guess
		// and check letters recursively until finding a correct one with
		// all the positions filled.
		if (!justCheck) {
			if (firstMissing != -1) {
				guessLetter(firstMissing, solution);
			} else {
				writeLine(new String(solution));
			}
		}
		return true;
	}
}
