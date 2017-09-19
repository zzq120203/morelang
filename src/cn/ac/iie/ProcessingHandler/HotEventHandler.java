package cn.ac.iie.ProcessingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.WorkHandler;

import cn.ac.iie.Confguration.Config;
import cn.ac.iie.Util.DealEvent;
import cn.ac.iie.Util.RDBUtil;
import cn.ac.iie.entity.HotEvent;
import cn.ac.iie.entity.WXMessOriginEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * deal wechat message, if it is a hot, then get a HotMsg, else null
 */
public class HotEventHandler  implements EventHandler<WXMessOriginEntity>,LifecycleAware {
	private static Logger LOG = LoggerFactory.getLogger(HotEventHandler.class);
	private static DealEvent dealEvent;
	
	private RDBUtil rdb = new RDBUtil();

    private String oldName;
    private final String name="HotEventHandlerThread";
	
	public HotEventHandler() {
		if (dealEvent == null)
			dealEvent = new DealEvent();
	}
	
	@Override
	public void onEvent(WXMessOriginEntity event, long sequence, boolean endOfBatch) throws Exception {
		try {
			if (event.getTag() < 0) {
				return;
			}
			
			if (event.getM_type() == 1) {
//				LOG.debug(event.toString());
				HotEvent hotmsg = dealEvent.filterHotEvent(event.getM_content());
				if (hotmsg!= null) {
					LOG.debug("hot event:{}; content:{}",hotmsg.toString(),event.getM_content());
					try {
						putMap(hotmsg);
					} catch (Exception e) {
						LOG.error(e.getMessage(),e);
					}
				}
				long currentLong = System.currentTimeMillis();
				if ((currentLong - startLong) > Config.intervalMinute * 60 * 1000) {
					LOG.debug("pppppppppppppppppppppppppppp hot Event start");
					synchronized (hotEventMap) {
						hotEventMap.forEach((hotEvent, properties) -> {
							LOG.info(hotEvent.toString()+":::::"+properties);
							rdb.updataOrc(hotEvent, properties);
						});
						hotEventMap.clear();
					}
					startLong = currentLong;
					LOG.debug("pppppppppppppppppppppppppppp hot Event end");
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	private synchronized void putMap(HotEvent he) throws IOException {
		if (he != null) {
			if(hotEventMap.containsKey(he)) {
				//TODO
				HashSet<String> tmp = (HashSet<String>) hotEventMap.get(he).get(1);
				tmp.add(he.getKw_locale());
				hotEventMap.get(he).set(1, tmp);
				hotEventMap.get(he).set(2, ((long)(hotEventMap.get(he).get(2))) + 1L);
			} else {
				List<Object> tmpList = new ArrayList<Object>();
				HashSet<String> locale = new HashSet<>();
				locale.add(he.getKw_locale());
				tmpList.add(he.getKw_event());
				tmpList.add(locale);
				tmpList.add(1L);
				hotEventMap.put(he, tmpList);
			}
		}
		
	}
	
	public static long startLong = System.currentTimeMillis();
	public static Map<HotEvent, List<Object>> hotEventMap = new HashMap<HotEvent, List<Object>>();
	

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
