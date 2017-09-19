package cn.ac.iie.Util;

public class IPLocation {
	public String state;
	public String region;
	public String province;
	public String city;
	public String isp;
	
	public IPLocation(String state, String region, String province, String city, String isp) {
		this.state = state;
		this.region = region;
		this.province = province;
		this.city = city;
		this.isp = isp;
	}
	
	public String toString() {
		return state + "," + region + "," + province + "," + city + "," + isp;
	}
}
