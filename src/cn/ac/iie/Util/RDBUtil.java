package cn.ac.iie.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ac.iie.Confguration.Config;
import cn.ac.iie.entity.HotEvent;
import cn.ac.iie.entity.HotLocale;
import iie.mm.dao.ConnectionPoolManager;

public class RDBUtil {
	private static Logger LOG = LoggerFactory.getLogger(RDBUtil.class);

	public RDBUtil() {
		super();
	}

	public boolean updataOrc(HotEvent event, List<Object> kw_elc) {
		StringBuffer e_keyword = new StringBuffer(kw_elc.get(0) + "&(");
		HashSet<String> loc = (HashSet<String>) kw_elc.get(1);
		for (String string : loc) {
			e_keyword.append(string + "|");
		}
		e_keyword.setCharAt(e_keyword.length() - 1, ')');
		
		long e_hot = (long) kw_elc.get(2);
		int e_id = (event.getE_province() + event.getE_name()).hashCode();
		LOG.debug("ppppppppppppppppppppppppp isHOTEvent start");
		
		ZonedDateTime mt = getTimeLong(Config.intervalMinute);

		if (!isHotEvent(e_id, e_hot, mt)) {
			LOG.info("hot event e_id:{}, e_hot:{} is not hot event", e_id, e_hot);
//			return true;
		}

		LOG.info("hot event e_id:{}, e_hot:{}, e_keyword:{}", e_id, e_hot, e_keyword);

		Connection dbConn = null;
		PreparedStatement pstmt = null;
		try {
			dbConn = ConnectionPoolManager.getInstance().getConnection(Config.OracleDriver);
			String sql = "INSERT INTO t_hot_event (e_class,e_name,e_province,e_city,e_keyword,e_hot,e_id,e_publish_time,"
					+ "e_day,e_HOUR,e_MINUTE,e_hostid,id) "// TODO 机器ID
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?,t_hot_event_seq.nextval)";
			pstmt = dbConn.prepareStatement(sql);
			pstmt.setString(1, event.getE_class());
			pstmt.setString(2, event.getE_name());
			pstmt.setString(3, event.getE_province());
			pstmt.setString(4, event.getE_city());
			pstmt.setString(5, e_keyword.toString());
			pstmt.setLong(6, e_hot);
			pstmt.setInt(7, e_id);
			pstmt.setLong(8, mt.getLong(ChronoField.INSTANT_SECONDS));
			pstmt.setLong(9, mt.getDayOfMonth());
			pstmt.setLong(10, mt.getHour());
			pstmt.setLong(11, mt.getMinute());
			
			pstmt.setLong(12, Config.hostID);
			
			
			pstmt.executeUpdate();
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
			try {
				dbConn.close();
				return false;
			} catch (SQLException ex) {
				LOG.error(ex.getMessage(), ex);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				ConnectionPoolManager.getInstance().close(Config.OracleDriver, dbConn);
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		return false;
	}

	private boolean isHotEvent(int e_id, long e_hot, ZonedDateTime mt) {
		double[] data = null;
		if (data == null || data.length <= 0) {
			
			data = getDataArrayEvent(e_id, mt);
//			cached.put(e_id+"", data);
		}
		LOG.debug("sssssssssssssssssssssssssssssssssssssssssssssssssssssss length:"+data.length);
		BoxPlots bp = new BoxPlots(data, e_hot);
		boolean isHotLoc = bp.isOutliner();
		
		return isHotLoc;
	}

	private double[] getDataArrayEvent(int e_id, ZonedDateTime mt) {
		// TODO Auto-generated method stub
		double[] dataArray = new double[5];
		Connection dbConn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ZonedDateTime mtt = null;
		try {
			dbConn = ConnectionPoolManager.getInstance().getConnection(Config.OracleDriver);
			String sql = "select e_avg,E_PUBLISH_TIME,e_hot from t_hot_EVENT where E_ID= ? and e_hour = ? and e_minute = ? and e_hostid = ?";
			pstmt = dbConn.prepareStatement(sql);
			for (int i = 0; i < dataArray.length; i++) {
				pstmt.clearParameters();
				mtt = mt.minusMinutes(i * Config.intervalMinute);
				pstmt.setLong(1, e_id);
				pstmt.setLong(2, mtt.getHour());
				pstmt.setLong(3, mtt.getMinute());
				
				pstmt.setLong(4, Config.hostID);
				
				rs = pstmt.executeQuery();
				double Rate_rise = 0D;
				while (rs.next()) {
					Rate_rise = rs.getDouble("e_avg");
					if (Rate_rise == 0D) {//默认0
						Rate_rise = getAvgEvent(e_id, mt);
					} else {
						Rate_rise = (rs.getLong("e_hot") - Rate_rise) / Rate_rise;
					}
				}
				dataArray[i] = Rate_rise;
			}
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
			try {
				dbConn.close();
				return null;
			} catch (SQLException ex) {
				LOG.error(ex.getMessage(), ex);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				ConnectionPoolManager.getInstance().close(Config.OracleDriver, dbConn);
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return dataArray;
	}
	
	private double getAvgEvent(int e_id, ZonedDateTime mt) {
		//TODO
		double dataArray = 0D;
		Connection dbConn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ZonedDateTime mtt = null;
		double Rate_rise = 0D;
		try {
			dbConn = ConnectionPoolManager.getInstance().getConnection(Config.OracleDriver);
			String sql = "select e_avg from t_hot_event where E_ID = ? and e_day = ? and e_hour = ? and e_hostid = ? and e_status = 0";
			pstmt = dbConn.prepareStatement(sql);
			for (int i = 0; i < 7; i++) {
				pstmt.clearParameters();
				mtt = mt.minusDays(i);
				pstmt.setLong(1, e_id);
				pstmt.setLong(2, mtt.getDayOfMonth());
				pstmt.setLong(3, mtt.getHour());

				pstmt.setLong(4, Config.hostID);
				
				rs = pstmt.executeQuery();
				while (rs.next()) {
					dataArray += rs.getDouble("avg");
				}
			}
			pstmt.close();
			
			pstmt = dbConn.prepareStatement(sql);
			
			Rate_rise = dataArray/7D;
			
			sql = "UPDATE t_hot_event SET e_avg = ? WHERE E_ID = ? and e_hour = ? and e_hostid = ?";
			pstmt = dbConn.prepareStatement(sql);
			pstmt.setDouble(1, Rate_rise);
			pstmt.setLong(2, e_id);
			pstmt.setLong(3, mt.getHour());
			
			pstmt.setLong(4, Config.hostID);
			
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
			try {
				dbConn.close();
				return Rate_rise;
			} catch (SQLException ex) {
				LOG.error(ex.getMessage(), ex);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				ConnectionPoolManager.getInstance().close(Config.OracleDriver, dbConn);
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return Rate_rise;
	}
	
	public boolean updataLoc(HotLocale hotLoc, Long count) {
		ZonedDateTime mt = getTimeLong(Config.intervalMinute);
		if (!isHotLocale(hotLoc.getL_PROVINCE_CODE(), count, mt)) {
			LOG.info("hot Locale L_PROVINCE:{}, L_HOT:{} is not hot Locale", hotLoc.getL_PROVINCE(), count);
//			return true;
		}

		Connection dbConn = null;
		PreparedStatement pstmt = null;
		try {
			dbConn = ConnectionPoolManager.getInstance().getConnection(Config.OracleDriver);
			String sql = "INSERT INTO t_hot_LOCALE (L_PROVINCE_CODE,L_PROVINCE,L_EVENT,L_HOT,L_TIME,"
					+ "l_day,l_HOUR,l_MINUTE,l_hostid,id) "
					+ "values (?,?,?,?,?,?,?,?,?,t_hot_LOCALE_seq.nextval)";
			pstmt = dbConn.prepareStatement(sql);
			pstmt.setInt(1, hotLoc.getL_PROVINCE_CODE());
			pstmt.setString(2, hotLoc.getL_PROVINCE());
			pstmt.setString(3, null);
			pstmt.setLong(4, count);
			
			pstmt.setLong(5, mt.getLong(ChronoField.INSTANT_SECONDS));
			pstmt.setLong(6, mt.getDayOfMonth());
			pstmt.setLong(7, mt.getHour());
			pstmt.setLong(8, mt.getMinute());
			
			pstmt.setLong(9, Config.hostID);
			
			pstmt.executeUpdate();
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
			try {
				dbConn.close();
				return false;
			} catch (SQLException ex) {
				LOG.error(ex.getMessage(), ex);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				ConnectionPoolManager.getInstance().close(Config.OracleDriver, dbConn);
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		return false;

	}

	private static RPoolProxy rpp = null;
	
	private boolean isHotLocale(int province_code, Long count, ZonedDateTime mt) {
		double[] data = null;
		if (data == null || data.length <= 0) {
			data = getDataArray(province_code, mt);
//			locale.put(province_code, data);
		}
		BoxPlots bp = new BoxPlots(data, count);
		boolean isHotLoc = bp.isOutliner();
		
		return isHotLoc;
	}

	private double[] getDataArray(int province_code, ZonedDateTime mt) {
		// TODO Auto-generated method stub
		double[] dataArray = new double[5];
		Connection dbConn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ZonedDateTime mtt = null;
		try {
			dbConn = ConnectionPoolManager.getInstance().getConnection(Config.OracleDriver);
			String sql = "select l_avg,l_time,l_hot from t_hot_locale where l_province_code= ? and l_hour = ? and l_minute = ? and l_hostid = ?";
			pstmt = dbConn.prepareStatement(sql);
			for (int i = 0; i < dataArray.length; i++) {
				pstmt.clearParameters();
				mtt = mt.minusMinutes(i * Config.intervalMinute);
				pstmt.setLong(1, province_code);
				pstmt.setLong(2, mtt.getHour());
				pstmt.setLong(3, mtt.getMinute());
				
				pstmt.setLong(4, Config.hostID);
				
				rs = pstmt.executeQuery();
				double Rate_rise = 0D;
				while (rs.next()) {
					Rate_rise = rs.getDouble("l_avg");
					if (Rate_rise == 0D) {//默认0
						Rate_rise = getAvg(province_code, mt);
					} else {
						Rate_rise = (rs.getLong("l_hot") - Rate_rise) / Rate_rise;
					}
				}
				dataArray[i] = Rate_rise;
			}
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
			try {
				dbConn.close();
				return null;
			} catch (SQLException ex) {
				LOG.error(ex.getMessage(), ex);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				ConnectionPoolManager.getInstance().close(Config.OracleDriver, dbConn);
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return dataArray;
	}

	private double getAvg(int province_code, ZonedDateTime mt) {
		//TODO
		double dataArray = 0D;
		Connection dbConn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ZonedDateTime mtt = null;
		double Rate_rise = 0D;
		try {
			dbConn = ConnectionPoolManager.getInstance().getConnection(Config.OracleDriver);
			String sql = "select l_avg from t_hot_locale where l_province_code= ? and l_day = ? and l_hour = ? and l_hostid = ? and l_status = 0";
			pstmt = dbConn.prepareStatement(sql);
			for (int i = 0; i < 7; i++) {
				pstmt.clearParameters();
				mtt = mt.minusDays(i);
				pstmt.setLong(1, province_code);
				pstmt.setLong(2, mtt.getDayOfMonth());
				pstmt.setLong(3, mtt.getHour());
				
				pstmt.setLong(4, Config.hostID);
				
				rs = pstmt.executeQuery();
				while (rs.next()) {
					dataArray += rs.getDouble("l_avg");
				}
			}
			
			Rate_rise = dataArray/7D;
			pstmt.close();
			sql = "UPDATE t_hot_locale SET l_avg = ? WHERE l_province_code = ? and l_hour = ? and l_hostid = ?";
			pstmt = dbConn.prepareStatement(sql);
			pstmt.setDouble(1, Rate_rise);
			pstmt.setLong(2, province_code);
			pstmt.setLong(3, mt.getHour());
			
			pstmt.setLong(4, Config.hostID);
			
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
			try {
				dbConn.close();
				return Rate_rise;
			} catch (SQLException ex) {
				LOG.error(ex.getMessage(), ex);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				ConnectionPoolManager.getInstance().close(Config.OracleDriver, dbConn);
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return Rate_rise;
	}

	public ZonedDateTime getTimeLong(int interval_minute) {
		ZonedDateTime zoned = ZonedDateTime.now();
		return zoned.minusMinutes(zoned.getMinute() % interval_minute).minusSeconds(zoned.getSecond()).minusNanos(zoned.getNano());
	}

	public static void main(String[] args) {
		RDBUtil rdb = new RDBUtil();
		ZonedDateTime mt = rdb.getTimeLong(Config.intervalMinute);
		System.out.println(mt.getLong(ChronoField.INSTANT_SECONDS));
		System.out.println(mt.minusMinutes(5));
		System.out.println(mt.minusHours(56));
	}

}
