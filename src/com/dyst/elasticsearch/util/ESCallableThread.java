package com.dyst.elasticsearch.util;

import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.search.SearchHits;

public class ESCallableThread implements Callable<SearchHits>{
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	private CountDownLatch threadsSignal;
//	private FilterBuilder filter=null;
	public SearchHits hits  ;

	public ESCallableThread(CountDownLatch threadsSignal, FilterBuilder filter) {
//		this.threadsSignal = threadsSignal;//
//		this.filter = filter;
	}
	public SearchHits call() throws Exception {
//		try {
//			ESsearcherFilter essearch = new ESsearcherFilter();
//			hits =essearch.tdcpgjcx(filter);//通过filter查询ES库
//			threadsSignal.countDown();// 线程计数器减1,执行完操作后在
//		} catch (Exception e) {
//			threadsSignal.countDown();// 线程计数器减1,执行完操作后在
//			Thread.currentThread().stop();
//			// System.exit();
//		}
		return null;
	}
}