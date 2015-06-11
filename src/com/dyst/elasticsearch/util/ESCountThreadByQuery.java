package com.dyst.elasticsearch.util;

import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;

import org.elasticsearch.index.query.FilterBuilder;

import com.dyst.elasticsearch.ESsearcherFilter;
/**
 * 查询ES库记录总数
 * @author Administrator
 */
public class ESCountThreadByQuery extends Thread {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private CountDownLatch threadsSignal;
	private FilterBuilder query = null;
	public int count;
	private String bussiness;

	public ESCountThreadByQuery(CountDownLatch threadsSignal, FilterBuilder query,String bussiness) {
		this.threadsSignal = threadsSignal;
		this.query = query;
		this.bussiness = bussiness;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void run() {
		try {
			ESsearcherFilter essearch = new ESsearcherFilter();
			count = essearch.getTdcpgjcxCount(query, bussiness);
			threadsSignal.countDown();// 线程计数器减1,执行完操作后在
		} catch (Exception e) {
			threadsSignal.countDown();// 线程计数器减1,执行完操作后在
			Thread.currentThread().yield();
		}
	}
}