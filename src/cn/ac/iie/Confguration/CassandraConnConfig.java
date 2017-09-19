package cn.ac.iie.Confguration;

public class CassandraConnConfig {
	private String dataCenter;
	private String connAddress;
	private int port;
	private String userName;
	private String password;
	private String keySpace;
	/**
	 * @return the dataCenter
	 */
	public String getDataCenter() {
		return dataCenter;
	}
	/**
	 * @param dataCenter the dataCenter to set
	 */
	public void setDataCenter(String dataCenter) {
		this.dataCenter = dataCenter;
	}
	/**
	 * @return the connAddress
	 */
	public String getConnAddress() {
		return connAddress;
	}
	/**
	 * @param connAddress the connAddress to set
	 */
	public void setConnAddress(String connAddress) {
		this.connAddress = connAddress;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the keySpace
	 */
	public String getKeySpace() {
		return keySpace;
	}
	/**
	 * @param keySpace the keySpace to set
	 */
	public void setKeySpace(String keySpace) {
		this.keySpace = keySpace;
	}
}
