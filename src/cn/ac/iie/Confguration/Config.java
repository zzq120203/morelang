package cn.ac.iie.Confguration;

import cn.ac.iie.Util.FieldMeta;

public class Config {

	/**
	 * mq's parameters of url data storage
	 */
	@FieldMeta(isOptional = false, desc = "")
	public static String StoreMQAddress;

	@FieldMeta(isOptional = false, desc = "")
	public static int StoreMQPort;

	@FieldMeta(isOptional = true, desc = "")
	public static final String MQStoreEncoding = "UTF-8";

	@FieldMeta(isOptional = false, desc = "")
	public static String StoreConsumerGroup;

	@FieldMeta(isOptional = false, desc = "")
	public static String Topic;

	@FieldMeta(isOptional = false, desc = "")
	public static String UrlStoreConsumerGroup;

	@FieldMeta(isOptional = false, desc = "")
	public static String UrlTopic;

	/**
	 * mq's parameters of original message
	 */
	@FieldMeta(isOptional = false, desc = "")
	public static String OriMessMQAddress;

	@FieldMeta(isOptional = false, desc = "")
	public static int OriMessMQPort;

	@FieldMeta(isOptional = false, desc = "")
	public static String OriMessConsumerGroup;

	@FieldMeta(isOptional = false, desc = "")
	public static String OriMessTopic;

	@FieldMeta(isOptional = false, desc = "")
	public static int OriMessConsumerthreadNr;

	@FieldMeta(isOptional = false, desc = "")
	public static int GroupGatherPercent;

	@FieldMeta(isOptional = false, desc = "")
	public static int EnableContentAnalysis;

	@FieldMeta(isOptional = false, desc = "")
	public static int DetectLevel;

	@FieldMeta(isOptional = false, desc = "")
	public static int DetectDays;

	@FieldMeta(isOptional = false, desc = "")
	public static String ip2regiondb;

	/**
	 * redis store for chatroom and wxid relationship,
	 */
	@FieldMeta(isOptional = false, desc = "")
	public static String RedisURI;

	@FieldMeta(isOptional = false, desc = "")
	public static String RedisAuth;

	/**
	 * db of configuration
	 */
	@FieldMeta(isOptional = false, desc = "")
	public static String ConfigDBURI;

	@FieldMeta(isOptional = false, desc = "")
	public static String ConfigDBUser;

	@FieldMeta(isOptional = false, desc = "")
	public static String ConfigDBPassword;

	@FieldMeta(isOptional = true, desc = "type of the database, supporting mysql(default) and oracle")
	public static String DBType = "mysql";

	@FieldMeta(isOptional = true, desc = "")
	public static int threadNumber = 1;

	@FieldMeta(isOptional = true, desc = "")
	public static String log4jPath = "./configs/log4j.properties";

	@FieldMeta(isOptional = true, desc = "")
	public static int intervalMinute = 10;

	/**
	 * MPP
	 */
	@FieldMeta(isOptional = false, desc = "")
	public static String DataCenterAllMess;

	@FieldMeta(isOptional = false, desc = "")
	public static String ConnAddressAllMess;

	@FieldMeta(isOptional = false, desc = "")
	public static int MppPortAllMess;

	@FieldMeta(isOptional = false, desc = "")
	public static String UserNameAllMess;

	@FieldMeta(isOptional = false, desc = "")
	public static String PasswordAllMess;

	@FieldMeta(isOptional = false, desc = "")
	public static String KeySpaceAllMess;

	@FieldMeta(isOptional = false, desc = "")
	public static String DataCenterOther;

	@FieldMeta(isOptional = false, desc = "")
	public static String ConnAddressOther;

	@FieldMeta(isOptional = false, desc = "")
	public static int MppPortOther;

	@FieldMeta(isOptional = false, desc = "")
	public static String UserNameOther;

	@FieldMeta(isOptional = false, desc = "")
	public static String PasswordOther;

	@FieldMeta(isOptional = false, desc = "")
	public static String KeySpaceOther;

	@FieldMeta(isOptional = false, desc = "")
	public static String TableNameOfAllMess = "";

	@FieldMeta(isOptional = false, desc = "")
	public static String TableNameOfTargetedMess = "";

	@FieldMeta(isOptional = false, desc = "")
	public static String TableNameOfBoCe = "";

	@FieldMeta(isOptional = true, desc = "read timeout(in milliseconds) of cassandra, default is 0")
	public static int CassandraReadTimeOut = 0;

	@FieldMeta(isOptional = false, desc = "")
	public static String OracleDriver;

	@FieldMeta(isOptional = false, desc = "")
	public static long hostID;
}
