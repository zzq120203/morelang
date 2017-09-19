package cn.ac.iie.DataExchange;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import avro.shaded.com.google.common.collect.Lists;
import cn.ac.iie.Confguration.CassandraConnConfig;
import cn.ac.iie.Confguration.Config;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.google.common.base.Strings;

/**
 * @author zhangjc 2017/3/29
 */
public class CassandraDriverWrapper {
	private final static Logger LOG = LoggerFactory.getLogger(CassandraDriverWrapper.class);
	private Cluster cluster;
	private Session session;

	private int connectionTimeOut = 10000;// 10s

	private boolean retry = true;
	private ObjectMapper om = new ObjectMapper();
	private Map<String, PreparedStatement> tableToPrepareSt = new HashMap<String, PreparedStatement>();

	public void PrepareInsert(String tableName) {
		if (!tableToPrepareSt.containsKey(tableName)) {
			PreparedStatement insertStm = session.prepare("insert into " + tableName + " json ?");
			insertStm.setIdempotent(true);
			tableToPrepareSt.put(tableName, insertStm);
		}
	}

	public CassandraDriverWrapper(CassandraConnConfig config){
		this(config.getDataCenter(),config.getConnAddress(),config.getPort(),
				config.getUserName(),config.getPassword(),config.getKeySpace());
	}
	
	public CassandraDriverWrapper(String dataCenter, String connAddress, int port, String userName, String password, String keySpace) {
		if (Strings.isNullOrEmpty(dataCenter))
			throw new IllegalArgumentException("Null or empty of dataCenter");

		if (Strings.isNullOrEmpty(connAddress))
			throw new IllegalArgumentException("Null or empty of connAddress");


		if (Strings.isNullOrEmpty(userName))
			throw new IllegalArgumentException("Null or empty of userName");

		if (Strings.isNullOrEmpty(password))
			throw new IllegalArgumentException("Null or empty of password");

		if (Strings.isNullOrEmpty(keySpace))
			throw new IllegalArgumentException("Null or empty of keySpace");

		QueryOptions options = new QueryOptions();
		options.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
		Cluster.Builder builder = Cluster.builder();
		if (userName.length() > 0)
			builder.withSocketOptions(new SocketOptions().setConnectTimeoutMillis(connectionTimeOut).setReadTimeoutMillis(Config.CassandraReadTimeOut))
					.withLoadBalancingPolicy(new TokenAwarePolicy(DCAwareRoundRobinPolicy.builder().withLocalDc(dataCenter).build()))
					.withCredentials(userName, password).addContactPoint(connAddress).withPort(port).withQueryOptions(options);
		else
			builder.withSocketOptions(new SocketOptions().setConnectTimeoutMillis(connectionTimeOut).setReadTimeoutMillis(Config.CassandraReadTimeOut))
					.withLoadBalancingPolicy(new TokenAwarePolicy(DCAwareRoundRobinPolicy.builder().withLocalDc(dataCenter).build()))
					.addContactPoint(connAddress).withPort(port).withQueryOptions(options);

		if (retry)
			cluster = builder.withRetryPolicy(new ConsumRetryPolicy()).build();
		else
			cluster = builder.build();
		session = cluster.connect(keySpace);
	}

	/**
	 * insert a single row in synchronous way
	 * 
	 * @param tableName
	 * @param tuple
	 *            map of <column name,value> pair
	 */
	public void insertion(String tableName, Map<String, Object> tuple) {
		PreparedStatement insertStm = tableToPrepareSt.get(tableName);

		try {
			session.execute(insertStm.bind(om.writeValueAsString(tuple)));
		} catch (IOException e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void insertionAsync(String tableName, Map<String, Object> tuple) {
		PreparedStatement insertStm = tableToPrepareSt.get(tableName);

		try {
			session.executeAsync(insertStm.bind(om.writeValueAsString(tuple)));
		} catch (IOException e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * insert a batch of tuples in synchronous way
	 * 
	 * @param tableName
	 * @param tuples
	 *            list of tuples, a tuple is a row that represented by map of
	 *            <column name ,value> pair
	 */
	public void insertionBatch(String tableName, List<Map<String, Object>> tuples) {

		PreparedStatement insertStm = tableToPrepareSt.get(tableName);
		BatchStatement batchStm = new BatchStatement();
		BoundStatement bs;

		for (Map<String, Object> kv : tuples) {
			try {
				bs = insertStm.bind(om.writeValueAsString(kv));
				batchStm.add(bs);
			} catch (IOException e) {
				LOG.error(e.getMessage());
				LOG.error(kv.toString());
				e.printStackTrace();
			}
		}
		LOG.info("write data to cassandra, table:{}, batch size:{}", tableName, tuples.size());
		ResultSet res = session.execute(batchStm);
		batchStm.clear();
		LOG.debug(res.toString());
	}

	/**
	 * insert a batch of tuples in asynchronous way
	 * 
	 * @param tableName
	 * @param tuples
	 *            list of tuples, a tuple is a row that represented by map of
	 *            <column name ,value> pair
	 */
	public void insertionBatchAsync(String tableName, List<Map<String, Object>> tuples) {
//		if(datapersist==false){
//			return;
//		}
		long lastErrorLogTimestamp=0L;
		PreparedStatement insertStm = tableToPrepareSt.get(tableName);
		BoundStatement bs = null;
		List<ResultSetFuture> future = Lists.newArrayListWithExpectedSize(tuples.size());
		for (Map<String, Object> map : tuples) {
			try {

				bs = insertStm.bind(om.writeValueAsString(map));
			} catch (JsonGenerationException e) {
				LOG.error(e.getMessage());
				e.printStackTrace();
			} catch (JsonMappingException e) {
				LOG.error(e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				LOG.error(e.getMessage());
				e.printStackTrace();
			}
			ResultSetFuture result = session.executeAsync(bs);
			future.add(result);
		}

		for (int i = 0; i < future.size(); i++) {
			try {
				ResultSet rs = future.get(i).get();
//				ResultSet rs = future.get(i).get(2,TimeUnit.SECONDS);
				String result_string = rs.toString();
				if (result_string.contains("ERROR")) {
					LOG.error(result_string);
				}

			} catch (Exception e) {
				if(System.currentTimeMillis()-lastErrorLogTimestamp>2000){
					LOG.error(e.getMessage());
					lastErrorLogTimestamp=System.currentTimeMillis();
				}

				
//				try {
//					//LOG.error("Error key is {} ", om.writeValueAsString(tuples.get(i)));
//				} catch (JsonGenerationException e1) {
//					e1.printStackTrace();
//				} catch (JsonMappingException e1) {
//					e1.printStackTrace();
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
			}
		}
	}
}
