package cn.ac.iie.ProcessingHandler;

import cn.ac.iie.DataExchange.CustomizedMQProducer;
import cn.ac.iie.DataExchange.MQProducerWrapper;
import cn.ac.iie.Service.MoreLanguage;
import cn.ac.iie.entity.MMURLEntity;
import cn.ac.iie.di.datadock.rdata.exchange.client.exception.REConnectionException;
import cn.ac.iie.di.datadock.rdata.exchange.client.exception.RESessionException;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

public class MMURLPubishHandler implements EventHandler<MMURLEntity>, LifecycleAware {
	private final static Logger LOG = LoggerFactory.getLogger(MMURLPubishHandler.class);
	private String topic;
	private static MQProducerWrapper producer = null;
	private static CustomizedMQProducer custProducer = null;
	private ObjectMapper om = new ObjectMapper();

	private String oldName;
	private final String name = "MMURLPublisherThread";

	public MMURLPubishHandler(String mqAddress, int mqPort, String produerrGroup, String topic) {
		if (MoreLanguage.fetchFromTestMQ) {
			producer = new MQProducerWrapper(mqAddress, mqPort, produerrGroup);
			this.topic = topic;
			producer.startProducer();
		} else {
			try {
				custProducer = new CustomizedMQProducer(mqAddress, mqPort, topic, produerrGroup, MMURLEntity.class);
				custProducer.startProducer();
			} catch (REConnectionException e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
			} catch (RESessionException e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
			}
		}
	}

	@Override
	public void onEvent(MMURLEntity event, long sequence, boolean endOfBatch) throws Exception {
		try {
			if (MoreLanguage.fetchFromTestMQ) {
				String urlValue = om.writeValueAsString(event);
				producer.sendMess(topic, urlValue);
			} else {
				custProducer.setFieldValue("g_id", event.getG_id());
				custProducer.setFieldValue("m_mm_url", event.getM_mm_url());
				custProducer.add();
				custProducer.flush();
			}
			// LOG.info(urlValue);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	public static void stopProducer() {
		if (producer != null)
			producer.stopProducer();
		if (custProducer != null)
			try {
				custProducer.stopProducer();
			} catch (REConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
