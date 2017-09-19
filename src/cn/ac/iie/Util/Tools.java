package cn.ac.iie.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;


public class Tools {
	private static SimpleDateFormat sd = new SimpleDateFormat("yyyyMMddHH");

	/**
	 *  converts timestamp (in seconds) to data format(yyyyMMddhh)
	 * @param timeStampsInSecond
	 * @return
	 */
	
	public static String TimestampsToDate(long timeStampsInSecond){
		Date date=new java.util.Date(timeStampsInSecond*1000);
		return sd.format(date);
	}
	
	/**
	 *  converts timestamp (in seconds) to data format(yyyyMMddhh)
	 * @param timeStampsInSecond
	 * @return
	 */
	public static String TimestampsToDate(String timeStampsInSecond){
		return TimestampsToDate(Long.parseLong(timeStampsInSecond));
	}
	
	public static Map<String,Long> MapAdd(Map<String,Long> map,String key, Long inc){
		if(map==null)
			map=new HashMap<String,Long>();
		
		if(map.containsKey(key)){
			map.put(key, map.get(key)+inc);
		}else{
			map.put(key, inc);
		}
		return map;
	}
	
	public static Map<Integer,Long> MapAdd(Map<Integer,Long> map,Integer key,Long inc){
		if(map==null)
			map=new HashMap<Integer,Long>();
		
		if(map.containsKey(key))
			map.put(key, map.get(key)+inc);
		else
			map.put(key, inc);
		
		return map;
	}
	
	public static Map<Long,Long> MapAdd(Map<Long,Long> map,Long key,Long inc){
		if(map==null)
			map=new HashMap<Long,Long>();
		
		if(map.containsKey(key))
			map.put(key, map.get(key)+inc);
		else
			map.put(key, inc);
		
		return map;
	}
	
	public static String convertFirstCapital(String fildeName){
		byte[] items = fildeName.getBytes();
		items[0] =  (byte)((char)items[0]-'a'+'A');;
		return new String(items);
	}
	
	public static long genSimHashCode(String content) {
		List<Term> list = ToAnalysis.parse(content).getTerms();
		int []midBitValue = new int[64];
		Map<String,Integer> map = new HashMap<String,Integer>();
		
		for (int i = 0; i < list.size(); ++i) {
			if (map.containsKey(list.get(i).getName())) {
				map.put(list.get(i).getName(), map.get(list.get(i).getName())+1);
			} else {
				map.put(list.get(i).getName(), 1);
			}
		}
		
		for (Map.Entry<String, Integer> en : map.entrySet()) {
			long hashV = cn.ac.iie.DS.MurmurHash.hash64(en.getKey());
			for (int i = 0; i < 63; ++i) {
				if ((hashV & (1L << i)) == 0) {
					midBitValue[i] -= en.getValue();
				} else {
					midBitValue[i] += en.getValue();
				}
			}
		}
		
		long hashV = 0L;
		for (int i = 0; i < 64; ++i) {
			if (midBitValue[i] > 0) {
				hashV = hashV | (1L << i);
			}
		}
		return hashV;
	}
}
