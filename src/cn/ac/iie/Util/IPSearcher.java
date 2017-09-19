package cn.ac.iie.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbMakerConfigException;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * project test script
 * 
 * @author chenxin<chenxin619315@gmail.com>
*/
public class IPSearcher {
	public final static Logger LOG = LoggerFactory.getLogger(IPSearcher.class);
	
    public int algorithm = DbSearcher.MEMORY_ALGORITYM;
    
    public String dbFilePath;
    
    public DbConfig config;

    public DbSearcher searcher;
    
    public IPSearcher(String dbFilePath) {
    	this.dbFilePath = dbFilePath;
    	
    	File file = new File(dbFilePath);
        if ( file.exists() == false ) {
            LOG.error("Error: Invalid ip2region.db file: " + dbFilePath);
            return;
        }
        
		try {
			config = new DbConfig();
			searcher = new DbSearcher(config, dbFilePath);
		} catch (DbMakerConfigException e) {
			LOG.error(e.getMessage());
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage());
		}
        
    }
    
    public void quit() {
    	try {
    		if (searcher != null)
    			searcher.close();
    	} catch (IOException e) {
    		LOG.error(e.getMessage());
    	}
    }
    
    public String doSearch(String line) {
        DataBlock dataBlock = null;

        if (searcher == null)
        	return null;
    	try {
             //define the method
             Method method = searcher.getClass().getMethod("memorySearch", String.class);

             if ( line.length() < 2 ) return null;
             if ( Util.isIpAddress(line) == false ) {
            	 LOG.error("Error: Invalid ip address: " + line);
             } else { 
            	 dataBlock = (DataBlock) method.invoke(searcher, line);
             }
         } catch (NoSuchMethodException e) {
        	 LOG.error(e.getMessage());
         } catch (SecurityException e) {
        	 LOG.error(e.getMessage());
         } catch (IllegalAccessException e) {
        	 LOG.error(e.getMessage());
         } catch (IllegalArgumentException e) {
        	 LOG.error(e.getMessage());
         } catch (InvocationTargetException e) {
        	 LOG.error(e.getMessage());
         }
    	if (dataBlock != null)
    		return dataBlock.getRegion();
    	else
    		return null;
    }
    
    public IPLocation doSearch2Loc(String line) {
    	IPLocation loc = null;
    	String str = doSearch(line);
    	
    	if (str != null) {
    		String[] locs = str.split("\\|");
    		if (locs != null && locs.length == 5) {
    			loc = new IPLocation(locs[0], locs[1], locs[2], locs[3], locs[4]);
    		}
    	}
    	
    	return loc;
    }
    
    public static void main(String[] argv) {    
        if ( argv.length == 0 ) {
            System.out.println("| Usage: java -jar ip2region-{version}.jar [ip2region db file]");
            return;
        }
        IPSearcher ips = new IPSearcher(argv[0]);
        
        System.out.println(ips.doSearch("110.153.221.148"));
        System.out.println(ips.doSearch2Loc("110.153.221.148"));
        ips.quit();
    }
}
