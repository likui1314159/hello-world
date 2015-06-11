package com.dyst.elasticsearch.util;

import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.search.SearchHits;

import com.dyst.elasticsearch.ESsearcherFilter;

public class ESThread extends Thread {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private CountDownLatch threadsSignal;
	private FilterBuilder filter=null;
	public SearchHits hits  ;
	private int from,pagsize;
	private String bussiness;
	private String sort="";
	private String sortType = "";

	public ESThread(CountDownLatch threadsSignal, FilterBuilder filter, int from, int pagsize, 
			String bussiness,String sort,String sortType) {
		this.threadsSignal = threadsSignal;
		this.filter = filter;
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
			hits =essearch.tdcpgjcx(filter,from,pagsize,bussiness,sort,sortType);//ͨ��filter��ѯES��
			threadsSignal.countDown();// �̼߳�������1,ִ�����������
		} catch (Exception e) {
			threadsSignal.countDown();// �̼߳�������1,ִ�����������
			Thread.currentThread().yield();
			// System.exit();
		}
	}
}
