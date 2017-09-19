package cn.ac.iie.Util;

import java.util.Comparator;

public class GCategory implements Comparator<GCategory> {
	public String name;
	public String color;
	
	public GCategory(String name) {
		this.name = name;
	}
	
	public void setColor(String color) {
		this.color = color;
	}

	@Override
	public int compare(GCategory o1, GCategory o2) {
		return o2.name.compareTo(o1.name);
	}
}
