package cn.ac.iie.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * manage location and event, including: location event and location code ,event
 * code
 */
public class MapUtil {
	private static class SingleHelper {
		static final MapUtil INSTANCE = new MapUtil();
	}

	public HashMap<String, String> location = null;
	public HashMap<String, String> event = null;
	public HashMap<String, Integer> hotWordMap = null;
	public HashMap<String, Integer> locationAndCode = null;
	public HashMap<String, String> eventAndCode = null;
	public Set<String> filterOutWord=null;

	private MapUtil() {
		loadLocation();
		loadEvent();
		loadHotWordMap();
		loadFilterOutWord();
	}


	public static MapUtil getInstance() {
		return SingleHelper.INSTANCE;
	}

	/**
	 * 更新热词的频率信息
	 *
	 * @param keyword:要更新的热词
	 */
	public synchronized void updateHotWordMap(String keyword) {
		if (hotWordMap.containsKey(keyword)) {
			int value = hotWordMap.get(keyword);
			hotWordMap.put(keyword, value + 1);
		} else
			hotWordMap.put(keyword, 1);
	}

	/**
	 * 创建hotwordMap
	 *
	 * @return
	 */
	private void loadHotWordMap() {
		this.hotWordMap = new HashMap<String, Integer>();
	}

	/**
	 * return top k hotword
	 *
	 * @param k
	 * @return: hotword list
	 */
	public List<String> getTopK(int k) {
		List<String> hotWordList = new ArrayList<String>();
		Map<String, Integer> sortedHotMap = this.sortHotWordMap(hotWordMap);
		Iterator<Map.Entry<String, Integer>> iterator = sortedHotMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Integer> entry = iterator.next();
			hotWordList.add(entry.getKey());
		}
		return hotWordList;
	}

	public Map<String, Integer> sortHotWordMap(Map<String, Integer> hotwordMap) {
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		if (hotwordMap != null && !hotwordMap.isEmpty()) {
			List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(
					hotwordMap.entrySet());
			Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
					return o1.getValue() - o2.getValue();
				}
			});
			Iterator<Map.Entry<String, Integer>> iter = entryList.iterator();
			Map.Entry<String, Integer> tmpEntry = null;
			while (iter.hasNext()) {
				tmpEntry = iter.next();
				sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
			}

		}
		return sortedMap;
	}

	/**
	 * 加载地点列表（省市县三级）
	 */
	private void loadLocation() {
		location = new HashMap<String, String>();
		// 获取location.csv的inputstream
		InputStream locInputStream = this.getClass().getResourceAsStream("/library/location.csv");
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(locInputStream, "gbk");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line = null;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				String[] line_contents = line.split(",");
				if (line_contents.length == 1)
					location.put(line_contents[0], null);
				else if (line_contents.length == 2)
					location.put(line_contents[0], line_contents[1]);
				else if (line_contents.length == 3 && line_contents[1] != null)
					location.put(line_contents[0], line_contents[1] + "," + line_contents[2]);
				else if (line_contents.length == 3 && line_contents[1] == null)
					location.put(line_contents[0], line_contents[2]);
				else
					System.err.println("Error: The format of location is invalid");

			}
		} catch (IOException e) {
			System.out.println("Error: Common.java/Fun getLocation/line 24");
			e.printStackTrace();
		}
	}

	/**
	 * 添加一个三级地点，例如，那么需要传递的参数为(五道口，海淀区，北京)
	 *
	 * @param location
	 * @param cityName
	 * @param provinceName
	 * @return return true if add successfully else return false;
	 */
	public boolean addNewLocation(String location, String cityName, String provinceName) {
		if (this.location.containsKey(location))
			return false;
		else {
			if (cityName == null) {
				this.location.put(location, provinceName);
			} else {
				if (this.location.containsKey(location))
					this.location.put(location, cityName + "," + provinceName);
			}
			return true;
		}
	}

	/**
	 * 添加一个二级地点，例如（天安门，北京）
	 *
	 * @param locationName:地点名称
	 * @param provinceName：所属地区
	 * @return return true if add successfully else return false;
	 */
	public boolean addNewLocation(String locationName, String provinceName) {
		return this.addNewLocation(locationName, null, provinceName);
	}

	/**
	 * 对一个三级地点的信息进行更新
	 *
	 * @param locationName
	 * @param cityName
	 * @param provinceName
	 * @return
	 */
	public synchronized boolean updateLocation(String locationName, String cityName, String provinceName) {
		if (this.location.containsKey(locationName)) {
			if (cityName == null)
				this.location.put(locationName, provinceName);
			else
				this.location.put(locationName, cityName + "," + provinceName);
			return true;
		} else {
			System.err.println("This locationName doesn't exist!");
			return false;
		}
	}

	/**
	 * 对一个二级地点进行更新
	 *
	 * @param locationName
	 * @param province
	 * @return
	 */
	public synchronized boolean updateLocation(String locationName, String province) {
		return this.updateLocation(locationName, null, province);
	}

	/**
	 * 加载事件列表
	 *
	 * @return
	 */
	private void loadEvent() {
		this.event = new HashMap<String, String>();
		// 获取event.csv的inputstream
		InputStream evtInputStream = this.getClass().getResourceAsStream("/library/event.csv");
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(evtInputStream, "gbk");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line = null;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				String[] line_contents = line.split(",");
				if (line_contents.length == 2)
					this.event.put(line_contents[1], line_contents[0]);
				else
					System.err.println("Error:common.java/line64");
			}
		} catch (IOException e) {
			System.err.println("Exception:commom.java/line 61");
			e.printStackTrace();
		}
	}

	/**
	 * @param eventName:事件关键词
	 * @param eventClass:事件所属的大类别
	 * @return: return true if add successfully else return false;
	 */
	public boolean addNewEvent(String eventName, String eventClass) {
		if (this.event.containsKey(eventName)) {
			System.err.println("This keyword for event has already existed!");
			return false;
		} else
			this.event.put(eventName, eventClass);
		return true;
	}

	/**
	 * 对现有的eventName的大类别进行更改
	 *
	 * @param eventName:事件关键词
	 * @param eventClass:需要更新的类别
	 * @return: return true if update successfully else return false
	 */
	public synchronized boolean updataEvent(String eventName, String eventClass) {
		if (!this.event.containsKey(eventName)) {
			System.err.println("This keyword does't exist!");
			return false;
		} else
			this.event.put(eventName, eventClass);
		return true;
	}

	private void loadFilterOutWord() {
		this.filterOutWord=new HashSet<String>();
		InputStream filterOutInputStream = this.getClass().getResourceAsStream("/library/filterOutWord.txt");
		InputStreamReader filterOutReader = null;
		try {
			filterOutReader = new InputStreamReader(filterOutInputStream, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		BufferedReader bufferedReader = new BufferedReader(filterOutReader);
		String line = null;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				this.filterOutWord.add(line.trim());
			}
		} catch (IOException e) {
			System.err.println("Exception:commom.java/line 61");
			e.printStackTrace();
		}
		
	}


	/**
	 * get the superior for location e.g. tiananmen->beijing
	 *
	 * @param location
	 * @return
	 */
	public String trackLocation(String location) {
		if (!this.location.containsKey(location)) {
			System.err.println("Error:DealMsg.java/Fun trackLocation/line 73");
		} else {
			return this.location.get(location);
		}
		
		return null;
	}
	/**
	 * convert location: e.g. 四川->四川省
	 * @param locationWord
	 * @return
	 */
	public String locationConvert(String locationWord) {
		if(this.location.containsKey(locationWord))return locationWord;
		Iterator<Map.Entry<String, String>>iterator=this.location.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<String, String>entry=iterator.next();
			String key=entry.getKey();
			if(key.contains(locationWord)) {
				return key;
			}
		}
		return null;
	}

	/**
	 * get the class for an event e.g. earthquake->natural disaster
	 *
	 * @param evtWord
	 * @return event class
	 */
	public String trackEvent(String evtWord) {
		if (!this.event.containsKey(evtWord)) {
			System.err.println("Error:DealMsg.java/Funt trackEvent/line 93");
		} else {
			return this.event.get(evtWord);
		}
		return null;
	}


	public HashMap<String, String> getLocation() {
		return location;
	}


	public void setLocation(HashMap<String, String> location) {
		this.location = location;
	}


	public HashMap<String, String> getEvent() {
		return event;
	}


	public void setEvent(HashMap<String, String> event) {
		this.event = event;
	}


	public HashMap<String, Integer> getHotWordMap() {
		return hotWordMap;
	}


	public void setHotWordMap(HashMap<String, Integer> hotWordMap) {
		this.hotWordMap = hotWordMap;
	}


	public HashMap<String, Integer> getLocationAndCode() {
		return locationAndCode;
	}


	public void setLocationAndCode(HashMap<String, Integer> locationAndCode) {
		this.locationAndCode = locationAndCode;
	}


	public HashMap<String, String> getEventAndCode() {
		return eventAndCode;
	}


	public void setEventAndCode(HashMap<String, String> eventAndCode) {
		this.eventAndCode = eventAndCode;
	}


	public Set<String> getFilterOutWord() {
		return filterOutWord;
	}


	public void setFilterOutWord(Set<String> filterOutWord) {
		this.filterOutWord = filterOutWord;
	}
	
}
