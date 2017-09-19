package cn.ac.iie.Util;

import java.io.File;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class GroupGather {
	public final static Logger LOG = LoggerFactory.getLogger(LargeBitmap.class);
	public String GGDir = null;
	
	private class GGEntry {
		public int idx;
		public long ts;
		
		public GGEntry(int idx) {
			this.idx = idx;
			this.ts = System.currentTimeMillis();
		}
	}
	
	public static HashMap<String, GGEntry> ggMap = new HashMap<String, GGEntry>();
	
	static { 
		System.loadLibrary("gana");
	}
	
	public static native int paInit();
	
	public static native int paFina();
	
	public static native int allocGpa(String pathname, long fid);
	
	public static native int freeGpa(int idx);
	
	public static native int gUpdate(int idx, long fid, long gid, long uid, long ts);
	
	public static native String gjson(int idx, long gid);
	
	public GroupGather(String dir) {
		// make sure the dir exists
		new File(dir).mkdirs();
		if (dir.endsWith("/"))
			GGDir = dir;
		else
			GGDir = dir + "/";
		
		paInit();
	}
	
	public void closeAll() {
		paFina();
	}
	
	public int ggUpdate(String pathname, long fid, long gid, long uid, long ts) {
		GGEntry gge = ggMap.get(pathname);
		
		if (gge == null) {
			synchronized (ggMap) {
				gge = ggMap.get(pathname);
				if (gge == null) {
					int idx = allocGpa(pathname, fid);
					
					if (idx < 0) {
						LOG.info("Allocate GG failed w/ " + idx);
						return idx;
					}
					gge = new GGEntry(idx);
					ggMap.put(pathname, gge);
					LOG.info("Allocate GroupGather " + pathname + "@" + gge.idx);
				}
			}
		}
		
		return gUpdate(gge.idx, fid, gid, uid, ts);
	}
	
	public GroupInfo ggi(String pathname, long fid, long gid) {
		GGEntry gge = ggMap.get(pathname);
		
		if (gge == null) {
			synchronized (ggMap) {
				gge = ggMap.get(pathname);
				if (gge == null) {
					int idx = allocGpa(pathname, fid);
					
					if (idx < 0) {
						LOG.info("Allocate GG failed w/ " + idx);
						return null;
					}
					gge = new GGEntry(idx);
					ggMap.put(pathname, gge);
					LOG.info("Allocate GroupGather " + pathname + "@" + gge.idx);
				}
			}
		}
		
		 String json = gjson(gge.idx, gid);
		 return JSON.parseObject(json, GroupInfo.class);
	}
}
