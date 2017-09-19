package cn.ac.iie.DataExchange;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.WriteType;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.policies.RetryPolicy;

public class ConsumRetryPolicy implements RetryPolicy{

	@Override
	public RetryDecision onReadTimeout(Statement statement, ConsistencyLevel cl, int requiredResponses, int receivedResponses,
			boolean dataRetrieved, int nbRetry) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		return RetryDecision.retry(cl);
	}

	@Override
	public RetryDecision onWriteTimeout(Statement statement, ConsistencyLevel cl, WriteType writeType, int requiredAcks, int receivedAcks,
			int nbRetry) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		return RetryDecision.retry(cl);
	}

	@Override
	public RetryDecision onUnavailable(Statement statement, ConsistencyLevel cl, int requiredReplica, int aliveReplica, int nbRetry) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		return RetryDecision.retry(cl);
	}

	@Override
	public RetryDecision onRequestError(Statement statement, ConsistencyLevel cl, DriverException e, int nbRetry) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
		}
		return RetryDecision.retry(cl);
	}

	@Override
	public void init(Cluster cluster) {
	}

	@Override
	public void close() {
	}
}
