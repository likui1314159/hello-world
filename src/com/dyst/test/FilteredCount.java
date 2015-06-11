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
	  * 使用prepareCount查询方式，如果生成的条件是filter的方式，则使用时
	  * 需要设置一个query，该query不能为空，如果查询全部数据，可以使用matchAllQuery，
	  */
    public  void countByFiltered(String cphid,String jcdid,String cplx,String begintime,String endtime) {   
    	ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
    	try {
			MatchAllQueryBuilder query = matchAllQuery();
//			BoolQueryBuilder query = boolQuery();
//			query.must(QueryBuilders.termQuery("cphm1", "粤A045J8"));
			FilterBuilder filter =  ESutil.getFilterByCon
   		 (begintime, endtime, "", cphid, cplx, "", jcdid, "", "", "", "", "02");
			Date date1 = new Date();
			CountResponse response = client.prepareCount("sb").setTypes("sb")
			                         .setQuery(QueryBuilders.filteredQuery(query,filter))
			                         .execute().actionGet();
			Date date2 = new Date();
			double d = (date2.getTime()-date1.getTime());
			System.out.println("filtered查询耗时："+d/1000+"秒");
			System.out.println( "-总记录数:"+response.getCount());
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(client!=null){
//				ecclient.release();
			}
		}
    } 
  /*
   * 使用prepareSearch查询方式，setSearchType(SearchType.COUNT)设置查询方式为查询记录总数
   * 可以直接分配条件filter
   */
  
    public  void countByFilter(String cphid,String jcdid,String cplx,String begintime,String endtime) {   
    	ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
	
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
			System.out.println("filter查询耗时："+d/1000+"秒");
			System.out.println( "-总记录数:"+response.getHits().getTotalHits());
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(client!=null){
//				ecclient.freeConnection(name, con);
			}
		}
    } 
}
