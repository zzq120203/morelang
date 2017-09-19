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

public class LanguageWhiteListMaintaining implements Runnable {
	private final static Logger LOG = LoggerFactory.getLogger(LanguageWhiteListMaintaining.class);
	
	private static AtomicBoolean init = new AtomicBoolean(false);
	
	private long lastMaxId = 0L;
	
	private String locAll = "select id,lang_code,lang_name from t_lang_white where status = 1";
	
	private String locUpdate = "select max(id) as maxid from t_lang_white where status = 1";
	
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
					HashSet<String> langs = new HashSet<String>();
					lastMaxId = 0;
					
					while (result.next()) {
						if (result.getLong("id") > lastMaxId) {
							lastMaxId = result.getLong("id");
						}
						langs.add(result.getString("lang_code"));
						LOG.info("refresh lang list, add name:{}/code:{}", result.getString("lang_name"), result.getString("lang_code"));
					}
					DataPushHandler.setlangWhiteList(langs);
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
