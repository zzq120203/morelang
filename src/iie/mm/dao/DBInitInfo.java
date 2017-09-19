package iie.mm.dao;

import java.util.ArrayList;
import java.util.List;

import cn.ac.iie.Confguration.Config;
/**
 * 初始化，模拟加载所有的配置文件
 * @author Ran
 *
 */
public class DBInitInfo {
	public  static List<DBbean>  beans = null;
	static{
		beans = new ArrayList<DBbean>();

		DBbean ora = new DBbean(Config.OracleDriver, Config.ConfigDBURI, Config.ConfigDBUser, Config.ConfigDBPassword, Config.OracleDriver);
		ora.setCheakPool(false);
		beans.add(ora);
		
	}
}
