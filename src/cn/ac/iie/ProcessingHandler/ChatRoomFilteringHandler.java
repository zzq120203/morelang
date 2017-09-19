package cn.ac.iie.ProcessingHandler;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

import cn.ac.iie.entity.WXMessOriginEntity;

public class ChatRoomFilteringHandler implements EventHandler<WXMessOriginEntity>, LifecycleAware {
	private static final Logger LOG = LoggerFactory.getLogger(ChatRoomFilteringHandler.class);
	@SuppressWarnings("unchecked")
	public static Map<Long,Map<Long,byte[]>>[] chatRoomIdToZhuti = (Map<Long, Map<Long,byte[]>>[]) new Map[2];
	private static AtomicInteger idx = new AtomicInteger(0);
	
	 private String oldName;
	 private String name = "ChatRoomFilterThread";

	@SuppressWarnings("unchecked")
	public static Map<Long,Byte>[] chatRoomId_whiteList = (Map<Long, Byte>[]) new Map[2];
	private static AtomicInteger wlIdx = new AtomicInteger(0);

	@Override
	public void onEvent(WXMessOriginEntity event, long sequence, boolean endOfBatch) throws Exception {
		try {
			if (event.getTag() < 0 || (event.getTag() & 64) == 0)
				return;
			
			if(chatRoomId_whiteList[wlIdx.get()].containsKey(event.getM_chat_room())){
				event.setTag(event.getTag() | 32);
				return;
			}
			
			if (chatRoomIdToZhuti[idx.get()].containsKey(event.getM_chat_room())) {
				event.setTag(event.getTag() | 1);
				for(Map.Entry<Long, byte[]>en:chatRoomIdToZhuti[idx.get()].get(event.getM_chat_room()).entrySet()){
					event.addThemes(en.getKey());
					LOG.info("targeted chatroom:" + event.getM_chat_room() + " zhuti:" + en.getKey() + 
							", tag:" + event.getTag() + " g_id:" + event.getG_id());
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
	}
	
	public static synchronized void setCHATROOMIDMap(Map<Long,Map<Long,byte[]>> map){
		chatRoomIdToZhuti[(idx.get() + 1) % 2] = map;
		
		idx.set((idx.get() + 1) % 2);
		if(chatRoomIdToZhuti[(idx.get() + 1) % 2] != null){
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			chatRoomIdToZhuti[(idx.get() + 1) % 2].clear();
		}
	}
	
	public static synchronized void setCHATROOM_WHITELIST(Map<Long,Byte> map){
		chatRoomId_whiteList[(wlIdx.get() + 1) % 2] = map;
		
		wlIdx.set((wlIdx.get() + 1) % 2);
		if(chatRoomId_whiteList[(wlIdx.get() + 1) % 2] != null){
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			chatRoomId_whiteList[(wlIdx.get() + 1) % 2].clear();
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
