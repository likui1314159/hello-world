package com.dyst.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.codehaus.xfire.client.Client;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * ͼƬ��ַ��ѯ�߳���
 * @author Administrator
 */
public class PicThread extends Thread {
	private CountDownLatch threadsSignal;//�߳���
	private List<Double> ListPic = new ArrayList<Double>();//��Ž��
	private String str_xml = "";
	private String queryType = "";
	
	
	//���췽��
	public PicThread(CountDownLatch threadsSignal, String str_xml, 
			String queryType, List<Double> listPic) {
		this.threadsSignal = threadsSignal;//�߳���
		this.ListPic = listPic;//��Ž��
		this.str_xml = str_xml;
		this.queryType = queryType;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void run() {
		try {
			Date date1 = new Date();
			Client client = new Client(new URL("http://100.100.37.40:8989/dyst/services/InAccess?wsdl"));
			
			client.invoke("executes", new String[] {"01", queryType, "hello,world", "1" ,str_xml});
			Object[] s = client.invoke("executes", new String[] {"01", queryType, "hello,world", "0" ,str_xml});
			//��ȡ���
			Document document = (Document) DocumentHelper.parseText((String)s[0]);//Stringת��ΪXML
			Element root = document.getRootElement();
			Element head = (Element) root.selectNodes("head").get(0);
			String success = head.element("success").getText();//�Ƿ��ѯ�ɹ�
			if("1".equals(success.trim())){
				System.out.println("��¼����" + Integer.parseInt(head.element("count").getText()) + "��");
			}
			
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
