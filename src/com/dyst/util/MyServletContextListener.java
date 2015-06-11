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
 * ��ʼ������
 */
public class MyServletContextListener implements ServletContextListener {
	//��������Ϣ����
	public static List<Jjhomd> jjhomdList = new ArrayList<Jjhomd>(); 
	
	// ��ʼ�����ط���
	public void contextInitialized(ServletContextEvent arg0) {
		try {
//			System.out.println("��ʼ������11");
			//��ʼ�������ļ���Ϣ
			Config.getInstance();
			
			//oralce���ݿ����ӳ�
			DBConnectionManager.getInstance();
			
			//���ؼ�����Ϣ�������Ӧ��ͼƬ�洢ip��
			createGetJcdTimer();
			
			//����һ��������
			createGetJjhomdTimer();
			
			//ES���ݿ����ӳ�
			ESClientManager.getInstance(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// tomcat����ʱִ�з���
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("Web�������ر�");
		// tomcat �ر�Web����ʱ�ر�ES���ӳ�����
		ESClientManager.getInstance().release();// �ͷ���������
		DBConnectionManager.getInstance().release();// �ͷ���������
	}
	
	/**
	 * ��ʱ���ؼ����Ӧ��ͼƬ�洢ip
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
	 * ��ʱ����һ��������
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
		}, 5000, time);//�ӳ�5�����
	}
}