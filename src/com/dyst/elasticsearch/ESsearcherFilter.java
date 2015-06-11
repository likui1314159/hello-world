package com.dyst.elasticsearch;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.ReceiveTimeoutTransportException;
import org.elasticsearch.transport.TransportSerializationException;

import com.dyst.elasticsearch.util.ESClientManager;

/**
 * 通过Filter方式实现ES查询
 * @author Administrator
 */
public class ESsearcherFilter {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	Client client = ESClient.getInstance().client;
//	Client client = es.client;
//	TransportClientPool pool = new TransportClientPool(5);
//	Client client = TransportClientPool.getTransportClient();
	/**
	 * 根据Filter和分页条件查询ＥＳ库数据
	 * @param filter  过滤器
	 * @param from    开始记录
	 * @param to      截止记录
	 * @param pagsize 每页大小数
	 * @param sort  排序字段
	 * @param sortType 排序类型
	 * @return 查询结果
	 */
	public SearchHits tdcpgjcx(FilterBuilder filter,int from ,int pagsize,String bussiness ,String sort,String sortType) {
		
//		Client client = ESClient.getInstance().client;
//		Date date1 = new Date();
		
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		
		String selectType = "sb";
//		if("03".equals(bussiness)){//未识别查询
//			selectType = "wsb";
//		}
		try {
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch("sb").setTypes(selectType)
					 .setFilter(filter)
//					 .setSearchType(SearchType.QUERY_AND_FETCH)
					 .setFrom(from).setSize(pagsize)
					 .addFields(new String[]{"cphm1","jcdid","cplx1","tgsj","cdid","tpid1",
   							 "tpid2","tpid3","tpid4","tpid5","sd","cdid","cb"})//指定查询字段
   					.setExplain(false);//不对查询数据进行解析;
					if(!"".equals(sort.trim())&&!"".equals(sortType)){//生成排序sql
						searchRequestBuilder.addSort(sort, "DESC".equals(sortType.toUpperCase())?SortOrder.DESC:SortOrder.ASC );//排序
					}
			SearchResponse response = searchRequestBuilder.execute().actionGet();
			
			SearchHits hits = response.getHits();
//			System.out.println(hits.getTotalHits() + "-总记录数");
//			Date date2 = new Date();
//			double d = (date2.getTime()-date1.getTime());
//			System.out.println("ES库查询记录共耗时："+d/1000+"秒");
			return hits;
		}catch (ReceiveTimeoutTransportException e) {
			e.printStackTrace();
//			System.out.println("数据库连接出现异常，超时"+e);
			if (client != null) {
				client.close();
			}
			return null;
		} finally {
			if (client != null) {
				ecclient.freeConnection("es", client);
			}
		}
	}
	
	/**
	 * 根据Query和分页条件查询ＥＳ库数据
	 * @param filter  过滤器
	 * @param from    开始记录
	 * @param to      截止记录
	 * @param pagsize 每页大小数
	 * @return 查询结果
	 */
	public SearchHits tdcpgjcx(QueryBuilder query,int from ,int pagsize,String bussiness) {
		
//		Client client = ESClient.getInstance().client;
		Date date1 = new Date();
		
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		
		String selectType = "sb";
//		if("03".equals(bussiness)){//未识别查询
//			selectType = "wsb";
//		}
		try {
			//prepareSearch  索引                       setTypes  索引类型
			SearchResponse response = client.prepareSearch("sb").setTypes(selectType)
					 .setQuery(query)//语句条件
//					 .setSearchType(SearchType.QUERY_AND_FETCH)
					 .setFrom(from).setSize(pagsize)//从开始取，取多少数据
					 .setExplain(false)//不对查询数据进行解析
   					 .addFields(new String[]{"cphm1","jcdid","cplx1","tgsj","cdid","tpid1",
   							 "tpid2","tpid3","tpid4","tpid5","sd","cdid","cb"})
//					 .addSort("tgsj", SortOrder.DESC)//排序
					 .execute().actionGet();//执行
			
			SearchHits hits = response.getHits();//获取结果
			
//			System.out.println(hits.getHits().length + "-ES条件查询记录数");
//			System.out.println(hits.getTotalHits() + "-ES总记录数");
			Date date2 = new Date();
			double d = (date2.getTime()-date1.getTime());
			System.out.println("ES库查询共耗时："+d/1000+"秒");
			
			return hits;
		}catch (ReceiveTimeoutTransportException e) {
			e.printStackTrace();
//			System.out.println("数据库连接出现异常，超时"+e);
			if (client != null) {
				client.close();
			}
			return null;
		} finally {
			if (client != null) {
				ecclient.freeConnection("es", client);
			}
		}
	}
	/**
	 * 通过表中的值来查询 总数,filter方式
	 */
	public Integer getTdcpgjcxCount(FilterBuilder filter,String bussiness) throws Exception {
//		Date date1 = new Date();
		
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		String selectType = "sb";
//		if("03".equals(bussiness)){//未识别查询
//			selectType = "wsb";
//		}
		try {
			MatchAllQueryBuilder query = matchAllQuery();
			Date date1 = new Date();
			//prepareSearch  索引                       setTypes  索引类型
//			SearchResponse response = client.prepareSearch("sb").setTypes(selectType)
//					 .setFilter(filter)
//					 .setSearchType(SearchType.COUNT)
//					 .setExplain(false)//不对查询数据进行解析
//				     .execute().actionGet();//执行
			CountResponse response = client.prepareCount("sb").setTypes("sb")
            .setQuery(QueryBuilders.filteredQuery(query,filter))
            .execute().actionGet();
//			SearchHits hits = response.getHits();//获取结果
			
//			System.out.println(hits.getTotalHits() + "-ES总记录数");
			Date date2 = new Date();
			double d = (date2.getTime()-date1.getTime());
			System.out.println("Filtered__ES库查询记录总数共耗时："+d/1000+"秒");
			
			return ((Long)response.getCount()).intValue();
		}catch (ReceiveTimeoutTransportException e) {
			e.printStackTrace();
			System.out.println("数据库连接出现异常，超时"+e);
			if (client != null) {
				client.close();
			}
			return 0;
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("ES数据库查询异常");
		} 
		finally {
			if (client != null) {
		    	ecclient.freeConnection("es", client);
		   }
		}
	}
	/**
	 * 通过表中的值来查询总数 ,query方式
	 */
	public Integer getTdcpgjcxCount(QueryBuilder query,String bussiness) throws Exception {
		Date date1 = new Date();
		
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		String selectType = "sb";
//		if("03".equals(bussiness)){//未识别查询
//			selectType = "wsb";
//		}
		try {
			//prepareSearch  索引                       setTypes  索引类型
			CountResponse response = client.prepareCount("sb").setTypes(selectType)
					 .setQuery(query)//语句条件
//					 .setSearchType(SearchType.COUNT)//搜索类型
//					 .setExplain(false)//是否解析数据，即解析出关键字
				     .execute().actionGet();//执行
//			SearchHits hits = response.getHits();//获取结果
			
//			System.out.println(hits.getTotalHits() + "-ES总记录数");
			Date date2 = new Date();
			double d = (date2.getTime()-date1.getTime());
			System.out.println("ES库Query查询方式，记录总数共耗时："+d/1000+"秒");
			
			return ((Long)response.getCount()).intValue();
		}catch (ReceiveTimeoutTransportException e) {
			e.printStackTrace();
			System.out.println("数据库连接出现异常，超时"+e);
			if (client != null) {
				client.close();
			}
			return 0;
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("ES数据库查询异常");
		} 
		finally {
			if (client != null) {
		    	ecclient.freeConnection("es", client);
		   }
		}
	}
	
	/**
	 * 更新ＥＳ识别记录
	 * tpid1 图片id
	 * cphm1 修改后的车牌号码
	 * cplx1 修改后的车牌类型
	 */
	public String updateEsSb(String tpid1,String cphm1,String cplx1) throws Exception {
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		try {
			//车牌号码及图片id不能为空
			if(tpid1 != null && !"".equals(tpid1.trim()) 
					&& cphm1 != null && !"".equals(cphm1.trim())){
				//如果车牌类型null，则置为空字符串
				if(cplx1 == null){
					cplx1 = "";
				}
				
				//更新识别记录id
				UpdateRequestBuilder update = client.prepareUpdate("sb", "sb", tpid1);
				Map<String,Object> map = new HashMap<String, Object>();
				//指定修改字段值，其他字段不更新
				map.put("cphm1", cphm1);//修改车牌号码
				map.put("cplx1", cplx1);//修改车牌颜色
				update.setDoc(map);
				
				@SuppressWarnings("unused")
				UpdateResponse res = update.execute().actionGet();
				
			    return "1";
			}else{
				return "0";
			}
		}catch (ReceiveTimeoutTransportException e) {
			e.printStackTrace();
			System.out.println("数据库连接出现异常,超时"+e);
			if (client != null) {
				client.close();
			}
			throw new Exception("ES数据库更新异常");
		}catch (TransportSerializationException e) {
			e.printStackTrace();
			throw new Exception("没有找到要修改的数据记录");
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("ES数据库更新异常");
		}finally {
		   if (client != null) {
		    	ecclient.freeConnection("es", client);
		   }
		}
	}
	
	/**
	 * 更新ＥＳ记录，
	 */
	public Integer getES() throws Exception {
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		try {
			//更新识别记录id
			@SuppressWarnings("unused")
			GetResponse response = client.prepareGet("sb", "sb", "2013071723233301103A003001").execute().actionGet();
			return null;
		}catch (ReceiveTimeoutTransportException e) {
			e.printStackTrace();
			System.out.println("数据库连接出现异常，超时"+e);
			if (client != null) {
				client.close();
			}
			return 0;
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("ES数据库查询异常");
		} 
		finally {
			if (client != null) {
		    	ecclient.freeConnection("es", client);
		   }
		}
	}
	public static void main(String[] args) {
//		ESsearcherFilter es = new ESsearcherFilter();
		try {
//			es.updateES(null,null,null);//更新
//			es.getES();//查询单条记录
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
