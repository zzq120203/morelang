package cn.ac.iie.Util;

import java.util.Comparator;

public class GLink implements Comparator<GLink> {
	public String source;
	public String target;
	public int value;
	
	public GLink(String source, String target) {
		this.source = source;
		this.target = target;
		this.value = 1;
	}

	@Override
	public int compare(GLink o1, GLink o2) {
		String a = o1.source + "->" + o1.target;
		String b = o2.source + "->" + o2.target;
		return b.compareTo(a);
	}
}
