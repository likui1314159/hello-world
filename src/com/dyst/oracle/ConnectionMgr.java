package com.dyst.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


public class ConnectionMgr {
	public static Connection  getConnection(){
		Connection con =null;
		
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String url ="jdbc:oracle:thin:@10.42.31.89:1522:sunlight";
			String user ="zxxt";
			String password = "sunlightzxxt";
			con=DriverManager.getConnection(url, user, password);
			
		}catch(Exception e){
			e.printStackTrace();
			
		}
		return  con;
		
	}
	public static  void close (Connection con,Statement st,ResultSet rs){
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

}