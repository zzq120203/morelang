package cn.ac.iie.MetaData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iie.Confguration.Config;
import cn.ac.iie.ProcessingHandler.DataPushHandler;
import iie.mm.dao.ConnectionPoolManager;

public class LocaleWhiteListMaintaining implements Runnable {
	private final static Logger LOG = LoggerFactory.getLogger(LocaleWhiteListMaintaining.class);
	
	private static AtomicBoolean init = new AtomicBoolean(false);
	
	private long lastMaxId = 0L;
	
	private String locAll = "select id,locale_id,locale_name from t_locale_white where status = 1";
	
	private String locUpdate = "select max(id) as maxid from t_locale_white where status = 1";
	
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

				ResultSet result = stmt.executeQuery(locUpdate);
				while (result.next()) {
					if (result.getLong("maxid") != lastMaxId) {
						update = true;
					}					
				}				
				
				if (update == true) {
					result = stmt.executeQuery(locAll);
					HashSet<Long> locs = new HashSet<Long>();
					lastMaxId = 0;
					
					while (result.next()) {
						if (result.getLong("id") > lastMaxId) {
							lastMaxId = result.getLong("id");
						}
						locs.add(result.getLong("locale_id"));
						LOG.info("refresh locale list, add name:{}/id:{}", result.getString("locale_name"), result.getLong("locale_id"));
					}
					DataPushHandler.setLocWhiteList(locs);
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
