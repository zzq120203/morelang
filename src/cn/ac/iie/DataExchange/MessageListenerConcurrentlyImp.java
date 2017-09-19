package cn.ac.iie.DataExchange;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.lmax.disruptor.RingBuffer;

import cn.ac.iie.entity.WXChatroomMessEntity;
import cn.ac.iie.entity.WXMessOriginEntity;
/**
 * @author zhangjc
 *
 */
public class MessageListenerConcurrentlyImp implements Runnable, MessageListenerConcurrently {

	private RingBuffer<WXMessOriginEntity> ringBuffer;
	private static final Logger LOG = LoggerFactory.getLogger(MessageListenerConcurrentlyImp.class);
	private AtomicLong lastUpdateTime = new AtomicLong(0);
	public static AtomicBoolean stopSignalSend = new AtomicBoolean(false);
	private	ObjectMapper om = new XmlMapper();

	public static List<Integer> pro = new ArrayList<Integer>() {
		{
			add(650000);// xijiang
			add(530000); // yunnan
		}
	};

	public static List<List<Integer>> city = new ArrayList<List<Integer>>() {
		{
			add(new ArrayList<Integer>() {
				{
					add(652900);
					add(654004);
					add(650100);
				}
			});

			add(new ArrayList<Integer>() {
				{
					add(530400);
					add(530100);
					add(530600);
				}
			});
		}
	};

	public static Random rand = new Random();

	public MessageListenerConcurrentlyImp(RingBuffer<WXMessOriginEntity> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	@SuppressWarnings("unchecked")
	public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

		for (int i = 0; i < msgs.size(); ++i) {
			MessageExt msg = msgs.get(i);
			String body = null;
			try {
				body = new String(msg.getBody(), "UTF-8");
				// LOG.info(body);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
			}
			long sequence = 0;
			try {
				WXChatroomMessEntity entity = om.readValue(body, WXChatroomMessEntity.class);
				try {
					sequence = ringBuffer.next();
					WXMessOriginEntity raw = ringBuffer.get(sequence);
					raw.resetAllFields();
					raw.setM_chat_room(entity.getChatroom());
					raw.setU_send_ip(entity.getClientIP());
					raw.setM_content(entity.getContent());
					raw.setU_name(entity.getFrom());
					raw.setM_publish_time(System.currentTimeMillis() / 1000);
					// raw.setM_publish_time(entity.getFromTime());
					raw.setM_mm_id(entity.getMediaId());
					raw.setU_ch_id((entity.getFromUin()));
					raw.setM_type(entity.getType());
					raw.setM_ch_id(entity.getMsgID());
					raw.setTag(0);
					raw.setM_country_code(150);
					raw.setM_dom_for(1);
					int pr = Math.abs(rand.nextInt()) % pro.size();
					raw.setU_loc_province(pro.get(pr));
					raw.setU_loc_county(city.get(pr).get(Math.abs(rand.nextInt()) % city.size()));
				} finally {
					ringBuffer.publish(sequence);
				}
			} catch (com.fasterxml.jackson.core.JsonParseException e) {
				// e.printStackTrace();
				// LOG.error("Data:"+body);
			} catch (com.fasterxml.jackson.databind.JsonMappingException e) {
				e.printStackTrace();
				// LOG.error("Data:"+body);
			} catch (IOException e) {
				// e.printStackTrace();
				// LOG.error("Data:"+body);
			} finally {
			}
		}
		lastUpdateTime.set(System.currentTimeMillis());

		return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
	}

	public void run() {
		int counter = 10;
		while (true) {
			if (System.currentTimeMillis() - lastUpdateTime.get() > 2000) {
				if (stopSignalSend.get() == false) {
					  long sequence = ringBuffer.next();
					  WXMessOriginEntity raw = ringBuffer.get(sequence); raw.setTag(-1);
					  ringBuffer.publish(sequence);
					// LOG.info("sending flush record");
				} else {
					while (counter-- > 0) {
						long sequence = ringBuffer.next();
						try {
							WXMessOriginEntity raw = ringBuffer.get(sequence);
							raw.setTag(-2);
						} finally {
							ringBuffer.publish(sequence);
							LOG.info("sending -2 flag");
						}
					}
					break;
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
