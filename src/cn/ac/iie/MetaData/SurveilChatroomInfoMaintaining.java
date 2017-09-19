package cn.ac.iie.MetaData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iie.Confguration.Config;
import cn.ac.iie.ProcessingHandler.ChatRoomFilteringHandler;
import iie.mm.dao.ConnectionPoolManager;

public class SurveilChatroomInfoMaintaining implements Runnable {
	private final static Logger LOG=LoggerFactory.getLogger(SurveilChatroomInfoMaintaining.class);
	
	private long lastMaxTimeStamp=0L;
	private long lastRecordCounter=0L;
	private static AtomicBoolean init=new AtomicBoolean(false);
	
	private String getAll="select t_g.m_chat_room_id as chid,t_g.c_create_time as ct, t_th.t_id as tid from t_keypoint_group t_g, "+
	"t_theme t_th where t_th.t_id=t_g.cc_id and t_th.t_type=2 and  kg_status>0";

	private String checkUpdate="select count(*) as counter, max(c_create_time) as maxdate from t_keypoint_group where kg_status>0";
	
	private String getAll_Chatroom_WhiteList="select m_chat_room_id,updatetime from t_group_white where status>0";
	private String checkUpdate_Chatroom_WhiteList="select count(*) as counter, max(updatetime) as maxdate from t_group_white where status>0";
	private long cw_lastMaxTimeStamp=0L;
	private long cw_lastRecordCounter=0L;
	
	public static boolean isInit(){
		return init.get();
	}
	
	@Override
	public void run() {
		Connection conn=null;
		Statement stmt=null;
		
		while(true){
			try{
				conn = ConnectionPoolManager.getInstance().getConnection(Config.OracleDriver);
				stmt = conn.createStatement();
				boolean update=false;
				if(lastMaxTimeStamp!=0){
					ResultSet result = stmt.executeQuery(checkUpdate);
					while(result.next()){
						if(result.getLong("counter")!=lastRecordCounter||
								result.getDate("maxdate").getTime()!=lastMaxTimeStamp){
							update=true;
						}
					}
				}
				
				if(lastMaxTimeStamp==0 || update==true){
					ResultSet result=stmt.executeQuery(getAll);
					Map<Long,Map<Long,byte[]>> map=new HashMap<Long,Map<Long,byte[]>>();
					Map<Long,Map<Long,byte[]>> tmpChatroomToZhuti=new HashMap<Long,Map<Long,byte[]>>();
					lastMaxTimeStamp=-1;
					lastRecordCounter=0;
					while(result.next()){
						if(result.getDate("ct").getTime()>lastMaxTimeStamp){
							lastMaxTimeStamp=result.getDate("ct").getTime();
						}
						Map<Long,byte[]> tmpMap=map.get(result.getLong("chid"));
						Map<Long,byte[]> tmTmpChatroomToZhuti=tmpChatroomToZhuti.get(result.getLong("chid"));
						if(tmpMap==null){
							tmpMap=new HashMap<Long,byte[]>();
							map.put(result.getLong("chid"), tmpMap);
							tmTmpChatroomToZhuti=new HashMap<Long,byte[]>();
							tmpChatroomToZhuti.put(result.getLong("chid"), tmTmpChatroomToZhuti);
						}
						tmpMap.put(result.getLong("tid"), new byte[0]);
						tmTmpChatroomToZhuti.put(result.getLong("tid"), new byte[0]);

						LOG.info("refresh surveillance chatroom  list,adding chatroom id:{} tid:{}",result.getLong("chid"),result.getLong("tid"));
						lastRecordCounter++;
					}
					
					ChatRoomFilteringHandler.setCHATROOMIDMap(map);
				}
				
				update=false;
				if(cw_lastMaxTimeStamp!=0){
					ResultSet result = stmt.executeQuery(checkUpdate_Chatroom_WhiteList);
					while(result.next()){
						if(result.getLong("counter")!=cw_lastRecordCounter||
								result.getDate("maxdate").getTime()!=cw_lastMaxTimeStamp){
							update=true;
						}
					}
				}
				
				if(cw_lastMaxTimeStamp==0 || update==true){
					ResultSet result=stmt.executeQuery(getAll_Chatroom_WhiteList);
					Map<Long,Byte> map=new HashMap<Long,Byte>();
					while(result.next()){
						if(result.getDate("updatetime").getTime()>cw_lastMaxTimeStamp){
							cw_lastMaxTimeStamp=result.getDate("updatetime").getTime();
						}
						map.put(result.getLong("m_chat_room_id"), (byte)0);
						LOG.info("refresh chatroom white list,adding chatroom id:{}",result.getLong("m_chat_room_id"));
					}
					cw_lastRecordCounter=map.size();
					ChatRoomFilteringHandler.setCHATROOM_WHITELIST(map);
				}
				if(init.get()==false)
					init.set(true);
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
				try {
					conn.close();
				} catch (SQLException ex) {
					LOG.error(ex.getMessage(), ex);
				}
			} finally {
				try {
					if (stmt != null)
						try {
							stmt.close();
						} catch (SQLException e) {
							LOG.error(e.getMessage(), e);
						}
					ConnectionPoolManager.getInstance().close(Config.OracleDriver, conn);
					Thread.sleep(5 * 1000);
				} catch (InterruptedException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}
}
