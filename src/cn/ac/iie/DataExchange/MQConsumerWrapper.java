package cn.ac.iie.DataExchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.lmax.disruptor.RingBuffer;

import cn.ac.iie.entity.WXMessOriginEntity;


public class MQConsumerWrapper {
	private final static Logger LOG=LoggerFactory.getLogger(MQConsumerWrapper.class);
	private DefaultMQPushConsumer consumer=null;
	
	/**
	 * @param mqAddr node ip of mq cluster
	 * @param mqPort port of mq service
	 * @param consumerGroup consumer group
	 * @param topic 
	 * @param messageQueue buffer that holds messages receiving from mq
	 * @throws MQClientException
	 */
	public MQConsumerWrapper(String mqAddr, int mqPort, String consumerGroup, String topic, RingBuffer<WXMessOriginEntity> ringBuffer) throws MQClientException {
		consumer = new DefaultMQPushConsumer();
		consumer.setConsumerGroup(consumerGroup);
		consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
		consumer.setNamesrvAddr(mqAddr + ":" + mqPort);
		consumer.subscribe(topic, "*");
		consumer.registerMessageListener(new MessageListenerConcurrentlyImp(ringBuffer));
		consumer.setMessageModel(MessageModel.CLUSTERING);
	}
	
	public void startConsumer(){
		try {
			consumer.start();
		} catch (MQClientException e) {
			LOG.error(e.getMessage());
		}
	}
	
	public void stopConsumer(){
		LOG.info("shuting down consumer!!");
		consumer.shutdown();
	}
}

