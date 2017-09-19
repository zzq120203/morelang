package cn.ac.iie.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 
 * 问题：无法识别中文语名
 * @author zzq12
 *
 */
public class UrlExtract {

	private static String regex = "(https:|http:)//[^\\s]+[a-z|A-Z|0-9]";
	
	public UrlExtract() {
		super();
		// TODO Auto-generated constructor stub
	}

	public HashSet<String> extract(String info) {
		info = info.replaceAll("[\u4e00-\u9fa5|【|】|，|。|“|”]", " ").replace("/:", " ").replaceAll("[-]{4,}", " ");

		HashSet<String> rlist = new HashSet<String>();
		Arrays.stream(info.split(" "))
			.filter(str -> !str.isEmpty())
			.forEach(str -> {
				rlist.addAll(processUrl(str));
			}
		);

		return rlist;
	}

	private Set<String> processUrl(String info) throws IllegalStateException{
		Pattern pattern = Pattern.compile(regex);
		info = info.replaceAll("http://", " http://").replaceAll("https://", " https://");
		Set<String> ulist = Arrays.stream(info.split(" "))
				.map(str -> {
					Matcher m = pattern.matcher(str);
					if (m.find())
						return m.group();
					else 
						return "";
				}).filter(str -> str.startsWith("http"))
				.collect(Collectors.toSet());
		return ulist;
	}

	private static void prtSet(Set<String> list) {
		list.stream().forEach(System.out::println);
	}
	
	public static void main(String[] args) {
		
		String txt1 = "【星钻限量】完美羽毛bb 限时特惠 抢 抢 抢！！！原价239.9元  券后【39.9元】"
				+ "包邮领券下单：https://s.click.taobao.com/1ygzkdw 圣罗兰气垫 水润，Q弹水光肌，"
				+ "轻薄净透，水润养颜，滋润保湿https://s.click.taobao.com/1ygzkdwhttps://s.click.taobao.com/1ygzkdw，清透不脱妆，美就是这样简单。复制这条信息，打开「"
				+ "手机淘宝」即https://s.click.taobao.com/1ygzkdw可领劵并下单￥TX8Y0c7qger￥ :time 1502076340";
		
		prtSet(new UrlExtract().extract(txt1));
	}

}
