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
import cn.ac.iie.DS.LogicSyntaxTree;
import cn.ac.iie.ProcessingHandler.ContentAnalysisHandler;
import iie.mm.dao.ConnectionPoolManager;

public class SurveilKeywordInfoMaintaining implements Runnable {
	private final static Logger LOG = LoggerFactory.getLogger(SurveilKeywordInfoMaintaining.class);

	private long lastMaxTimeStamp = 0L;
	private long lastRecordCounter = 0L;
	private static AtomicBoolean init = new AtomicBoolean(false);

	private String getAll = "select t.r_id as rid,t.rule as trule,t.r_update_time as updatetime,t.tp_id as tpid, t_th.t_id as tid"
			+ " from t_rule t, t_topic t_p, t_theme t_th where t.rule_type=0 and t.tp_id=t_p.tp_id and t_p.tp_t_id=t_th.t_id and t_p.tp_status>0 and t.r_status>0 and t_th.t_status>0";

	private String checkUpdate = "select count(*) as counter, max(t.r_update_time) as maxdate "
			+ " from t_rule t, t_topic t_p, t_theme t_th where t.rule_type=0 and t.tp_id=t_p.tp_id and t_p.tp_t_id=t_th.t_id and t_p.tp_status>0 and t.r_status>0 and t_th.t_status>0";

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
				if (lastMaxTimeStamp != 0) {
					ResultSet result = stmt.executeQuery(checkUpdate);
					while (result.next()) {
						// LOG.info("counter:{}
						// maxtime:{}",result.getLong("counter"),result.getDate("maxdate").getTime());
						if (result.getLong("counter") != lastRecordCounter
								|| result.getDate("maxdate").getTime() != lastMaxTimeStamp) {
							update = true;
						}
					}
				}

				if (lastMaxTimeStamp == 0 || update == true) {
					ResultSet result = stmt.executeQuery(getAll);
					Map<Long, LogicSyntaxTree> map = new HashMap<Long, LogicSyntaxTree>();
					Map<Long, Map<Long, byte[]>> ruleIdToZhutiid = new HashMap<Long, Map<Long, byte[]>>();
					Map<Long, Map<Long, byte[]>> ruleIdToZhuantiid = new HashMap<Long, Map<Long, byte[]>>();
					lastMaxTimeStamp = 0;
					lastRecordCounter = 0;
					while (result.next()) {
						if (result.getDate("updatetime").getTime() > lastMaxTimeStamp) {
							lastMaxTimeStamp = result.getDate("updatetime").getTime();
						}
						try {
							map.put(result.getLong("rid"), LogicSyntaxTree.parse(result.getString("trule")));
							Map<Long, byte[]> tmpRidToZhuantiid = ruleIdToZhuantiid.get(result.getLong("rid"));
							Map<Long, byte[]> tmpRidToZhutiid = ruleIdToZhutiid.get(result.getLong("rid"));

							if (tmpRidToZhuantiid == null) {
								tmpRidToZhuantiid = new HashMap<Long, byte[]>();
								ruleIdToZhuantiid.put(result.getLong("rid"), tmpRidToZhuantiid);
								tmpRidToZhutiid = new HashMap<Long, byte[]>();
								ruleIdToZhutiid.put(result.getLong("rid"), tmpRidToZhutiid);
							}

							tmpRidToZhuantiid.put(result.getLong("tpid"), new byte[0]);
							tmpRidToZhutiid.put(result.getLong("tid"), new byte[0]);
							LOG.info("refresh keyword, rid:" + result.getLong("rid") + "/zhuantiid:"
									+ result.getLong("tpid") + "/zhutiid:" + result.getLong("tid") + "/rule:"
									+ result.getString("trule"));
						} catch (Exception e) {
							LOG.error(e.getMessage());
						}
						lastRecordCounter++;
					}

					ContentAnalysisHandler.setLSTMap(map, ruleIdToZhutiid, ruleIdToZhuantiid);
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
