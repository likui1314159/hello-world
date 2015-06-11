package com.dyst.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


public class DateConnection {
     //���ݿ���ز�����
	
	/**
	 * jdbc��ȡ���ݿ����ӷ���
	 */
	public Connection getconnect() {
		Connection conn = null;
//		Statement st = null;//ͨ��ȫ���������ң����ٲ��������ʱ�鷳
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
	 * �ر����ݿ����ӷ���
	 * �Ѵ��ݹ����� connection��statement��resultset�ر�
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
	//����������
	public static void main(String[] args) {
		DateConnection conn = new DateConnection();
		conn.getconnect();
	}
}
