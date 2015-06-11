package com.dyst.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TestDyst {
	public static void testData(int xcs, String queryType, String cphm, 
			String qssj, String jzsj) throws Exception{
		int number = xcs;//�߳�������10��120��
		List<Double> listData = new ArrayList<Double>();//��Ӧʱ�伯��
		
		//��ѯ����������Ҫ�Ǹ�ʱ��
		String str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
    		+ "<head><pagesize></pagesize><from>0</from></head><body><data>"
			+ "<hphm>" + cphm + "</hphm><kssj>" + qssj + "</kssj><jssj>" + jzsj + "</jssj>"
			+ "<tpid></tpid><jcdid></jcdid><hpzl></hpzl><cplx></cplx><hmdCphm></hmdCphm></data></body></root>";

		CountDownLatch threadsSignal = new CountDownLatch(number);//����number���߳�
		for(int j = 1;j <= number;j++) {
			PicThread index = new PicThread(threadsSignal, str_xml, queryType, listData);//01��ʶ���ѯ��04ģ����ѯ
			index.start();
		}
		threadsSignal.await();//�ȴ������߳̽���

		double total = 0f;//��ʱ��
		List<String> list = new ArrayList<String>();
		for(int i = 0;i < listData.size();i++){
			total = total + listData.get(i);
			list.add(listData.get(i)+"");
		}
		System.out.println("ƽ����Ӧʱ�䣺" + (total/listData.size()) + "����");
		
		Collections.sort(list);
		if(list.size() > 0){
			System.out.println("��С��Ӧʱ�䣺" + list.get(0) + "����");
		}
		if(list.size() > 1){
			System.out.println("�����Ӧʱ�䣺" + list.get(list.size() - 1) + "����");
		}
		
		System.out.println("�����ʣ�" + (((number - listData.size())/(number*1.0)) * 100) + "%");
	}
	
	public static void testPic(int xcs, String tpids) throws Exception{
		int number = xcs;//�߳�������10��120��
		List<Double> listData = new ArrayList<Double>();//��Ӧʱ�伯��
		
		String[] tp = tpids.split(",");
		String xmlPic = "<?xml version='1.0' encoding='gb2312'?><root><head/><body>";
		for(int i = 0;i < tp.length;i++){
			xmlPic = xmlPic + "<tpid>" + tp[i] + "</tpid>";
		}					
		xmlPic = xmlPic + "</body></root>";
		
		CountDownLatch threadsSignal = new CountDownLatch(number);//����number���߳�
		for(int j = 1;j <= number;j++) {
			PicThread2 index = new PicThread2(threadsSignal, xmlPic, listData);//01��ʶ���ѯ��04ģ����ѯ
			index.start();
			
		}
		threadsSignal.await();//�ȴ������߳̽���

		double total = 0f;//��ʱ��
		List<String> list = new ArrayList<String>();
		for(int i = 0;i < listData.size();i++){
			total = total + listData.get(i);
			list.add(listData.get(i)+"");
		}
		System.out.println("ƽ����Ӧʱ�䣺" + (total/listData.size()) + "����");
		
		Collections.sort(list);
		if(list.size() > 0){
			System.out.println("��С��Ӧʱ�䣺" + list.get(0) + "����");
		}
		if(list.size() > 1){
			System.out.println("�����Ӧʱ�䣺" + list.get(list.size() - 1) + "����");
		}
		
		System.out.println("�����ʣ�" + (((number - listData.size())/(number*1.0)) * 100) + "%");
	}
	
	public static void main(String[] args) throws Exception {
//		testData(1, "01", "", "2014-01-01 00:00:00", "2014-01-02 23:59:59");
		
//		testPic(10, "2013120108360560131B001921,2013120108334744111B003411");
		//2013120108335849111B003221
//		2013120108332020121B000811
//		2013120108325577111B003711
//		2013120108290197111B001911
//		2013120108331524141B002231
	}
}
