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
 * 图片地址查询线程类
 * @author Administrator
 */
public class PicThread extends Thread {
	private CountDownLatch threadsSignal;//线程数
	private List<Double> ListPic = new ArrayList<Double>();//存放结果
	private String str_xml = "";
	private String queryType = "";
	
	
	//构造方法
	public PicThread(CountDownLatch threadsSignal, String str_xml, 
			String queryType, List<Double> listPic) {
		this.threadsSignal = threadsSignal;//线程数
		this.ListPic = listPic;//存放结果
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
			//获取结果
			Document document = (Document) DocumentHelper.parseText((String)s[0]);//String转化为XML
			Element root = document.getRootElement();
			Element head = (Element) root.selectNodes("head").get(0);
			String success = head.element("success").getText();//是否查询成功
			if("1".equals(success.trim())){
				System.out.println("记录数：" + Integer.parseInt(head.element("count").getText()) + "条");
			}
			
			Date date2 = new Date();
			double d = (date2.getTime()-date1.getTime());
			ListPic.add(d);
			threadsSignal.countDown();// 线程计数器减1,执行完操作后在
		} catch (Exception e) {
			threadsSignal.countDown();// 线程计数器减1,执行完操作后在
			Thread.currentThread().yield();
		}
	}
}
