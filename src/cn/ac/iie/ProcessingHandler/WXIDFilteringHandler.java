package cn.ac.iie.ProcessingHandler;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

import cn.ac.iie.Util.IPSearcher;
import cn.ac.iie.entity.WXMessOriginEntity;

public class WXIDFilteringHandler implements  EventHandler<WXMessOriginEntity>,LifecycleAware{
	private final static Logger LOG = LoggerFactory.getLogger(WXIDFilteringHandler.class);
    private String oldName;
    private String name = "WXIDFilteringThread";
	
	@SuppressWarnings("unchecked")
	private static Map<Long,Map<Long,byte[]>>[] wxidToZhuti=(Map<Long, Map<Long,byte[]>>[]) new Map[2];
	
	private static AtomicInteger idx = new AtomicInteger(0);
	
	public WXIDFilteringHandler() {
		LOG.info("CONSTRUCT WXIDFilteringHandler");
	}
	
	@Override
	public void onEvent(WXMessOriginEntity  event, long sequence, boolean endOfBatch) throws Exception {
		try {
			if (event.getTag() < 0 || ((event.getTag() & 32) != 0) || (event.getTag() & 64) == 0)
				return;

			if (wxidToZhuti[idx.get()].containsKey(event.getU_ch_id())) {
				event.setTag(event.getTag() | 2);
				for (Map.Entry<Long,byte[]> en : wxidToZhuti[idx.get()].get(event.getU_ch_id()).entrySet()) {
					event.addThemes(en.getKey());
					LOG.info("targeted wxid:{} zhuti:{}" ,event.getU_ch_id(), en.getKey());
				}
			}
			
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
	}
	
	public static synchronized void setWXIDMap(Map<Long, Map<Long,byte[]>> map){
		wxidToZhuti[(idx.get() + 1) % 2] = map;
		LOG.info("setWXIDMap");
		idx.set((idx.get() + 1) % 2);
		if (wxidToZhuti[(idx.get() + 1) % 2] != null) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			wxidToZhuti[(idx.get() + 1) % 2].clear();
		}
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
