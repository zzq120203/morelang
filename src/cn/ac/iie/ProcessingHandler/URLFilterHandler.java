package cn.ac.iie.ProcessingHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;

import cn.ac.iie.Util.UrlExtract;
import cn.ac.iie.entity.URLInfoEntity;
import cn.ac.iie.entity.WXMessOriginEntity;

public class URLFilterHandler implements EventHandler<WXMessOriginEntity>, LifecycleAware {

	private static Logger LOG = LoggerFactory.getLogger(URLFilterHandler.class);
    private RingBuffer<URLInfoEntity> ringBuffer;
	
	private UrlExtract ue = new UrlExtract();
    
    private String oldName;
    private final String name="URLFilterHandlerThread";
    
	public URLFilterHandler(RingBuffer<URLInfoEntity> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}


	@Override
	public void onEvent(WXMessOriginEntity event, long sequence, boolean endOfBatch) throws Exception {
		
		try {
			if (event.getTag() < 0 || ((event.getTag() & 32) != 0))
				return;
			if (event.getM_type() == 3 || event.getM_type() == 34 || event.getM_type() == 43 || event.getM_type() == 44) {
				return;
			} else if (event.getM_type() == 1) {
				if (urlWhiteKeywordMapUpdate.get()) {
				urlWhiteKeywordMap.clear();
				urlWhiteKeywordMap = tmpUrlWhiteKeywordMap;
				urlWhiteKeywordMapUpdate.set(false);
			}
			// skip URL extracting
			boolean containsKWL = false;
			
			for (String st : urlWhiteKeywordMap.keySet()) {
				if (event.getM_content().indexOf(st)>=0) {
					containsKWL = true;
					break;
				}
			}
			
			if (containsKWL == false) {
//				LOG.debug(event.getM_content());
				HashSet<String> list = ue.extract(event.getM_content());
				if (list.size() > 0/** && list.size() < 5**/) {
					int url_counter = 0;
					for (String url : list) {
						if(url.isEmpty()){
							continue;
						}
						long seq = 0;
						try {
							seq = ringBuffer.next();
							URLInfoEntity raw = ringBuffer.get(seq);
							raw.setG_id(event.getG_id());
							raw.setUrl(url);
							raw.setU_ch_id((event.getU_ch_id()));
							raw.setM_chat_room((event.getM_chat_room()));
							raw.setM_ch_id(event.getM_ch_id());
							raw.setM_publish_time(event.getM_publish_time());
							raw.setM_content(event.getM_content());
							raw.setU_name(event.getU_name());
							raw.setU_send_ip(event.getU_send_ip());
							raw.setU_loc_province(event.getU_loc_province());
							raw.setU_loc_county(event.getU_loc_county());
							raw.setM_dom_for(event.getM_dom_for());
							raw.setM_country_code(event.getM_country_code());
							url_counter++;
						} finally {
							ringBuffer.publish(seq);
						}
					}
					event.setM_url_count(url_counter);
				}
			}
			} else {
				LOG.error("unhandle type mess, type:" + event.getM_type() + ", m_content:" + event.getM_content() + 
						", tag:" + event.getM_content());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
		
	}
	
	private static Map<String, byte[]> tmpUrlWhiteKeywordMap = new HashMap<String, byte[]>();
	private static Map<String, byte[]> urlWhiteKeywordMap = new HashMap<String, byte[]>();

	private static AtomicBoolean urlWhiteKeywordMapUpdate = new AtomicBoolean(false);

	public static synchronized void setURLWLMap(Map<String, byte[]> map) {
		tmpUrlWhiteKeywordMap = map;
		urlWhiteKeywordMapUpdate.set(true);
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
