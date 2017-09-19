package cn.ac.iie.ProcessingHandler;

import cn.ac.iie.DataExchange.MQProducerWrapper;
import cn.ac.iie.entity.URLInfoEntity;

import java.util.concurrent.atomic.AtomicLong;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.WorkHandler;

public class URLPubishHandler implements WorkHandler<URLInfoEntity>, LifecycleAware {
	private final static Logger LOG = LoggerFactory.getLogger(URLPubishHandler.class);
	private String topic;
	private static MQProducerWrapper producer = null;
	private ObjectMapper om = new ObjectMapper();

	private String oldName;
	private final String name = "URLPublisherThread";
	
	public URLPubishHandler(String mqAddress, int mqPort, String produerrGroup, String topic) {
		if (producer == null) {
			producer = new MQProducerWrapper(mqAddress, mqPort, produerrGroup);
			producer.startProducer();
		}
		this.topic = topic;
	}

	@Override
	public void onEvent(URLInfoEntity event) throws Exception {

		try {
			String urlValue = om.writeValueAsString(event);
			producer.sendMess(topic, urlValue);
//			LOG.info(urlValue);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void stopProducer() {
		if (producer != null)
			producer.stopProducer();
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
