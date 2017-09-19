package cn.ac.iie.entity;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WXMessOriginEntity {
	private String g_id;
	private long m_publish_time;
	private long m_insert_time;
	private long m_chat_room;
	private String m_ch_id;
	private int m_type;
	private String m_content;
	private String m_language;
	private String u_g_ch_key;
	private String u_name;
	private long u_ch_id;
	private String u_send_ip;
	private int u_loc_county;
	private int u_loc_province;
	private long m_mm_id;
	private List<String> m_mm_feature;
	private List<Long> m_themes_list;
	private List<Long> m_topics_list;
	private List<Long> m_rules_list;
	private int m_year;
	private int m_month;
	private int m_day;
	private int m_hour;
	/*
	 * 1: in; -1: out; 0: unknown
	 */
	private int m_dom_for;
	private int m_country_code;
	private String m_mm_url;
	private int m_url_count;
	
	//tag of processing result,
	// tag & 1 !=0 indicates the mess is targeted by chat_room_id,
	// tag & 2 !=0 indicates the mess is targeted by wxid
	// tag & 4 !=0 indicates the mess is targeted by keyword
	// tag & 8 !=0 indicates the mess contains multi-media content
	// tag & 16!=0 indicates the mess contains url(s).
	// tag & 32!=0 indicates the mess is targeted by white list of chatroom id
	// tag & 64!=0 indicates the mess is targeted by more languages
	private int tag = 0;
	

	public WXMessOriginEntity(){
		m_mm_feature=new ArrayList<String>();
		m_themes_list=new ArrayList<Long>();
		m_topics_list=new ArrayList<Long>();
		m_rules_list=new ArrayList<Long>();
		tag=0;
	}
	/**
	 * @return the g_id
	 */
	public String getG_id() {
		return g_id;
	}

	/**
	 * @param g_id the g_id to set
	 */
	public void setG_id(String g_id) {
		this.g_id = g_id;
	}

	/**
	 * @return the m_publish_time
	 */
	public long getM_publish_time() {
		return m_publish_time;
	}

	/**
	 * @param m_publish_time the m_publish_time to set
	 */
	public void setM_publish_time(long m_publish_time) {
		this.m_publish_time = m_publish_time;
	}

	/**
	 * @return the m_insert_time
	 */
	public long getM_insert_time() {
		return m_insert_time;
	}

	/**
	 * @param m_insert_time the m_insert_time to set
	 */
	public void setM_insert_time(long m_insert_time) {
		this.m_insert_time = m_insert_time;
	}

	/**
	 * @return the m_chat_room
	 */
	public long getM_chat_room() {
		return m_chat_room;
	}

	/**
	 * @param m_chat_room the m_chat_room to set
	 */
	public void setM_chat_room(long m_chat_room) {
		this.m_chat_room = m_chat_room;
	}

	/**
	 * @return the m_ch_id
	 */
	public String getM_ch_id() {
		return m_ch_id;
	}

	/**
	 * @param m_ch_id the m_ch_id to set
	 */
	public void setM_ch_id(String m_ch_id) {
		this.m_ch_id = m_ch_id;
	}

	/**
	 * @return the m_type
	 */
	public int getM_type() {
		return m_type;
	}

	/**
	 * @param m_type the m_type to set
	 */
	public void setM_type(int m_type) {
		this.m_type = m_type;
	}

	/**
	 * @return the m_content
	 */
	public String getM_content() {
		return m_content;
	}

	/**
	 * @param m_content the m_content to set
	 */
	public void setM_content(String m_content) {
		this.m_content = m_content;
	}

	/**
	 * @return the m_language
	 */
	public String getM_language() {
		return m_language;
	}

	/**
	 * @param m_language the m_language to set
	 */
	public void setM_language(String m_language) {
		this.m_language = m_language;
	}

	/**
	 * @return the u_g_ch_key
	 */
	public String getU_g_ch_key() {
		return u_g_ch_key;
	}

	/**
	 * @param u_g_ch_key the u_g_ch_key to set
	 */
	public void setU_g_ch_key(String u_g_ch_key) {
		this.u_g_ch_key = u_g_ch_key;
	}

	/**
	 * @return the u_name
	 */
	public String getU_name() {
		return u_name;
	}

	/**
	 * @param u_name the u_name to set
	 */
	public void setU_name(String u_name) {
		this.u_name = u_name;
	}

	/**
	 * @return the u_ch_id
	 */
	public long getU_ch_id() {
		return u_ch_id;
	}

	/**
	 * @param u_ch_id the u_ch_id to set
	 */
	public void setU_ch_id(long u_ch_id) {
		this.u_ch_id = u_ch_id;
	}

	/**
	 * @return the u_send_ip
	 */
	public String getU_send_ip() {
		return u_send_ip;
	}

	/**
	 * @param u_send_ip the u_send_ip to set
	 */
	public void setU_send_ip(String u_send_ip) {
		this.u_send_ip = u_send_ip;
	}

	/**
	 * @return the u_loc_county
	 */
	public int getU_loc_county() {
		return u_loc_county;
	}

	/**
	 * @param u_loc_county the u_loc_county to set
	 */
	public void setU_loc_county(int u_loc_county) {
		this.u_loc_county = u_loc_county;
	}

	/**
	 * @return the u_loc_province
	 */
	public int getU_loc_province() {
		return u_loc_province;
	}

	/**
	 * @param u_loc_province the u_loc_province to set
	 */
	public void setU_loc_province(int u_loc_province) {
		this.u_loc_province = u_loc_province;
	}

	/**
	 * @return the m_mm_id
	 */
	public long getM_mm_id() {
		return m_mm_id;
	}

	/**
	 * @param m_mm_id the m_mm_id to set
	 */
	public void setM_mm_id(long m_mm_id) {
		this.m_mm_id = m_mm_id;
	}

	/**
	 * @return the m_mm_feature
	 */
	public List<String> getM_mm_feature() {
		return m_mm_feature;
	}

	/**
	 * @param m_mm_feature the m_mm_feature to set
	 */
	public void setM_mm_feature(List<String> m_mm_feature) {
		this.m_mm_feature = m_mm_feature;
	}

	/**
	 * @return the m_themes_list
	 */
	public List<Long> getM_themes_list() {
		return m_themes_list;
	}

	/**
	 * @param m_themes_list the m_themes_list to set
	 */
	public void setM_themes_list(List<Long> m_themes_list) {
		this.m_themes_list = m_themes_list;
	}

	/**
	 * @return the m_topics_list
	 */
	public List<Long> getM_topics_list() {
		return m_topics_list;
	}

	/**
	 * @param m_topics_list the m_topics_list to set
	 */
	public void setM_topics_list(List<Long> m_topics_list) {
		this.m_topics_list = m_topics_list;
	}

	/**
	 * @return the m_rules_list
	 */
	public List<Long> getM_rules_list() {
		return m_rules_list;
	}

	/**
	 * @param m_rules_list the m_rules_list to set
	 */
	public void setM_rules_list(List<Long> m_rules_list) {
		this.m_rules_list = m_rules_list;
	}

	/**
	 * @return the m_year
	 */
	public int getM_year() {
		return m_year;
	}

	/**
	 * @param m_year the m_year to set
	 */
	public void setM_year(int m_year) {
		this.m_year = m_year;
	}

	/**
	 * @return the m_month
	 */
	public int getM_month() {
		return m_month;
	}

	/**
	 * @param m_month the m_month to set
	 */
	public void setM_month(int m_month) {
		this.m_month = m_month;
	}

	/**
	 * @return the m_day
	 */
	public int getM_day() {
		return m_day;
	}

	/**
	 * @param m_day the m_day to set
	 */
	public void setM_day(int m_day) {
		this.m_day = m_day;
	}

	/**
	 * @return the m_hour
	 */
	public int getM_hour() {
		return m_hour;
	}

	/**
	 * @param m_hour the m_hour to set
	 */
	public void setM_hour(int m_hour) {
		this.m_hour = m_hour;
	}

	/**
	 * @return the tag
	 */
	public int getTag() {
		return tag;
	}

	/**
	 * @param tag the tag to set
	 */
	public void setTag(int tag) {
		this.tag = tag;
	}
	
	public String toString(){
		return "pubtime: " + m_publish_time + ", wxid: " + u_ch_id + ", chroomid: " + m_chat_room + ", content: " + m_content +
				", tag: " + tag;
	}
	
	public int getM_dom_for(){
		return m_dom_for;
	}
	
	public void setM_dom_for(int m_dom_for){
		this.m_dom_for=m_dom_for;
	}
	
	public int getM_country_code(){
		return m_country_code;
	}
	
	public void setM_country_code(int m_country_code){
		this.m_country_code=m_country_code;
	}
	
	public void setM_mm_url(String m_mm_url){
		this.m_mm_url=m_mm_url;
	}
	
	public String getM_mm_url(){
		return this.m_mm_url;
	}
	
	public void addFeatureList(List<String> list){
		for(String st:list)
			this.m_mm_feature.add(st);
	}
	
	public void addFeature(String str){
		this.m_mm_feature.add(str);
	}
	
	public void addThemesList(List<Long> list){
		for(Long l:list)
			this.m_themes_list.add(l);
	}
	
	public void addThemes(long l){
		this.m_themes_list.add(l);
	}
	
	public void addTopicsList(List<Long> list){
		for(Long l:list)
			this.m_topics_list.add(l);
	}
	
	public void addTopics(long t){
		this.m_topics_list.add(t);
	}
	
	public void addRulesList(List<Long> list){
		for(long l:list)
			this.m_rules_list.add(l);
	}
	
	public void addRules(long r){
		this.m_rules_list.add(r);
	}
	
	public void setM_url_count(int m_url_count){
		this.m_url_count=m_url_count;
	}
	
	public int getM_url_count(){
		return m_url_count;
	}
	
	public void resetAllFields(){
		g_id=null;
		m_publish_time=0L;
		m_insert_time=0L;
		m_chat_room=0L;
		m_ch_id=null;
		m_type=0;
		m_content=null;
		m_language=null;
		u_g_ch_key=null;
		u_name=null;
		u_ch_id=0L;
		u_send_ip=null;
		u_loc_county=0;
		u_loc_province=0;
		m_mm_id=0;
		m_mm_feature.clear();
		m_themes_list.clear();
		m_topics_list.clear();
		m_rules_list.clear();
		m_year=0;
		m_month=0;
		m_day=0;
		m_hour=0;
		m_dom_for=0;
		m_country_code=0;
		m_mm_url=null;
		m_url_count=0;
		tag=0;
	}
}
