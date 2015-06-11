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
	 * 两种查询总数方式
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
				"", "");//准备查询条件
		query = ESutil.getQueryBuilderByCon("2014-04-21 00:00:00", "2014-05-02 00:00:00", "", "", "", "", "", "", "", "", 
				"", "");//准备查询条件
		Date d = new Date();
		try {
			int queryCount = essearch.getTdcpgjcxCount(query, "");
			 queryCount = essearch.getTdcpgjcxCount(filter, "");
			System.out.println("记录总数："+queryCount);
			System.out.println("查询时间:"+(System.currentTimeMillis()-d.getTime())/1000d+"秒");
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	/**
	 * 查询索引
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
		System.out.println(d/1000+"秒");
	}
	@Test
	public void TestEsReg() {
		try {
//			ESsearcherFilter essearch = new ESsearcherFilter();
			ESClientManager ecclient = ESClientManager.getInstance();
			Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
			BoolQueryBuilder query = boolQuery();
//			String regexp = "粤.*[02468].*";//车牌号中包含02468数字的车牌
//			String regexp = "粤.{5}[02468]";//车牌号中包含02468数字的车牌
			String regexp = "粤.{4}[02468].";
			String regexp1 = "粤.{4}[02468].";
			String regexp2 = "粤....[02468].";
			
			//minimumNumberShouldMatch，最小匹配项
			query.should(QueryBuilders.regexpQuery("cphm1", regexp)).minimumNumberShouldMatch(1);
//			query.should(QueryBuilders.regexpQuery("cphm1", regexp1)).minimumNumberShouldMatch(1);
//			query.should(QueryBuilders.regexpQuery("cphm1", regexp2)).minimumNumberShouldMatch(1);
			
			query.must(QueryBuilders.termQuery("qpsfwc", "1"));
			//prepareSearch  索引，setTypes  索引类型，两个均可给定多个值，
			SearchResponse response = client.prepareSearch("sb").setTypes("sb")
					 .setQuery(query)//语句条件
//					 .setSearchType(SearchType.QUERY_AND_FETCH)
					 .setFrom(0).setSize(10)//从开始取，取多少数据
					 .setTimeout(TimeValue.timeValueMillis(1000))//设置超时时间
					 .setExplain(false)//不对查询数据进行解析
//   				 .addFields(new String[]{"cphm1","jcdid","cplx1","tgsj","cdid","tpid1",
//   							 "tpid2","tpid3","tpid4","tpid5","sd","cdid","cb"})
//					 .addSort("tgsj", SortOrder.DESC)//排序
					 .execute().actionGet();//执行
			
			SearchHits hits = response.getHits();//获取结果
			//获取失败的分片The failed number of shards the search was executed on.
			System.out.println("查询失败分片数："+response.getFailedShards());
			//How long the search took in milliseconds.查询使用的时间，单位：毫秒
			System.out.println("查询共耗时："+response.getTookInMillis()/1000d+"秒");
			//The total number of shards the search was executed on.
			//查询的所有分片数
			System.out.println("查询分片数："+response.getTotalShards());
			System.out.println("是否超时"+response.isTimedOut());//是否超时
			//获取消息头信息
//			System.out.println(response.getHeaders());
	       //The successful number of shards the search was executed on.成功查询的分片
			System.out.println(response.getSuccessfulShards());
			RestStatus rs = response.status();
			System.out.println("本次查询状态："+rs.getStatus());
			System.out.println();
			
			
			System.out.println("符合条件记录总数："+((Long)hits.getTotalHits()).intValue());
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
				//Oracle库和ES库查询分界点,,,参数可配置
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
