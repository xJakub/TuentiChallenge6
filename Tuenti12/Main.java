import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
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

	public static VirusCity readTree(int n) {
		TreeMap<String, VirusCity> map = new TreeMap<>();

		for (int i=0; i<n-1; i++) {
			String cityFromName = sc.next();
			String cityToName = sc.next();

			if (!map.containsKey(cityFromName)) {
				map.put(cityFromName, new VirusCity(cityFromName));
			}
			if (!map.containsKey(cityToName)) {
				map.put(cityToName, new VirusCity(cityToName));
			}

			VirusCity cityFrom = map.get(cityFromName);
			VirusCity cityTo = map.get(cityToName);

			cityFrom.addJump(cityTo);
			cityTo.setParent(cityFrom);
		}

		VirusCity root = null;

		for (VirusCity tree : map.values()) {
			tree.sortChildren();
			if (!tree.hasParent()) {
				root = tree;
			}
		}

		root.setCityList(map);

		return root;
	}

	private static ArrayList<ArrayList<VirusCity>> getEquivalencies(
			VirusCity mainVirus) {

		ArrayList<ArrayList<VirusCity>> equivalencies = new ArrayList<>();


		for (VirusCity city : mainVirus.getCityList()) {
			boolean found = false;

			for (ArrayList<VirusCity> list : equivalencies) {
				if (city.equalsTo(list.get(0))) {
					list.add(city);
					found = true;
					break;
				}
			}

			if (!found) {
				ArrayList<VirusCity> list = new ArrayList<>();
				list.add(city);
				equivalencies.add(list);
			}
		}

		return equivalencies;
	}


	public static void main(String[] args) throws Exception {

		openFiles("submitInput.sql", "output.txt");

		int n = sc.nextInt();
		
		// read the original virus' cities
		VirusCity mainVirus = readTree(n);
		
		// and separate them into equivalency classes
		ArrayList<ArrayList<VirusCity>> mainEquivalencies = getEquivalencies(mainVirus);

		int t = sc.nextInt();
		
		for (int caseNumber=1; caseNumber<=t; caseNumber++) {
			// read another virus and check if its root
			// is equivalent to the original's one
			VirusCity otherVirus = readTree(n);

			if (!mainVirus.equalsTo(otherVirus)) {
				writeLine("Case #"+caseNumber+": NO");
			} else {
				write("Case #"+caseNumber+":");

				// separate the new cities into equivalency classes
				ArrayList<ArrayList<VirusCity>> otherEquivalencies = getEquivalencies(otherVirus);

				// both viruses have the same equivalency classes
				// since they are alphabetically sorted, we just
				// iterate them from left to right (at the same time)
				// to assign the related cities
				for (int mainListIndex=0; mainListIndex<mainEquivalencies.size(); mainListIndex++) {
					ArrayList<VirusCity> mainList = mainEquivalencies.get(mainListIndex);

					for (int otherListIndex=0; otherListIndex<mainEquivalencies.size(); otherListIndex++) {
						ArrayList<VirusCity> otherList = otherEquivalencies.get(otherListIndex);
						if (mainList.get(0).equalsTo(otherList.get(0))) {

							for (int l=0; l<mainList.size(); l++) {
								mainList.get(l).setRelatedCity(otherList.get(l));
							}
						}
					}
				}

				for (VirusCity city : mainVirus.getCityList()) {
					write(" " + city.getName() + "/" + city.getRelatedCity().getName());
				}

				writeLine("");
			}
		}

		closeFiles();
	}

}
