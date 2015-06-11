package com.dyst.oracle;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.dyst.entites.Jcd;

public class JcdOracle {
	public static List<Jcd> jcdList = new ArrayList<Jcd>();
	
	/**
	 * 加载监测点对应的图片存放路径
	 */
	public static void getJcds(){
		//连接数据库
		DBConnectionManager dbCon = null;
		Connection connection = null;
		QueryRunner qr = new QueryRunner();
		try {
			dbCon = DBConnectionManager.getInstance();
			connection = dbCon.getConnection("db");
			
			//执行查询
			String sql = "select id, tpcflj from jcd where tpcflj is not null";
			jcdList = (List<Jcd>)qr.query(connection, sql, new BeanListHandler<Jcd>(Jcd.class));
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(connection != null){
				dbCon.freeConnection("db", connection);
			}
		}
	}
	
	/**
	 * 根据监测点id返回图片存放路径
	 */
	public static String getTpcflj(String jcdid){
		if(jcdid == null || "".equals(jcdid)){
			return "";
		}

		for(int i = 0;i < jcdList.size();i++){
			Jcd jcd = jcdList.get(i);
			if(jcdid.equals(jcd.getId())){
				return jcd.getTpcflj();
			}
		}
		return "";
	}
}
