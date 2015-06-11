package com.dyst.elasticsearch.util;

import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;

import org.elasticsearch.index.query.FilterBuilder;

import com.dyst.elasticsearch.ESsearcherFilter;
/**
 * ��ѯES���¼����
 * @author Administrator
 */
public class ESCountThread extends Thread {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private CountDownLatch threadsSignal;
	private FilterBuilder filter=null;
	public int count;
	private String bussiness;

	public ESCountThread(CountDownLatch threadsSignal, FilterBuilder filter,String bussiness) {
		this.threadsSignal = threadsSignal;
		this.filter = filter;
		this.bussiness = bussiness;
	}
	@SuppressWarnings("static-access")
	@Override
	public void run() {
		try {
			ESsearcherFilter essearch = new ESsearcherFilter();
			count =essearch.getTdcpgjcxCount(filter,bussiness);
			threadsSignal.countDown();// �̼߳�������1,ִ�����������
		} catch (Exception e) {
			threadsSignal.countDown();// �̼߳�������1,ִ�����������
			Thread.currentThread().yield();
		}
	}
}
