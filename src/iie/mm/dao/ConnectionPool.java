package iie.mm.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConnectionPool implements IConnectionPool {
	
	private static Logger LOG = LoggerFactory.getLogger(ConnectionPool.class);
	
	// 连接池配置属性
	private DBbean dbBean;
	private boolean isActive = false; // 连接池活动状态
	private int contActive = 0;// 记录创建的总的连接数
	
	// 空闲连接
	private List<Connection> freeConnection = new Vector<Connection>();
	// 活动连接
	private List<Connection> activeConnection = new Vector<Connection>();
	// 将线程和连接绑定，保证事务能统一执行
	private static ThreadLocal<Connection> threadLocal = new ThreadLocal<Connection>();
	
	public ConnectionPool(DBbean dbBean) {
		super();
		this.dbBean = dbBean;
		init();
		cheackPool();
	}

	// 初始化
	public void init() {
		try {
			Class.forName(dbBean.getDriverName());
			for (int i = 0; i < dbBean.getInitConnections(); i++) {
				Connection conn;
				conn = newConnection();
				// 初始化最小连接数
				if (conn != null) {
					freeConnection.add(conn);
					contActive++;
				}
			}
			isActive = true;
		} catch (ClassNotFoundException e) {
			LOG.error(e.getMessage(), e);
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	// 获得当前连接
	public Connection getCurrentConnecton(){
		// 默认线程里面取
		Connection conn = threadLocal.get();
		if(!isValid(conn)){
			conn = getConnection();
		}
		return conn;
	}

	// 获得连接
	public synchronized Connection getConnection() {
		Connection conn = null;
		try {
			// 判断是否超过最大连接数限制
			if(contActive < this.dbBean.getMaxActiveConnections()){
				if (freeConnection.size() > 0) {
					conn = freeConnection.get(0);
					if (conn != null) {
						threadLocal.set(conn);
					}
					freeConnection.remove(0);
				} else {
					conn = newConnection();
				}
				
			}else{
				// 继续获得连接,直到从新获得连接
				wait(this.dbBean.getConnTimeOut());
				conn = getConnection();
			}
			if (isValid(conn)) {
				activeConnection.add(conn);
				contActive ++;
			}
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			LOG.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
		return conn;
	}

	// 获得新连接
	private synchronized Connection newConnection()
			throws ClassNotFoundException, SQLException {
		Connection conn = null;
		if (dbBean != null) {
			Class.forName(dbBean.getDriverName());
			conn = DriverManager.getConnection(dbBean.getUrl(),
					dbBean.getUserName(), dbBean.getPassword());
		}
		return conn;
	}

	// 释放连接
	public synchronized void releaseConn(Connection conn) throws SQLException {
		if (isValid(conn)&& !(freeConnection.size() > dbBean.getMaxConnections())) {
			freeConnection.add(conn);
		}
		activeConnection.remove(conn);
		contActive --;
		threadLocal.remove();
		// 唤醒所有正待等待的线程，去抢连接
		notifyAll();
	}

	// 判断连接是否可用
	private boolean isValid(Connection conn) {
		try {
			if (conn == null || conn.isClosed()) {
				return false;
			}
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		}
		return true;
	}

	// 销毁连接池
	public synchronized void destroy() {
		for (Connection conn : freeConnection) {
			try {
				if (isValid(conn)) {
					conn.close();
				}
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		for (Connection conn : activeConnection) {
			try {
				if (isValid(conn)) {
					conn.close();
				}
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		isActive = false;
		contActive = 0;
	}

	// 连接池状态
	@Override
	public boolean isActive() {
		return isActive;
	}
	
	// 定时检查连接池情况
	@Override
	public void cheackPool() {
		if(dbBean.isCheakPool()){
			new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
			LOG.info("{} 空线池连接数:{}; 活动连接数:{}; 总的连接数:{}", dbBean.getDriverName(), freeConnection.size(), activeConnection.size(), contActive);
				}
			},dbBean.getLazyCheck(),dbBean.getPeriodCheck());
		}
	}
}

