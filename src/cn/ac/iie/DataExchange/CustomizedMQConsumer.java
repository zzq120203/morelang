package cn.ac.iie.DataExchange;

import cn.ac.iie.Util.Tools;
import cn.ac.iie.di.datadock.rdata.exchange.client.core.session.receive.REAbstractReceiveMessageHandler;
import cn.ac.iie.di.datadock.rdata.exchange.client.exception.REConnectionException;
import cn.ac.iie.di.datadock.rdata.exchange.client.v1.ConsumePosition;
import cn.ac.iie.di.datadock.rdata.exchange.client.v1.REMessageExt;
import cn.ac.iie.di.datadock.rdata.exchange.client.v1.connection.REConnection;
import cn.ac.iie.di.datadock.rdata.exchange.client.v1.session.FormattedHandler;
import cn.ac.iie.di.datadock.rdata.exchange.client.v1.session.REReceiveSession;
import cn.ac.iie.di.datadock.rdata.exchange.client.v1.session.REReceiveSessionBuilder;
import cn.ac.iie.entity.WXMessOriginEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.RingBuffer;

public class CustomizedMQConsumer implements Runnable {

	private final static Logger LOG = LoggerFactory.getLogger(CustomizedMQConsumer.class);
	private REReceiveSession receiver = null;
	private RingBuffer<WXMessOriginEntity> ringBuffer;
	public static AtomicBoolean stopSignalSend = new AtomicBoolean(false);
	private static AtomicLong lastUpdateTime = new AtomicLong(0);

	public CustomizedMQConsumer(String mqAddr, int mqPort, String consumerGroup, String topic, RingBuffer<WXMessOriginEntity> ringBuffer, int threadNr)
			throws REConnectionException {
		REConnection conn = new REConnection(mqAddr + ":" + mqPort);
		REReceiveSessionBuilder builder = (REReceiveSessionBuilder) conn.getReceiveSessionBuilder(topic);
		builder.setGroupName(consumerGroup);
		builder.setConsumPosition(ConsumePosition.CONSUME_FROM_LAST_OFFSET);
		builder.setConsumeThreadNum(threadNr);
		builder.setFailureHandler(new REAbstractReceiveMessageHandler<byte[]>() {
			@Override
			public boolean handle(byte[] message) {
				LOG.error(new String(message));
				return true;
			}
		});

		builder.setHandler(new MessageConsumingHandler(ringBuffer, nameToType, nameToGetMethod, nameToSetMethod));

		receiver = (REReceiveSession) builder.build();
		this.ringBuffer = ringBuffer;
		refectionPrepare();
	}

	public void startConsumer() {
		try {
			receiver.start();
		} catch (REConnectionException e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
		}
	}

	public void stopConsumer() {
		try {
			receiver.closeGracefully();
			receiver.shutdown();
			LOG.info("shuting down consumer!");
		} catch (REConnectionException e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
		}
	}

	private Map<String, Class<?>> nameToType = new HashMap<String, Class<?>>();
	private Map<String, Method> nameToGetMethod = new HashMap<String, Method>();
	private Map<String, Method> nameToSetMethod = new HashMap<String, Method>();
	private Map<String, byte[]> excludeFileName = new HashMap<String, byte[]>() {
		{
			put("tag", new byte[0]);
			put("LOG", new byte[0]);
			put("m_insert_time", new byte[0]);
			put("m_mm_feature", new byte[0]);
			put("m_themes_list", new byte[0]);
			put("m_topics_list", new byte[0]);
			put("m_rules_list", new byte[0]);
			put("m_url_count", new byte[0]);
		}
	};

	private void refectionPrepare() {
		Class<WXMessOriginEntity> cl = WXMessOriginEntity.class;
		Field[] fields = cl.getDeclaredFields();
		for (Field f : fields) {
			String fieldName = f.getName();
			if (excludeFileName.containsKey(fieldName))
				continue;

			String capFieldName = Tools.convertFirstCapital(fieldName);
			nameToType.put(fieldName, f.getType());
			try {
				nameToGetMethod.put(fieldName, cl.getDeclaredMethod("get" + capFieldName, new Class[0]));
				nameToSetMethod.put(fieldName, cl.getDeclaredMethod("set" + capFieldName, f.getType()));
			} catch (Exception ex) {
				LOG.error(ex.getMessage());
			}
		}
	}

	public static class MessageConsumingHandler extends FormattedHandler {
		RingBuffer<WXMessOriginEntity> ringBuffer = null;
		Map<String, Class<?>> nameToType = null;
		Map<String, Method> nameToGetMethod = null;
		Map<String, Method> nameToSetMethod = null;

		public MessageConsumingHandler(RingBuffer<WXMessOriginEntity> ringBuffer, Map<String, Class<?>> nameToType,
				Map<String, Method> nameToGetMethod, Map<String, Method> nameToSetMethod) {
			this.ringBuffer = ringBuffer;
			this.nameToType = nameToType;
			this.nameToGetMethod = nameToGetMethod;
			this.nameToSetMethod = nameToSetMethod;
		}

		@Override
		public boolean handle(REMessageExt messageExt) {
			Iterator<REMessageExt.Record> itr = messageExt.getRecordIterator();
			long seq = 0;
			WXMessOriginEntity mess;
			while (itr.hasNext()) {
				try {
					seq = ringBuffer.next();
					mess = ringBuffer.get(seq);
					mess.resetAllFields();
					REMessageExt.Record rec = itr.next();

					for (Map.Entry<String, Class<?>> en : nameToType.entrySet()) {
						try {
							Method setMethod = nameToSetMethod.get(en.getKey());

							if (en.getValue() == int.class) {
								setMethod.invoke(mess, rec.getInt(en.getKey()));
							} else if (en.getValue() == long.class) {
								setMethod.invoke(mess, rec.getLong(en.getKey()));
							} else if (en.getValue() == String.class) {
								setMethod.invoke(mess, rec.getString(en.getKey()));
								// } else if (en.getValue() == List.class) {
								// setMethod.invoke(mess,
								// rec.getLongs(en.getKey()));
							} else {
								throw new RuntimeException("unrecgonized type:" + en.getValue() + " fieldname:" + en.getKey());
							}
						} catch (Exception ex) {
							ex.printStackTrace();
							LOG.error(ex.getMessage());
							LOG.error("file name:{}", en.getKey());
						}
					}
					mess.setM_insert_time(System.currentTimeMillis() / 1000);
					if (rec.getInt("m_type") != 1) {
						if (rec.getStrings("m_mm_feature") != null && rec.getStrings("m_mm_feature").size() > 0) {
							List<String> list = new ArrayList<String>();
							for (String st : rec.getStrings("m_mm_feature"))
								list.add(st);
							mess.setM_mm_feature(list);
						}

						if (rec.getLongs("m_themes_list") != null && rec.getLongs("m_themes_list").size() > 0) {
							List<Long> list = new ArrayList<Long>();
							for (Long l : rec.getLongs("m_themes_list")) {
								list.add(l);
							}
							mess.setM_themes_list(list);
						}

						if (rec.getLongs("m_topics_list") != null && rec.getLongs("m_topics_list").size() > 0) {
							List<Long> list = new ArrayList<Long>();
							for (Long l : rec.getLongs("m_topics_list")) {
								list.add(l);
							}
							mess.setM_topics_list(list);
						}

						if (rec.getLongs("m_rules_list") != null && rec.getLongs("m_rules_list").size() > 0) {
							List<Long> list = new ArrayList<Long>();
							for (Long l : rec.getLongs("m_rules_list")) {
								list.add(l);
							}
							mess.setM_rules_list(list);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					LOG.error(ex.getMessage());
				} finally {
					ringBuffer.publish(seq);
					lastUpdateTime.set(System.currentTimeMillis());
				}
			}
			return true;
		}
	}

	@Override
	public void run() {
		int counter = 10;
		LOG.info("BeatingThead starting");
		while (true) {
			try {
				if (System.currentTimeMillis() - lastUpdateTime.get() > 2000) {
					if (stopSignalSend.get() == false) {
						long sequence = 0;
						try {
							sequence = ringBuffer.next();
							WXMessOriginEntity raw = ringBuffer.get(sequence);
							raw.setTag(-1);
						} finally {
							ringBuffer.publish(sequence);
						}
						// LOG.info("sending flush record");
					} else {
						while (counter-- > 0) {
							if (counter == 9) {
								LOG.info("sending -2 flag");
							}
							long sequence = ringBuffer.next();
							WXMessOriginEntity raw = ringBuffer.get(sequence);
							raw.setTag(-2);
							ringBuffer.publish(sequence);
						}
						break;
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (Exception ex) {
				LOG.error(ex.getMessage());
			}
		}
	}
}
