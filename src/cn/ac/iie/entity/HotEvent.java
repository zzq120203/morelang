package cn.ac.iie.entity;

import java.util.List;

public class HotEvent {
    private String e_province;//一级地域 省份
    private String e_city;//二级地域：城市
    private String e_class;//事件类别 例如自然灾害、社会公共事件
    private String e_name;//具体时间：山体滑坡，泥石流
    private String kw_locale;
    private String kw_event;
    private List<String> keyword;
	public String getE_province() {
		return e_province;
	}
	public void setE_province(String e_province) {
		this.e_province = e_province;
	}
	public String getE_city() {
		return e_city;
	}
	public void setE_city(String e_city) {
		this.e_city = e_city;
	}
	public String getE_class() {
		return e_class;
	}
	public void setE_class(String e_class) {
		this.e_class = e_class;
	}
	public String getE_name() {
		return e_name;
	}
	public void setE_name(String e_name) {
		this.e_name = e_name;
	}
	public String getKw_locale() {
		return kw_locale;
	}
	public void setKw_locale(String kw_locale) {
		this.kw_locale = kw_locale;
	}
	public String getKw_event() {
		return kw_event;
	}
	public void setKw_event(String kw_event) {
		this.kw_event = kw_event;
	}
	public List<String> getKeyword() {
		return keyword;
	}
	public void setKeyword(List<String> keyword) {
		this.keyword = keyword;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((e_city == null) ? 0 : e_city.hashCode());
		result = prime * result + ((e_name == null) ? 0 : e_name.hashCode());
		result = prime * result + ((e_province == null) ? 0 : e_province.hashCode());
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
		HotEvent other = (HotEvent) obj;
		if (e_city == null) {
			if (other.e_city != null)
				return false;
		} else if (!e_city.equals(other.e_city))
			return false;
		if (e_name == null) {
			if (other.e_name != null)
				return false;
		} else if (!e_name.equals(other.e_name))
			return false;
		if (e_province == null) {
			if (other.e_province != null)
				return false;
		} else if (!e_province.equals(other.e_province))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "HotEvent [e_province=" + e_province + ", e_city=" + e_city + ", e_class=" + e_class + ", e_name="
				+ e_name + "]";
	}
    
}
