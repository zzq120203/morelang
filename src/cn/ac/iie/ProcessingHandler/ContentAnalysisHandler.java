package cn.ac.iie.ProcessingHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iie.entity.WXMessOriginEntity;
import cn.ac.iie.DS.LogicSyntaxTree;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

public class ContentAnalysisHandler implements EventHandler<WXMessOriginEntity>,LifecycleAware {
	private static Logger LOG = LoggerFactory.getLogger(ContentAnalysisHandler.class);
    private String oldName;
    private String name = "ContentAnalysisThread";

    public ContentAnalysisHandler() {
	}

	@Override
	public void onEvent(WXMessOriginEntity event, long sequence, boolean endOfBatch) throws Exception {
		try {
			if (event.getTag() < 0 || ((event.getTag() & 32) != 0) || (event.getTag() & 64) == 0)
				return;

			if (event.getM_type() == 3 || event.getM_type() == 34 || event.getM_type() == 43 || event.getM_type() == 44) {
				multiMediaMessLabling(event);
			} else if (event.getM_type() == 1) {

				if (updatedMap.get()) {
					synchronized (lockOb) {
						LST.clear();
						LST = tmp;

						ruleIdToZhutiid.clear();
						ruleIdToZhutiid = tmpRIDToZhutiid;
						ruleIdToZhuantiid.clear();
						ruleIdToZhuantiid = tmpRIDToZhuantiid;

						updatedMap.set(false);
					}
				}

				List<Long> u_list = new ArrayList<Long>();
				Map<Long, Byte> zhutiMap = new HashMap<Long, Byte>();
				Map<Long, Byte> zhuantiMap = new HashMap<Long, Byte>();

				for (Map.Entry<Long, LogicSyntaxTree> en : LST.entrySet()) {
					if (en.getValue().containsVerify(event.getM_content())) {
						LOG.info("targeted Rid: " + en.getKey() + ", uid: " + event.getU_ch_id() + ", content: " + event.getM_content());
						u_list.add(en.getKey());
						event.setTag(event.getTag() | 4);
						if (ruleIdToZhuantiid.containsKey(en.getKey())) {
							for (Map.Entry<Long, byte[]> ent : ruleIdToZhuantiid.get(en.getKey()).entrySet()) {
								zhuantiMap.put(ent.getKey(), (byte)0);
							}
						}
						if (ruleIdToZhutiid.containsKey(en.getKey())) {
							for (Map.Entry<Long, byte[]> ent : ruleIdToZhutiid.get(en.getKey()).entrySet()) {
								zhutiMap.put(ent.getKey(), (byte)0);
							}
						}
					}
				}

				if (u_list.size() > 0) {
					event.addRulesList(u_list);
					List<Long> tpList = new ArrayList<Long>();
					for (Map.Entry<Long, Byte> en : zhuantiMap.entrySet()) {
						tpList.add(en.getKey());
					}
					event.addTopicsList(tpList);

					List<Long> tList = new ArrayList<Long>();
					for (Map.Entry<Long, Byte> en : zhutiMap.entrySet()) {
						tList.add(en.getKey());
					}
					event.addThemesList(tList);
				}
			} else {
				LOG.error("unhandle type mess, type:" + event.getM_type() + ", m_content:" + event.getM_content() + 
						", tag:" + event.getM_content());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	private void multiMediaMessLabling(WXMessOriginEntity event) {
		event.setTag(event.getTag() | 8);
	}

	private Map<Long, LogicSyntaxTree> LST = new HashMap<Long, LogicSyntaxTree>();
	private static Map<Long, LogicSyntaxTree> tmp = null;

	private Map<Long, Map<Long,byte[]>> ruleIdToZhutiid = new HashMap<Long, Map<Long,byte[]>>();
	private Map<Long, Map<Long,byte[]>> ruleIdToZhuantiid = new HashMap<Long, Map<Long,byte[]>>();

	private static Map<Long, Map<Long,byte[]>> tmpRIDToZhutiid = null;
	private static Map<Long, Map<Long,byte[]>> tmpRIDToZhuantiid = null;

	private static AtomicBoolean updatedMap = new AtomicBoolean(false);

	private static byte[] lockOb = new byte[0];

	public static void setLSTMap(Map<Long, LogicSyntaxTree> tmpMap, Map<Long, Map<Long,byte[]>> ruleIdToZhutiid, 
			Map<Long, Map<Long,byte[]>> ruleIdToZhuantiid) {
		synchronized (lockOb) {
			tmp = tmpMap;
			tmpRIDToZhuantiid = ruleIdToZhuantiid;
			tmpRIDToZhutiid = ruleIdToZhutiid;
			updatedMap.set(true);
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
