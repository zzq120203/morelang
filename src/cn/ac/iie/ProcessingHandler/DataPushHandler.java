package cn.ac.iie.ProcessingHandler;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.WorkHandler;

import cn.ac.iie.entity.WXMessOriginEntity;

public class DataPushHandler implements EventHandler<WXMessOriginEntity>,LifecycleAware {

	private static Logger LOG = LoggerFactory.getLogger(DataPushHandler.class);
    
    private String oldName;
    private final String name="DataPushHandlerThread";
	
    private long start = System.currentTimeMillis();
    private AtomicLong count = new AtomicLong(0);
    private AtomicLong mlcount = new AtomicLong(0);
    private AtomicLong mmcount = new AtomicLong(0);
    
    
    private AtomicLong xinjiang = new AtomicLong(0);
    private AtomicLong neimeng = new AtomicLong(0);
    private AtomicLong xizang = new AtomicLong(0);
    
    
    
	public DataPushHandler() {
	}

	@Override
	public void onEvent(WXMessOriginEntity event, long sequence, boolean endOfBatch) throws Exception {
		
		// TODO Auto-generated method stub
		try {
			if (event.getTag() < 0)
				return;
			if (event.getM_type() == 3 || event.getM_type() == 34 || event.getM_type() == 43 || event.getM_type() == 44) {
				send(event, false);
			} else if (event.getM_type() == 1) {
				send(event, true);
			} else {
				LOG.error("unhandle type mess, type:" + event.getM_type() + ", m_content:" + event.getM_content() + 
						", tag:" + event.getM_content());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
	}
	
	private void send(WXMessOriginEntity event, boolean istxt) {
		boolean containsLWS = false;

		if (containsLWS == false) {
			for (long locid : locwl) {
				if (event.getU_loc_province() == locid) {
					containsLWS = true;
					break;
				}
			}
		}
		if (istxt && containsLWS == false) {
			for (String langid : langwl) {
				if (langid.equals(event.getM_language())) {
					containsLWS = true;
					break;
				}
			}
		}
		
		
		if ((event.getU_loc_province() == 650000) || "Uighur".equals(event.getM_language())) {
			xinjiang.incrementAndGet();
			containsLWS = true;
		}
		if ((event.getU_loc_province() == 150000) || "Mongolian".equals(event.getM_language())) {
			neimeng.incrementAndGet();
			containsLWS = true;
		}
		if ((event.getU_loc_province() == 540000) || "Tibetan".equals(event.getM_language())) {
			xizang.incrementAndGet();
			containsLWS = true;
		}
		
		
		count.incrementAndGet();
		if (containsLWS == true) {
			mlcount.incrementAndGet();
			event.setTag(event.getTag() | 64);
//			LOG.debug(event.getM_content());
			if (event.getM_type() != 1) {
				mmcount.incrementAndGet();
			}
			long current = System.currentTimeMillis();
			if ((current - start) > 1000*60) {
				LOG.info("60s mm count :{}; mlcounts :{}; counts :{}", mmcount.get(), mlcount.get(), count.get());
				LOG.info("60s xinjiang counts :{}; neimeng counts :{}; xizang counts :{}", xinjiang.get(), neimeng.get(), xizang.get());
				count.set(0);
				mmcount.set(0);
				mlcount.set(0);
				xinjiang.set(0);
				neimeng.set(0);
				xizang.set(0);
				start = current;
			}
			
		}
		
		
	}

	private static HashSet<Long> locwl = new HashSet<Long>(); 
	public static synchronized void setLocWhiteList(HashSet<Long> locs) {
		locwl.addAll(locs);
	}

	private static HashSet<String> langwl = new HashSet<String>(); 
	public static synchronized void setlangWhiteList(HashSet<String> langs) {
		
		langwl.addAll(langs);
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
	
	
	public static void main(String[] args) {
		int i = 0;
		i|=64;
		System.out.println((i&1)==0);
		System.out.println((i&2)==0);
		System.out.println((i&4)==0);
		System.out.println((i&8)==0);
		System.out.println((i&16)==0);
		System.out.println((i&32)==0);
		System.out.println((i&64)==0);
		i |= 32;
		System.out.println((i&1)==0);
		System.out.println((i&2)==0);
		System.out.println((i&4)==0);
		System.out.println((i&8)==0);
		System.out.println((i&16)==0);
		System.out.println((i&32)==0);
		System.out.println((i&64)==0);
	}

}
