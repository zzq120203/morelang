package cn.ac.iie.Util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LargeBitmap {
	public final static Logger LOG = LoggerFactory.getLogger(LargeBitmap.class);
	public String LBDir = null;
	
	private class LBEntry {
		public int idx;
		public long ts;
		
		public LBEntry(int idx) {
			this.idx = idx;
			this.ts = System.currentTimeMillis();
		}
	}
	
	public static HashMap<String, LBEntry> bitArrayMap = new HashMap<String, LBEntry>();
	
	static { 
		System.loadLibrary("gana");
	}
	
	public static native int acInit();
	
	public static native int acFina();
	
	public static native int allocBa(int type, String pathname);
	
	public static native void freeBa(int idx);
	
	public static native int setBit(int idx, long offset, int bit);
	
	public static native int getBit(int idx, long offset);
	
	public static native int countBits(int idx);
	
	public LargeBitmap(String dir) {
		// make sure the dir exists
		new File(dir).mkdirs();
		if (dir.endsWith("/"))
			LBDir = dir;
		else
			LBDir = dir + "/";
		
		acInit();
		new cleanThread().start();
	}
	
	public void closeAll() {
		acFina();
	}
	
	public class cleanThread extends Thread {
		public long lastTs = System.currentTimeMillis();

	    public void run() {
	        if (System.currentTimeMillis() - lastTs >= 30000) {
	        	ArrayList<String> toDel = new ArrayList<String>();
	        	
	        	synchronized (bitArrayMap) {
	        		for (Map.Entry<String, LBEntry> e : bitArrayMap.entrySet()) {
	        			LBEntry lbe = e.getValue();

	        			if (System.currentTimeMillis() - lbe.ts >= 3600000) {
	        				// open time over 1 hour
	        				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	        				String key = LBDir + sdf.format(new Date());

	        				if (key.equals(e.getKey())) {
	        					// this is current day, ignore it
	        					continue;
	        				} else {
	        					// add it to clean list
	        					toDel.add(e.getKey());
	        				}
	        			}
	        		}
	        		for (String d : toDel) {
	        			LBEntry lbe = bitArrayMap.remove(d);
	        			
	        			if (lbe != null) {
	        				freeBa(lbe.idx);
	        			}
	        		}
	        	}
	        }
	    }
	}
	
	public void LBSet(String pathname, long offset) {
		LBEntry lbe = bitArrayMap.get(pathname);
		
		if (lbe == null) {
			synchronized (bitArrayMap) {
				lbe = bitArrayMap.get(pathname);
				if (lbe == null) {
					int idx = allocBa(0, pathname);
					
					if (idx < 0) {
						LOG.info("Allocate BitArray '{}' failed w/ " + idx, pathname);
						return;
					}
					lbe = new LBEntry(idx);
					bitArrayMap.put(pathname, lbe);
					LOG.info("Allocate BitArray " + pathname + "@" + lbe.idx);
				}
			}
		}
		setBit(lbe.idx, offset, 1);
	}
	
	public int LBGet(String pathname, long offset) {
		LBEntry lbe = bitArrayMap.get(pathname);
		
		if (lbe == null) {
			synchronized (bitArrayMap) {
				lbe = bitArrayMap.get(pathname);
				if (lbe == null) {
					int idx = allocBa(0, pathname);
					
					if (idx < 0) {
						LOG.info("Allocate BitArray failed w/ " + idx);
						return idx;
					}
					lbe = new LBEntry(idx);
					bitArrayMap.put(pathname, lbe);
				}
			}
		}
		return getBit(lbe.idx, offset);
	}
	
	public long LBCount(String pathname) {
		LBEntry lbe = bitArrayMap.get(pathname);
		
		if (lbe == null) {
			synchronized (bitArrayMap) {
				lbe = bitArrayMap.get(pathname);
				if (lbe == null) {
					int idx = allocBa(0, pathname);
					
					if (idx < 0) {
						LOG.info("Allocate BitArray failed w/ " + idx);
						return idx;
					}
					lbe = new LBEntry(idx);
					bitArrayMap.put(pathname, lbe);
				}
			}
		}
		return countBits(lbe.idx);
	}
}
