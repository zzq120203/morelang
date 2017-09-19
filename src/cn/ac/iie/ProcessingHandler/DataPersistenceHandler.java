package cn.ac.iie.ProcessingHandler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iie.DataExchange.MPPDataLoading;
import cn.ac.iie.entity.WXMessOriginEntity;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

public class DataPersistenceHandler implements EventHandler<WXMessOriginEntity>,LifecycleAware {
	private static final Logger LOG=LoggerFactory.getLogger(DataPersistenceHandler.class);

    private String oldName;
    private String name="DataPersistenceThread";
	
	public DataPersistenceHandler(){}
	
	public DataPersistenceHandler(String dataCenter,String connAddress,int port,String userName,
			String password,String keySpace){
		//MPPDataLoading.init(dataCenter, connAddress, port, userName, password, keySpace);
	}
	
	@Override
	public void onEvent(WXMessOriginEntity event, long sequence, boolean endOfBatch) throws Exception {
		try{
			if (event.getTag() < 0) {
				return;
			}
			if ((event.getTag() & 64) != 0) {
				MPPDataLoading.ObjectPersistence(event);
			}
		} catch(Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
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
