package com.dyst.oracle;

import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;

public class OracleCountThread extends Thread {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private CountDownLatch threadsSignal;
	private String sql = "";
	public int count =0;

	public OracleCountThread(CountDownLatch threadsSignal, String sql) {
		this.threadsSignal = threadsSignal;//
		this.sql = sql;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		try {
			Search search = new Search();
			count = search.getTDCPGJCXCount(sql);
			threadsSignal.countDown();// �̼߳�������1,ִ�����������
		} catch (Exception e) {
			threadsSignal.countDown();// �̼߳�������1,ִ�����������
			Thread.currentThread().stop();
			// System.exit();
		}
	}
}
