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
import cn.ac.iie.ProcessingHandler.DataPushHandler;
import cn.ac.iie.ProcessingHandler.URLFilterHandler;
import iie.mm.dao.ConnectionPoolManager;

public class URLKeywordWhiteListInfoMaintaining implements Runnable {

private final static Logger LOG=LoggerFactory.getLogger(URLKeywordWhiteListInfoMaintaining.class);
	private long lastMaxTimeStamp=0L;
	private long lastRecordCounter=0L;
	private static AtomicBoolean init=new AtomicBoolean(false);
	
	private String getAll="select keyword,updatetime from url_keyword_whitelist where status>0";
	
	private String checkUpdate="select count(*) as counter, max(updatetime) as maxdate from url_keyword_whitelist where status>0";
	
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
					Map<String,byte[]> UrlWhiteKeywordMap=new HashMap<String,byte[]>();
					lastMaxTimeStamp=0;
					while(result.next()){
						if(result.getDate("updatetime").getTime()>lastMaxTimeStamp){
							lastMaxTimeStamp=result.getDate("updatetime").getTime();
						}
						UrlWhiteKeywordMap.put(result.getString("keyword"), new byte[0]);
						LOG.info("refresh url keyword white list, add whitekeyword:{}",result.getString("keyword"));
					}
					lastRecordCounter=UrlWhiteKeywordMap.size();
					URLFilterHandler.setURLWLMap(UrlWhiteKeywordMap);
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
