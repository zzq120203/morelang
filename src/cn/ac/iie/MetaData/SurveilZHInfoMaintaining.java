package cn.ac.iie.MetaData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iie.Confguration.Config;
import cn.ac.iie.ProcessingHandler.WXIDFilteringHandler;
import iie.mm.dao.ConnectionPoolManager;

public class SurveilZHInfoMaintaining implements Runnable {

	private final static Logger LOG = LoggerFactory.getLogger(SurveilZHInfoMaintaining.class);
	private long lastMaxTimeStamp = 0L;
	private long lastRecordCounter = 0L;
	private static AtomicBoolean init = new AtomicBoolean(false);
	
	private String getAll="select u_ch_id,c_create_time,cc_id from t_keypoint_account where ka_status>0";
	
	private String checkUpdate="select count(*) as counter, max(c_create_time) as maxdate from t_keypoint_account where ka_status>0";
	
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
					Map<Long,Map<Long,byte[]>> tmpZhToZhuiMap=new HashMap<Long,Map<Long,byte[]>>();
					lastMaxTimeStamp=0;
					lastRecordCounter=0;
					while(result.next()){
						if(result.getDate("c_create_time").getTime()>lastMaxTimeStamp){
							lastMaxTimeStamp=result.getDate("c_create_time").getTime();
						}
						Map<Long,byte[]> tmpMap=map.get(result.getLong("u_ch_id"));
						Map<Long,byte[]> tmTmpZhToZhuiMap=tmpZhToZhuiMap.get(result.getLong("u_ch_id"));
						if(tmpMap==null){
							tmpMap=new HashMap<Long,byte[]>();
							map.put(result.getLong("u_ch_id"), tmpMap);
							tmTmpZhToZhuiMap=new HashMap<Long,byte[]>();
							tmpZhToZhuiMap.put(result.getLong("u_ch_id"), tmTmpZhToZhuiMap);
						}
						tmpMap.put(result.getLong("cc_id"), new byte[0]);
						tmTmpZhToZhuiMap.put(result.getLong("cc_id"), new byte[0]);
						
						LOG.info("refresh surveillance zh list, add zh:{}/zhuti:{}",result.getLong("u_ch_id"),result.getLong("cc_id"));
						lastRecordCounter++;
					}
					WXIDFilteringHandler.setWXIDMap(map);
				}
				if (init.get() == false)
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
