package com.dyst.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;

import javax.servlet.ServletContextListener;

import com.dyst.elasticsearch.util.ESClientManager;
import com.dyst.entites.Jjhomd;
import com.dyst.oracle.DBConnectionManager;
import com.dyst.oracle.JcdOracle;
import com.dyst.oracle.JjhomdOracle;

/**
 * 初始化加载
 */
public class MyServletContextListener implements ServletContextListener {
	//红名单信息加载
	public static List<Jjhomd> jjhomdList = new ArrayList<Jjhomd>(); 
	
	// 初始化加载方法
	public void contextInitialized(ServletContextEvent arg0) {
		try {
//			System.out.println("初始化加载11");
			//初始化配置文件信息
			Config.getInstance();
			
			//oralce数据库连接池
			DBConnectionManager.getInstance();
			
			//加载监测点信息（监测点对应的图片存储ip）
			createGetJcdTimer();
			
			//加载一级红名单
			createGetJjhomdTimer();
			
			//ES数据库连接池
			ESClientManager.getInstance(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// tomcat销毁时执行方法
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("Web服务器关闭");
		// tomcat 关闭Web服务时关闭ES连接池连接
		ESClientManager.getInstance().release();// 释放所有连接
		DBConnectionManager.getInstance().release();// 释放所有连接
	}
	
	/**
	 * 定时加载监测点对应的图片存储ip
	 */
	private void createGetJcdTimer(){
		Config config = Config.getInstance();
		String getJcdTime = config.getGetJcdTime();
		if(getJcdTime == null || "".equals(getJcdTime)){
			return;
		}
		int time = Integer.parseInt(getJcdTime);
		
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				JcdOracle.getJcds();
			}
		}, 0, time);
	}
	
	/**
	 * 定时加载一级红名单
	 */
	private void createGetJjhomdTimer(){
		Config config = Config.getInstance();
		String getJjhomdTime = config.getGetJjhomdTime();
		if(getJjhomdTime == null || "".equals(getJjhomdTime)){
			return;
		}
		int time = Integer.parseInt(getJjhomdTime);
		
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				JjhomdOracle.getJjhomds();
			}
		}, 5000, time);//延迟5秒加载
	}
}