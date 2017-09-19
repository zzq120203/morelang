package cn.ac.iie.DataExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iie.Confguration.CassandraConnConfig;
import cn.ac.iie.Confguration.Config;
import cn.ac.iie.entity.WXMessOriginEntity;
import cn.ac.iie.Util.Tools;

public class MPPDataLoading {
	private static final Logger LOG=LoggerFactory.getLogger(MPPDataLoading.class);
	private static CassandraDriverWrapper casAllMessHelper;
	private static CassandraDriverWrapper casOtherMessHelper;
	private static List<Map<String,Object>> list=null;
	private static List<Map<String,Object>> targetList=null;
	
	private static int batchSize=40;
	public static AtomicBoolean receiveStopSig=new AtomicBoolean(false);
	
	public static void init(CassandraConnConfig allMessConnConfi, CassandraConnConfig otherMessConnConfig){
		list=new ArrayList<Map<String,Object>>();
		targetList=new ArrayList<Map<String,Object>>();
		casAllMessHelper=new CassandraDriverWrapper(allMessConnConfi);
		casOtherMessHelper=new CassandraDriverWrapper(otherMessConnConfig);
		
		casAllMessHelper.PrepareInsert(Config.TableNameOfAllMess);
		casOtherMessHelper.PrepareInsert(Config.TableNameOfTargetedMess);
//		casOtherMessHelper.PrepareInsert(Config.TableNameOfBoCe);
	}
	
	public static void ObjectPersistenceSingle(WXMessOriginEntity entity){
		if(entity.getTag()==-2){
			receiveStopSig.set(true);
			LOG.info("receiving stop signal");
			return;
		}
		if(entity.getTag()==-1){
			return;
		}
		
		//test code , getTag must not be equal to 826,
		//so always return
		if(entity.getTag()!=826){
			//return;
		}
		
		
		Map<String,Object> m=writeValuesAsMap(entity);
		casAllMessHelper.insertion(Config.TableNameOfAllMess, m);
		
		if((entity.getTag()&32)!=0){
			m.put("mih", 0);
			m.put("midi", 0);
			m.put("usih", -1);
			m.put("usidi", -1);
			m.put("mhu", 0);
			m.put("mdu", 0);

			casOtherMessHelper.insertion(Config.TableNameOfBoCe, m);
		}else if((entity.getTag()&7)!=0){
			m.put("mih", 0);
			m.put("midi", 0);
			m.put("usih", -1);
			m.put("usidi", -1);
			m.put("mhu", 0);
			m.put("mdu", 0);
			m.put("mrc", entity.getM_rules_list());
			if(entity.getM_type()==1){
				m.put("simha", Tools.genSimHashCode(entity.getM_content()));
			}
			/*try {
				LOG.info(om.writeValueAsString(m));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}*/
			casOtherMessHelper.insertion(Config.TableNameOfTargetedMess, m);
		}
	}
	
	
	public static void ObjectPersistence(WXMessOriginEntity entity){

		if(entity.getTag()<0){
			if(list.size()>0){
				casAllMessHelper.insertionBatchAsync(Config.TableNameOfAllMess, list);
				list.clear();
			}
			if(targetList.size()>0){
				casOtherMessHelper.insertionBatchAsync(Config.TableNameOfTargetedMess, targetList);
				targetList.clear();
			}
			if(entity.getTag()==-2){
				receiveStopSig.set(true);
				LOG.info("receiving stop signal");
			}
			return;
		}
		

		Map<String,Object> ma=writeValuesAsMap(entity);
		//boce 
		if((entity.getTag()&32)!=0){
			ma.put("mih", 0);
			ma.put("midi", 0);
			ma.put("usih", -1);
			ma.put("usidi", -1);
			ma.put("mhu", 0);
			ma.put("mdu", 0);
			casOtherMessHelper.insertion(Config.TableNameOfBoCe, ma);
			return;
		}

		//all
		{
//			if (rd.nextInt(clusterSize + secClusterSize) < clusterSize) {
			//if(entity.getM_type()==1){
//			if((entity.getTag()|8)==0 && entity.getM_type()==1){
				list.add(ma);
				if (list.size() >= batchSize) {
					casAllMessHelper.insertionBatchAsync(Config.TableNameOfAllMess, list);
					list.clear();
				}
			//}
		}

		//targeted message
		if((entity.getTag()&7)!=0){
			Map<String,Object> mt=writeValuesAsMap(entity);
			
			mt.put("mih", 0);
			mt.put("midi", 0);
			mt.put("usih", -1);
			mt.put("usidi", -1);
			mt.put("mhu", 0);
			mt.put("mdu", 0);
			mt.put("mrc", entity.getM_rules_list().size());
			if(entity.getM_type()==1){
				mt.put("simha", Tools.genSimHashCode(entity.getM_content()));
			}

			
			targetList.add(mt);
			if(targetList.size()>batchSize){
				casOtherMessHelper.insertionBatchAsync(Config.TableNameOfTargetedMess, targetList);
				targetList.clear();
			}
		}
	}
	
	@SuppressWarnings("serial")
	private static Map<String,Object> writeValuesAsMap(final WXMessOriginEntity e){
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("id",e.getG_id());
		map.put("pt",e.getM_publish_time());
		map.put("it",e.getM_insert_time());
		map.put("mcr",e.getM_chat_room());
		map.put("mid",e.getM_ch_id());
		map.put("type",e.getM_type());
		map.put("cont",e.getM_content());
		map.put("lang",e.getM_language());
		map.put("ugid",e.getU_g_ch_key());
		map.put("uwxid",e.getU_name());
		map.put("uchid",e.getU_ch_id());
		map.put("usip",e.getU_send_ip());
		map.put("ulc",e.getU_loc_county());
		map.put("ulp",e.getU_loc_province());
		map.put("mmid",e.getM_mm_id());
		map.put("mmf", new ArrayList<String>(){
			{
				addAll((e.getM_mm_feature()));
			}
		});	
		map.put("mthli",new ArrayList<Long>(){
			{
				addAll(e.getM_themes_list());
			}
		});
		map.put("mtoli",new ArrayList<Long>(){
			{
				addAll(e.getM_topics_list());
			}
		});
		map.put("mrl",new ArrayList<Long>(){
			{
				addAll(e.getM_rules_list());
			}
		});
		map.put("year", e.getM_year());
		map.put("mon", e.getM_month());
		map.put("day", e.getM_day());
		map.put("hr",e.getM_hour());
		map.put("mdf", e.getM_dom_for());
		map.put("mccode", e.getM_country_code());
		map.put("murl", e.getM_mm_url());
		map.put("murlc", e.getM_url_count());

		/*
		 * 		map.put("g_id",e.getG_id());
		map.put("g_channel",e.getG_channel());
		map.put("g_spec",e.getG_spec());
		map.put("g_ch_key",e.getG_ch_key());
		map.put("g_asp",e.getG_asp());
		map.put("g_adp",e.getG_adp());
		map.put("m_publish_time",e.getM_publish_time());
		map.put("m_insert_time",e.getM_insert_time());
		map.put("m_chat_room",e.getM_chat_room());
		map.put("m_ch_id",e.getM_ch_id());
		map.put("m_type",e.getM_type());
		map.put("m_content",e.getM_content());
		map.put("m_language",e.getM_language());
		map.put("m_board_name",e.getM_board_name());
		map.put("u_g_ch_key",e.getU_g_ch_key());
		map.put("u_name",e.getU_name());
		map.put("u_ch_id",e.getU_ch_id());
		map.put("u_send_ip",e.getU_send_ip());
		map.put("u_loc_county",e.getU_loc_county());
		map.put("u_loc_province",e.getU_loc_province());
		map.put("m_mm_id",e.getM_mm_id());
		map.put("m_mm_feature", e.getM_mm_feature());	
		map.put("m_themes_list",e.getM_themes_list());
		map.put("m_topics_list",e.getM_topics_list());
		map.put("m_rules_list",e.getM_rules_list());
		map.put("m_msg_cnt", e.getM_msg_cnt());
		map.put("m_year", e.getM_year());
		map.put("m_month", e.getM_month());
		map.put("m_day", e.getM_day());
		map.put("m_hour",e.getM_hour());
		 */
		
		return map;
	}
}
