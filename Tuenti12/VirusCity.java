import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

public class VirusCity {
	
	private ArrayList<VirusCity> children;
	private String name;
	private VirusCity parent = null;
	private TreeMap<String, VirusCity> cityList;
	private VirusCity relatedCity;
	private Integer rootDistance = null;
	
	public VirusCity(String name) {
		children = new ArrayList<>();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean hasParent() {
		return parent != null;
	}
	
	public void setParent(VirusCity parent) {
		this.parent  = parent;
	}
	
	public void addJump(VirusCity to) {
		children.add(to);
	}
	
	public int getRootDistance() {
		if (this.parent == null) {
			return 0;
		} else {
			if (rootDistance == null) {
				rootDistance = this.parent.getRootDistance()+1;
			}
			return rootDistance;
		}
	}
	
	public void sortChildren() {
		children.sort(new Comparator<VirusCity>() {

			@Override
			public int compare(VirusCity o1, VirusCity o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
	}
	
	public VirusCity[] getChildren() {
		return children.toArray(new VirusCity[children.size()]);
	}
	
	public int childrenCount() {
		return children.size();
	}
	
	public boolean equalsTo(VirusCity otherTree) {
		if (this.childrenCount() != otherTree.childrenCount()
				|| this.getRootDistance() != otherTree.getRootDistance()) {
			return false;
		}
		
		VirusCity[] myChildren = getChildren();
		VirusCity[] otherChildren = otherTree.getChildren();
		boolean otherUsed[] = new boolean[myChildren.length];

		for (int i=0; i<myChildren.length; i++) {
			boolean ok = false;
			
			for (int j=0; j<otherChildren.length; j++) {
				if (otherUsed[j]) continue;
				
				if (myChildren[i].equalsTo(otherChildren[j])) {
					otherUsed[j] = true;
					ok = true;
					break;
				}
			}
			
			if (!ok) return false;
		}
		
		return true;
	}

	public void setCityList(TreeMap<String, VirusCity> map) {
		this.cityList = map;
	}
	
	// root nodes have a list of all the nodes in the tree
	public VirusCity[] getCityList() {
		return cityList.values().toArray(new VirusCity[cityList.size()]);
	}
	
	public void setRelatedCity(VirusCity city) {
		this.relatedCity = city;
	}
	
	public VirusCity getRelatedCity() {
		return this.relatedCity;
	}
}
