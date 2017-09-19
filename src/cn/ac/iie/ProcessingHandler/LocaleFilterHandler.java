package cn.ac.iie.ProcessingHandler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.WorkHandler;

import cn.ac.iie.Confguration.Config;
import cn.ac.iie.Util.IPLocation;
import cn.ac.iie.Util.IPSearcher;
import cn.ac.iie.Util.RDBUtil;
import cn.ac.iie.entity.HotLocale;
import cn.ac.iie.entity.WXMessOriginEntity;

public class LocaleFilterHandler implements EventHandler<WXMessOriginEntity>,LifecycleAware {

	private static Logger LOG = LoggerFactory.getLogger(LocaleFilterHandler.class);
    private IPSearcher ips;
	
	private static Map<String,Integer> locMap = new HashMap<String,Integer>();
    
	public static long startLong = System.currentTimeMillis();
	public static Map<HotLocale, Long> hotLocMap = new HashMap<HotLocale, Long>();

	private RDBUtil rdb = new RDBUtil();
	
    private String oldName;
    private final String name="LocaleFilterHandlerThread";
	
	public LocaleFilterHandler(String ip2regiondb) {
		super();
		ips = new IPSearcher(ip2regiondb);
	}
	
	@Override
	public void onEvent(WXMessOriginEntity event, long sequence, boolean endOfBatch) throws Exception {
		try {
			if (event.getTag() < 0)
				return;
			if (event.getU_send_ip() != null) {
				IPLocation location = ips.doSearch2Loc(event.getU_send_ip());
				
//				LOG.debug(event.getU_send_ip() + "  location:" + location);
				//省市县
				Integer province = locMap.getOrDefault(location.province, -1);
				Integer city = locMap.getOrDefault(location.city, -1);
				event.setU_loc_province(province);
				event.setU_loc_county(city);
				
				//统计重点区域
				if (province > 0) {
					HotLocale loc = new HotLocale();
					loc.setL_PROVINCE(location.province);
					loc.setL_PROVINCE_CODE(province);
					if (hotLocMap.containsKey(loc)) {
						hotLocMap.put(loc, hotLocMap.get(loc) + 1L);
					} else {
						hotLocMap.put(loc, 1L);
					}
				}
				long currentLong = System.currentTimeMillis();
				if ((currentLong - startLong) > Config.intervalMinute * 60 * 1000) {
					LOG.debug("pppppppppppppppppppppppppppppppppppp: insert oracle start");
					synchronized (hotLocMap) {
						hotLocMap.forEach((hotLoc, count) -> {
							LOG.info(hotLoc.toString()+":::::"+count);
							rdb.updataLoc(hotLoc, count);
						});
					hotLocMap.clear();
					}
					startLong = currentLong;
					LOG.debug("pppppppppppppppppppppppppppppppppppp: insert oracle end");
				}
				
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		} finally {
		}
	}

	
	public static synchronized void setLocMap(Map<String,Integer> slocMap) {
		locMap.putAll(slocMap);
		LOG.info("set or update WXIDMap by ybk");
	}
	

	@Override
	public void onStart() {
        final Thread currentThread = Thread.currentThread();
        oldName = currentThread.getName();
        currentThread.setName(name);
	}

	@Override
	public void onShutdown() {
		Thread.currentThread().setName(oldName);
	}
	
}
