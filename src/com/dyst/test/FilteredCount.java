package com.dyst.test;

import static org.elasticsearch.index.query.QueryBuilders.*;

import java.util.Date;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.dyst.elasticsearch.util.ESClientManager;
import com.dyst.elasticsearch.util.ESutil;

public class FilteredCount {
	 /*
	  * ʹ��prepareCount��ѯ��ʽ��������ɵ�������filter�ķ�ʽ����ʹ��ʱ
	  * ��Ҫ����һ��query����query����Ϊ�գ������ѯȫ�����ݣ�����ʹ��matchAllQuery��
	  */
    public  void countByFiltered(String cphid,String jcdid,String cplx,String begintime,String endtime) {   
    	ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
    	try {
			MatchAllQueryBuilder query = matchAllQuery();
//			BoolQueryBuilder query = boolQuery();
//			query.must(QueryBuilders.termQuery("cphm1", "��A045J8"));
			FilterBuilder filter =  ESutil.getFilterByCon
   		 (begintime, endtime, "", cphid, cplx, "", jcdid, "", "", "", "", "02");
			Date date1 = new Date();
			CountResponse response = client.prepareCount("sb").setTypes("sb")
			                         .setQuery(QueryBuilders.filteredQuery(query,filter))
			                         .execute().actionGet();
			Date date2 = new Date();
			double d = (date2.getTime()-date1.getTime());
			System.out.println("filtered��ѯ��ʱ��"+d/1000+"��");
			System.out.println( "-�ܼ�¼��:"+response.getCount());
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(client!=null){
//				ecclient.release();
			}
		}
    } 
  /*
   * ʹ��prepareSearch��ѯ��ʽ��setSearchType(SearchType.COUNT)���ò�ѯ��ʽΪ��ѯ��¼����
   * ����ֱ�ӷ�������filter
   */
  
    public  void countByFilter(String cphid,String jcdid,String cplx,String begintime,String endtime) {   
    	ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
	
    	try {
		 FilterBuilder filter =  ESutil.getFilterByCon
   		 (begintime, endtime, "", cphid, cplx, "", jcdid, "", "", "", "", "02");
			Date date1 = new Date();
			SearchResponse response = client.prepareSearch("sb").setTypes("sb")
			.setFilter(filter)
			.setSearchType(SearchType.COUNT)
			.execute().actionGet();
			
			Date date2 = new Date();
			double d = (date2.getTime()-date1.getTime());
//			System.out.println(d);
			System.out.println("filter��ѯ��ʱ��"+d/1000+"��");
			System.out.println( "-�ܼ�¼��:"+response.getHits().getTotalHits());
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(client!=null){
//				ecclient.freeConnection(name, con);
			}
		}
    } 
}
