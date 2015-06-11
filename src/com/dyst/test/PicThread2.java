package com.dyst.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.codehaus.xfire.client.Client;

/**
 * ͼƬ��ַ��ѯ�߳���
 * @author Administrator
 */
public class PicThread2 extends Thread {
	private CountDownLatch threadsSignal;//�߳���
	private List<Double> ListPic = new ArrayList<Double>();//��Ž��
	private String str_xml = "";
	
	
	//���췽��
	public PicThread2(CountDownLatch threadsSignal, String str_xml, 
			List<Double> listPic) {
		this.threadsSignal = threadsSignal;//�߳���
		this.ListPic = listPic;//��Ž��
		this.str_xml = str_xml;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void run() {
		try {
			Date date1 = new Date();
			Client client = new Client(new URL("http://10.103.1.141:9080/dyst/services/InAccess?wsdl"));
			
			client.invoke("executes", new String[] {"01", "02", "hello,world", "0" , str_xml} );

			Date date2 = new Date();
			double d = (date2.getTime()-date1.getTime());
			ListPic.add(d);
			threadsSignal.countDown();// �̼߳�������1,ִ�����������
		} catch (Exception e) {
			threadsSignal.countDown();// �̼߳�������1,ִ�����������
			Thread.currentThread().yield();
		}
	}
}
