package com.dyst.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


public class DateConnection {
     //数据库相关操作类
	
	/**
	 * jdbc获取数据库连接方法
	 */
	public Connection getconnect() {
		Connection conn = null;
//		Statement st = null;//通过全局类来查找，减少部署服务器时麻烦
//		Properties pro = Getoracleuser.getProperties();
//		String user = pro.getProperty("user");
//		String driver =pro.getProperty("driver");
//		String password = pro.getProperty("password");
//		String url = pro.getProperty("url");
//		try {
//			Class.forName(driver);//.newInstance();
//			conn = DriverManager.getConnection(url, user, password);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		Config config = Config.getInstance();
		try {
			Class.forName(config.getDriver());//.newInstance();
			conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * 关闭数据库连接方法
	 * 把传递过来的 connection、statement、resultset关闭
	 * @param con
	 * @param st
	 * @param rs
	 */
	public void close (Connection con,Statement st,ResultSet rs){
		try{
			if(rs!=null){
				rs.close();
			}
			if(st!=null){
				st.close();
			}
			if(con!=null){
				con.close();
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}	
	//测试主函数
	public static void main(String[] args) {
		DateConnection conn = new DateConnection();
		conn.getconnect();
	}
}
