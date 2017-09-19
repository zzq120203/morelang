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
import cn.ac.iie.ProcessingHandler.LocaleFilterHandler;
import iie.mm.dao.ConnectionPoolManager;

public class SurveilLocaleInfoMaintaining implements Runnable {
	private final static Logger LOG = LoggerFactory.getLogger(SurveilLocaleInfoMaintaining.class);
	
	private static AtomicBoolean init = new AtomicBoolean(false);
	
	private long lastMaxId = 0L;
	
	private String checkUpdate="select max(id) from t_province_city";
	private String locall = "select * from t_province_city";
	public static boolean isInit() {
		return init.get();
	}

	@Override
	public void run() {
		Connection conn = null;
		Statement stmt = null;

		while (true) {
			try {
				conn = ConnectionPoolManager.getInstance().getConnection(Config.OracleDriver);
				stmt = conn.createStatement();
				boolean update = false;

				ResultSet result = stmt.executeQuery(checkUpdate);
				while (result.next()) {
					if (result.getLong(1) != lastMaxId) {
						update = true;
					}					
				}				
				
				if (update == true) {
					result = stmt.executeQuery(locall);
					Map<String,Integer> slocMap = new HashMap<String,Integer>();
					lastMaxId = 0;
					
					while (result.next()) {
						if (result.getLong("id") > lastMaxId) {
							lastMaxId = result.getLong("id");
						}
						String provincename = result.getString("provincename"), cityname = result.getString("cityname");
						if (cityname!=null)
							cityname = cityname.replace("　", "");
						int provinceid = result.getInt("provinceid"), cityid = result.getInt("cityid");
						slocMap.putIfAbsent(provincename, provinceid);
						slocMap.putIfAbsent(cityname, cityid);
//							slocMap.put(result.getString("countyname"), result.getInt("countyid"));
						LOG.info("refresh Locale list, add province:"+provincename+"/city:"+cityname);
					}
					LocaleFilterHandler.setLocMap(slocMap);;
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
