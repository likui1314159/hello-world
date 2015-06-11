package com.dyst.elasticsearch.util;

import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;

import com.dyst.elasticsearch.ESsearcherFilter;

public class ESThreadByQuery extends Thread {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private CountDownLatch threadsSignal;
	private FilterBuilder query=null;
	public SearchHits hits  ;
	private int from,pagsize;
	private String bussiness;
	private String sort="";
	private String sortType = "";
	
	public ESThreadByQuery(CountDownLatch threadsSignal, FilterBuilder query, int from, int pagsize,
			String bussiness,String sort,String sortType) {
		this.threadsSignal = threadsSignal;
		this.query = query;
		this.from = from ;
		this.pagsize =pagsize;
		this.bussiness=bussiness;
		this.sort = sort;
		this.sortType = sortType;
	}
	@SuppressWarnings("static-access")
	@Override
	public void run() {
		try {
			ESsearcherFilter essearch = new ESsearcherFilter();
			hits =essearch.tdcpgjcx(query,from,pagsize,bussiness,sort,sortType);//通过filter查询ES库
			threadsSignal.countDown();// 线程计数器减1,执行完操作后在
		} catch (Exception e) {
			threadsSignal.countDown();// 线程计数器减1,执行完操作后在
			Thread.currentThread().yield();
			// System.exit();
		}
	}
}