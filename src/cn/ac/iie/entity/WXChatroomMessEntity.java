package cn.ac.iie.entity;

public class WXChatroomMessEntity {
	private String msgID;
	private String from;
	private long fromUin;
	private String to;
	private int type;
	private String content;
	private long fromTime;
	private long chatroom;
	private int mediaId;
	private String clientIP;
	/**
	 * @return the msgID
	 */
	public String getMsgID() {
		return msgID;
	}
	/**
	 * @param msgID the msgID to set
	 */
	public void setMsgID(String msgID) {
		this.msgID = msgID;
	}
	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}
	/**
	 * @param from the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}
	/**
	 * @return the fromUin
	 */
	public long getFromUin() {
		return fromUin;
	}
	/**
	 * @param fromUin the fromUin to set
	 */
	public void setFromUin(long fromUin) {
		this.fromUin = fromUin;
	}
	/**
	 * @return the to
	 */
	public String getto() {
		return to;
	}
	/**
	 * @param fo the fo to set
	 */
	public void setto(String to) {
		this.to = to;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * @return the fromTime
	 */
	public long getFromTime() {
		return fromTime;
	}
	/**
	 * @param fromTime the fromTime to set
	 */
	public void setFromTime(long fromTime) {
		this.fromTime = fromTime;
	}
	/**
	 * @return the chatroom
	 */
	public long getChatroom() {
		return chatroom;
	}
	/**
	 * @param chatroom the chatroom to set
	 */
	public void setChatroom(long chatroom) {
		this.chatroom = chatroom;
	}
	/**
	 * @return the mediaId
	 */
	public int getMediaId() {
		return mediaId;
	}
	/**
	 * @param mediaId the mediaId to set
	 */
	public void setMediaId(int mediaId) {
		this.mediaId = mediaId;
	}
	/**
	 * @return the clientIP
	 */
	public String getClientIP() {
		return clientIP;
	}
	/**
	 * @param clientIP the clientIP to set
	 */
	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

}
