package cn.ac.iie.DataExchange;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iie.di.datadock.rdata.exchange.client.core.REFieldType;
import cn.ac.iie.di.datadock.rdata.exchange.client.exception.REConnectionException;
import cn.ac.iie.di.datadock.rdata.exchange.client.exception.RESessionException;
import cn.ac.iie.di.datadock.rdata.exchange.client.v1.connection.REConnection;
import cn.ac.iie.di.datadock.rdata.exchange.client.v1.session.RESendSession;
import cn.ac.iie.di.datadock.rdata.exchange.client.v1.session.RESendSessionBuilder;

public class CustomizedMQProducer {
	private static final Logger LOG=LoggerFactory.getLogger(CustomizedMQProducer.class);
	RESendSession session = null;

	
	public CustomizedMQProducer(String mqAddress, int mqPort, String topic, String producerGroup, Class _class) throws REConnectionException, RESessionException{
		REConnection conn = new REConnection(mqAddress+":"+mqPort);
		RESendSessionBuilder builder = (RESendSessionBuilder) conn.getSendSessionBuilder(topic);
		Field [] fields=_class.getDeclaredFields();
		
		for(Field f:fields){
			if(f.getType() == int.class){
				builder.addColumn(f.getName(), REFieldType.Int, true);
			}else if(f.getType()==long.class){
				builder.addColumn(f.getName(), REFieldType.Long, true);
			}else if(f.getType()==String.class){
				builder.addColumn(f.getName(), REFieldType.String, true);
			}else{
				throw new RuntimeException("unsupport data type exception");
			}
		}
		
		session = (RESendSession) builder.build();
	}
	
	public void startProducer() throws REConnectionException{
		session.start();
	}
	
	public void stopProducer() throws REConnectionException{
		session.shutdown();
	}
	
	public void setFieldValue(String fieldName,Object value){
		if(value instanceof String){
			try {
				session.setString(fieldName, (String) value);
			} catch (RESessionException e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
			}
		}else if(value instanceof Long){
			try {
				session.setLong(fieldName, (long) value);
			} catch (RESessionException e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
			}
		}else if(value instanceof Integer){
				try {
				session.setInt(fieldName, (int) value);
			} catch (RESessionException e) {
				e.printStackTrace();
				LOG.error(e.getMessage());
			}		
		}else{
			LOG.error("should not be here, fieldName:{} ",fieldName);
			LOG.error("should not be here, value:{}",value.toString());
			LOG.error("should not be here, valuetype:{}",value.getClass().getName());
		}
	}
	
	
	public void add(){
		try {
			session.add();
		} catch (RESessionException e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
		}
	}
	
	public void flush(){
		try{
			session.flush();
		} catch (REConnectionException|RESessionException e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
		}
	}
}
