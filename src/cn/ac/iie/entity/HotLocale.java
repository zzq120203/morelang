package cn.ac.iie.entity;

public class HotLocale {

	private int L_PROVINCE_CODE;
	private int L_CITY_CODE;
	private String L_PROVINCE;
	private String L_CITY;
	private String L_EVENT;
	public int getL_PROVINCE_CODE() {
		return L_PROVINCE_CODE;
	}
	public void setL_PROVINCE_CODE(int l_PROVINCE_CODE) {
		L_PROVINCE_CODE = l_PROVINCE_CODE;
	}
	public int getL_CITY_CODE() {
		return L_CITY_CODE;
	}
	public void setL_CITY_CODE(int l_CITY_CODE) {
		L_CITY_CODE = l_CITY_CODE;
	}
	public String getL_PROVINCE() {
		return L_PROVINCE;
	}
	public void setL_PROVINCE(String l_PROVINCE) {
		L_PROVINCE = l_PROVINCE;
	}
	public String getL_CITY() {
		return L_CITY;
	}
	public void setL_CITY(String l_CITY) {
		L_CITY = l_CITY;
	}
	public String getL_EVENT() {
		return L_EVENT;
	}
	public void setL_EVENT(String l_EVENT) {
		L_EVENT = l_EVENT;
	}
	public HotLocale() {
		super();
		// TODO Auto-generated constructor stub
	}
	public HotLocale(int l_PROVINCE_CODE, int l_CITY_CODE, String l_PROVINCE, String l_CITY, String l_EVENT) {
		super();
		L_PROVINCE_CODE = l_PROVINCE_CODE;
		L_CITY_CODE = l_CITY_CODE;
		L_PROVINCE = l_PROVINCE;
		L_CITY = l_CITY;
		L_EVENT = l_EVENT;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + L_CITY_CODE;
		result = prime * result + L_PROVINCE_CODE;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HotLocale other = (HotLocale) obj;
		if (L_CITY_CODE != other.L_CITY_CODE)
			return false;
		if (L_PROVINCE_CODE != other.L_PROVINCE_CODE)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "HotLocale [L_PROVINCE_CODE=" + L_PROVINCE_CODE + ", L_CITY_CODE=" + L_CITY_CODE + ", L_PROVINCE="
				+ L_PROVINCE + ", L_CITY=" + L_CITY + ", L_EVENT=" + L_EVENT + "]";
	}
	
}
