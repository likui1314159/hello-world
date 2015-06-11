package com.dyst.test;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;

import com.dyst.elasticsearch.ESsearcherFilter;
import com.dyst.elasticsearch.util.ESClientManager;
import com.dyst.elasticsearch.util.ESutil;
import com.dyst.util.InterUtil;

public class Testes {
	
	/*
	 * ���ֲ�ѯ������ʽ
	 */
	@Test
	public void testCount(){
		FilterBuilder filter = null;
		QueryBuilder query = null;
		ESsearcherFilter essearch = new ESsearcherFilter();
		String jcdid = "";
		for(int i=0;i<2000;i++){
			jcdid += "20504602"+",";
		}
		filter = ESutil.getFilterByCon("2014-04-21 00:00:00", "2014-05-02 00:00:00", "", "", "", "", "", "", "", "", 
				"", "");//׼����ѯ����
		query = ESutil.getQueryBuilderByCon("2014-04-21 00:00:00", "2014-05-02 00:00:00", "", "", "", "", "", "", "", "", 
				"", "");//׼����ѯ����
		Date d = new Date();
		try {
			int queryCount = essearch.getTdcpgjcxCount(query, "");
			 queryCount = essearch.getTdcpgjcxCount(filter, "");
			System.out.println("��¼������"+queryCount);
			System.out.println("��ѯʱ��:"+(System.currentTimeMillis()-d.getTime())/1000d+"��");
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	/**
	 * ��ѯ����
	 */
	@org.junit.Test
	public void tdcpgjcxCount() {
		Date date1 = new Date();
    	String jcd = "10306101,10301102,10301101,20602701,10107801," +
    			"10107802,20101812,20101813,20301814,20807601,20807602," +
    			"10107701,10107702,30707901,30707902,20807501,20807502,";// +
//    			"30701903,30701902,20104606,20400804";
    	FilteredCount search = new FilteredCount();
		try {
			search.countByFilter(
					"", jcd, "0,1", "2014-02-6 00:00:00","2014-03-22 00:25:33"
			);
			
			search.countByFiltered(
					"", jcd, "0,1", "2014-02-6 00:00:00","2014-03-22 00:25:33"
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Date date2 = new Date();
		double d = (date2.getTime()-date1.getTime());
//		System.out.println(d);
		System.out.println(d/1000+"��");
	}
	@Test
	public void TestEsReg() {
		try {
//			ESsearcherFilter essearch = new ESsearcherFilter();
			ESClientManager ecclient = ESClientManager.getInstance();
			Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
			BoolQueryBuilder query = boolQuery();
//			String regexp = "��.*[02468].*";//���ƺ��а���02468���ֵĳ���
//			String regexp = "��.{5}[02468]";//���ƺ��а���02468���ֵĳ���
			String regexp = "��.{4}[02468].";
			String regexp1 = "��.{4}[02468].";
			String regexp2 = "��....[02468].";
			
			//minimumNumberShouldMatch����Сƥ����
			query.should(QueryBuilders.regexpQuery("cphm1", regexp)).minimumNumberShouldMatch(1);
//			query.should(QueryBuilders.regexpQuery("cphm1", regexp1)).minimumNumberShouldMatch(1);
//			query.should(QueryBuilders.regexpQuery("cphm1", regexp2)).minimumNumberShouldMatch(1);
			
			query.must(QueryBuilders.termQuery("qpsfwc", "1"));
			//prepareSearch  ������setTypes  �������ͣ��������ɸ������ֵ��
			SearchResponse response = client.prepareSearch("sb").setTypes("sb")
					 .setQuery(query)//�������
//					 .setSearchType(SearchType.QUERY_AND_FETCH)
					 .setFrom(0).setSize(10)//�ӿ�ʼȡ��ȡ��������
					 .setTimeout(TimeValue.timeValueMillis(1000))//���ó�ʱʱ��
					 .setExplain(false)//���Բ�ѯ���ݽ��н���
//   				 .addFields(new String[]{"cphm1","jcdid","cplx1","tgsj","cdid","tpid1",
//   							 "tpid2","tpid3","tpid4","tpid5","sd","cdid","cb"})
//					 .addSort("tgsj", SortOrder.DESC)//����
					 .execute().actionGet();//ִ��
			
			SearchHits hits = response.getHits();//��ȡ���
			//��ȡʧ�ܵķ�ƬThe failed number of shards the search was executed on.
			System.out.println("��ѯʧ�ܷ�Ƭ����"+response.getFailedShards());
			//How long the search took in milliseconds.��ѯʹ�õ�ʱ�䣬��λ������
			System.out.println("��ѯ����ʱ��"+response.getTookInMillis()/1000d+"��");
			//The total number of shards the search was executed on.
			//��ѯ�����з�Ƭ��
			System.out.println("��ѯ��Ƭ����"+response.getTotalShards());
			System.out.println("�Ƿ�ʱ"+response.isTimedOut());//�Ƿ�ʱ
			//��ȡ��Ϣͷ��Ϣ
//			System.out.println(response.getHeaders());
	       //The successful number of shards the search was executed on.�ɹ���ѯ�ķ�Ƭ
			System.out.println(response.getSuccessfulShards());
			RestStatus rs = response.status();
			System.out.println("���β�ѯ״̬��"+rs.getStatus());
			System.out.println();
			
			
			System.out.println("����������¼������"+((Long)hits.getTotalHits()).intValue());
			for (int i = 0; i < hits.getHits().length; i++) {
				System.out.println(
						hits.getAt(i).getSource().get("cphm1") + "---"
						+ hits.getAt(i).getSource().get("tpid1") + "---"
						+ hits.getAt(i).getSource().get("cplx1") + "----"
						+ hits.getAt(i).getSource().get("jcdid") + "----");
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
        
	}
	@Test
	public void TestReg() {
		try {
			Pattern p = Pattern.compile("^.*((?<!\\d)\\d+).*$");
	        Matcher m = p.matcher("aj1sk2ld1v312");
	        if(m.matches()){
	        	 System.out.println(m.group(1));
	        }
	        Date midDate = null;
			Date ksDate = null;
			Date jzDate = null;
				//Oracle���ES���ѯ�ֽ��,,,����������
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				midDate = df.parse(InterUtil.getTime(Integer.parseInt("-1")));
				System.out.println(df.format(midDate));
				System.out.println("2014-04-21 00:00:00:122".substring(0,19));
				System.out.println("2014-04-21 00:00:00:".length());
				 double k = 4/2d+0.5;//       n/2+0.5  jcdsz.length/6
				    k = Math.floor(k);
				    System.out.println(k);
				    System.out.println(Math.ceil(3.4));
		} catch (Exception e) {
			e.printStackTrace();
		}
        
	}
}
