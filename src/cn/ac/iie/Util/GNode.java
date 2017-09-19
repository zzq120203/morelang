package cn.ac.iie.Util;

import java.util.Comparator;

public class GNode implements Comparator<GNode> {
	public static class GNodeJ {
		public int category;
		public String name;
		public int value;
		/*
		 * group: "square"; user; "circle"
		 */
		public String symbol;
		
		public GNodeJ() {
		}
	}
	public String category;
	public static final int GROUP_NODE = 0;
	public static final int USER_NODE = 1;
	public int nType;
	public GNodeJ j;
	
	public GNode(String name) {
		this.j = new GNodeJ();
		this.j.name = name;
	}
	
	public GNode(String category, String name, int nType) {
		this.j = new GNodeJ();
		this.category = category;
		this.j.name = name;
		this.j.value = 0;
		this.nType = nType;
		if (nType == 0) {
			this.j.symbol = "square";
		} else {
			this.j.symbol = "circle";
		}
	}
	
	public String updateCategory(String newCategory) {
		String[] categories = this.category.split(",");
		
		if (categories.length > 0) {
			boolean isFound = false;
			
			for (String category : categories) {
				if (category.equalsIgnoreCase(newCategory)) {
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				this.category = this.category + "," + newCategory;
				return this.category;
			}
		}
		return null;
	}

	@Override
	public int compare(GNode o1, GNode o2) {
	    return o2.j.name.compareTo(o1.j.name);
	}
}
