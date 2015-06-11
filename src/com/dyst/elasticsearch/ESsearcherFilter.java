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
 * ͨ��Filter��ʽʵ��ES��ѯ
 * @author Administrator
 */
public class ESsearcherFilter {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	Client client = ESClient.getInstance().client;
//	Client client = es.client;
//	TransportClientPool pool = new TransportClientPool(5);
//	Client client = TransportClientPool.getTransportClient();
	/**
	 * ����Filter�ͷ�ҳ������ѯ�ţӿ�����
	 * @param filter  ������
	 * @param from    ��ʼ��¼
	 * @param to      ��ֹ��¼
	 * @param pagsize ÿҳ��С��
	 * @param sort  �����ֶ�
	 * @param sortType ��������
	 * @return ��ѯ���
	 */
	public SearchHits tdcpgjcx(FilterBuilder filter,int from ,int pagsize,String bussiness ,String sort,String sortType) {
		
//		Client client = ESClient.getInstance().client;
//		Date date1 = new Date();
		
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		
		String selectType = "sb";
//		if("03".equals(bussiness)){//δʶ���ѯ
//			selectType = "wsb";
//		}
		try {
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch("sb").setTypes(selectType)
					 .setFilter(filter)
//					 .setSearchType(SearchType.QUERY_AND_FETCH)
					 .setFrom(from).setSize(pagsize)
					 .addFields(new String[]{"cphm1","jcdid","cplx1","tgsj","cdid","tpid1",
   							 "tpid2","tpid3","tpid4","tpid5","sd","cdid","cb"})//ָ����ѯ�ֶ�
   					.setExplain(false);//���Բ�ѯ���ݽ��н���;
					if(!"".equals(sort.trim())&&!"".equals(sortType)){//��������sql
						searchRequestBuilder.addSort(sort, "DESC".equals(sortType.toUpperCase())?SortOrder.DESC:SortOrder.ASC );//����
					}
			SearchResponse response = searchRequestBuilder.execute().actionGet();
			
			SearchHits hits = response.getHits();
//			System.out.println(hits.getTotalHits() + "-�ܼ�¼��");
//			Date date2 = new Date();
//			double d = (date2.getTime()-date1.getTime());
//			System.out.println("ES���ѯ��¼����ʱ��"+d/1000+"��");
			return hits;
		}catch (ReceiveTimeoutTransportException e) {
			e.printStackTrace();
//			System.out.println("���ݿ����ӳ����쳣����ʱ"+e);
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
	 * ����Query�ͷ�ҳ������ѯ�ţӿ�����
	 * @param filter  ������
	 * @param from    ��ʼ��¼
	 * @param to      ��ֹ��¼
	 * @param pagsize ÿҳ��С��
	 * @return ��ѯ���
	 */
	public SearchHits tdcpgjcx(QueryBuilder query,int from ,int pagsize,String bussiness) {
		
//		Client client = ESClient.getInstance().client;
		Date date1 = new Date();
		
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		
		String selectType = "sb";
//		if("03".equals(bussiness)){//δʶ���ѯ
//			selectType = "wsb";
//		}
		try {
			//prepareSearch  ����                       setTypes  ��������
			SearchResponse response = client.prepareSearch("sb").setTypes(selectType)
					 .setQuery(query)//�������
//					 .setSearchType(SearchType.QUERY_AND_FETCH)
					 .setFrom(from).setSize(pagsize)//�ӿ�ʼȡ��ȡ��������
					 .setExplain(false)//���Բ�ѯ���ݽ��н���
   					 .addFields(new String[]{"cphm1","jcdid","cplx1","tgsj","cdid","tpid1",
   							 "tpid2","tpid3","tpid4","tpid5","sd","cdid","cb"})
//					 .addSort("tgsj", SortOrder.DESC)//����
					 .execute().actionGet();//ִ��
			
			SearchHits hits = response.getHits();//��ȡ���
			
//			System.out.println(hits.getHits().length + "-ES������ѯ��¼��");
//			System.out.println(hits.getTotalHits() + "-ES�ܼ�¼��");
			Date date2 = new Date();
			double d = (date2.getTime()-date1.getTime());
			System.out.println("ES���ѯ����ʱ��"+d/1000+"��");
			
			return hits;
		}catch (ReceiveTimeoutTransportException e) {
			e.printStackTrace();
//			System.out.println("���ݿ����ӳ����쳣����ʱ"+e);
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
	 * ͨ�����е�ֵ����ѯ ����,filter��ʽ
	 */
	public Integer getTdcpgjcxCount(FilterBuilder filter,String bussiness) throws Exception {
//		Date date1 = new Date();
		
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		String selectType = "sb";
//		if("03".equals(bussiness)){//δʶ���ѯ
//			selectType = "wsb";
//		}
		try {
			MatchAllQueryBuilder query = matchAllQuery();
			Date date1 = new Date();
			//prepareSearch  ����                       setTypes  ��������
//			SearchResponse response = client.prepareSearch("sb").setTypes(selectType)
//					 .setFilter(filter)
//					 .setSearchType(SearchType.COUNT)
//					 .setExplain(false)//���Բ�ѯ���ݽ��н���
//				     .execute().actionGet();//ִ��
			CountResponse response = client.prepareCount("sb").setTypes("sb")
            .setQuery(QueryBuilders.filteredQuery(query,filter))
            .execute().actionGet();
//			SearchHits hits = response.getHits();//��ȡ���
			
//			System.out.println(hits.getTotalHits() + "-ES�ܼ�¼��");
			Date date2 = new Date();
			double d = (date2.getTime()-date1.getTime());
			System.out.println("Filtered__ES���ѯ��¼��������ʱ��"+d/1000+"��");
			
			return ((Long)response.getCount()).intValue();
		}catch (ReceiveTimeoutTransportException e) {
			e.printStackTrace();
			System.out.println("���ݿ����ӳ����쳣����ʱ"+e);
			if (client != null) {
				client.close();
			}
			return 0;
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("ES���ݿ��ѯ�쳣");
		} 
		finally {
			if (client != null) {
		    	ecclient.freeConnection("es", client);
		   }
		}
	}
	/**
	 * ͨ�����е�ֵ����ѯ���� ,query��ʽ
	 */
	public Integer getTdcpgjcxCount(QueryBuilder query,String bussiness) throws Exception {
		Date date1 = new Date();
		
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		String selectType = "sb";
//		if("03".equals(bussiness)){//δʶ���ѯ
//			selectType = "wsb";
//		}
		try {
			//prepareSearch  ����                       setTypes  ��������
			CountResponse response = client.prepareCount("sb").setTypes(selectType)
					 .setQuery(query)//�������
//					 .setSearchType(SearchType.COUNT)//��������
//					 .setExplain(false)//�Ƿ�������ݣ����������ؼ���
				     .execute().actionGet();//ִ��
//			SearchHits hits = response.getHits();//��ȡ���
			
//			System.out.println(hits.getTotalHits() + "-ES�ܼ�¼��");
			Date date2 = new Date();
			double d = (date2.getTime()-date1.getTime());
			System.out.println("ES��Query��ѯ��ʽ����¼��������ʱ��"+d/1000+"��");
			
			return ((Long)response.getCount()).intValue();
		}catch (ReceiveTimeoutTransportException e) {
			e.printStackTrace();
			System.out.println("���ݿ����ӳ����쳣����ʱ"+e);
			if (client != null) {
				client.close();
			}
			return 0;
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("ES���ݿ��ѯ�쳣");
		} 
		finally {
			if (client != null) {
		    	ecclient.freeConnection("es", client);
		   }
		}
	}
	
	/**
	 * ���£ţ�ʶ���¼
	 * tpid1 ͼƬid
	 * cphm1 �޸ĺ�ĳ��ƺ���
	 * cplx1 �޸ĺ�ĳ�������
	 */
	public String updateEsSb(String tpid1,String cphm1,String cplx1) throws Exception {
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		try {
			//���ƺ��뼰ͼƬid����Ϊ��
			if(tpid1 != null && !"".equals(tpid1.trim()) 
					&& cphm1 != null && !"".equals(cphm1.trim())){
				//�����������null������Ϊ���ַ���
				if(cplx1 == null){
					cplx1 = "";
				}
				
				//����ʶ���¼id
				UpdateRequestBuilder update = client.prepareUpdate("sb", "sb", tpid1);
				Map<String,Object> map = new HashMap<String, Object>();
				//ָ���޸��ֶ�ֵ�������ֶβ�����
				map.put("cphm1", cphm1);//�޸ĳ��ƺ���
				map.put("cplx1", cplx1);//�޸ĳ�����ɫ
				update.setDoc(map);
				
				@SuppressWarnings("unused")
				UpdateResponse res = update.execute().actionGet();
				
			    return "1";
			}else{
				return "0";
			}
		}catch (ReceiveTimeoutTransportException e) {
			e.printStackTrace();
			System.out.println("���ݿ����ӳ����쳣,��ʱ"+e);
			if (client != null) {
				client.close();
			}
			throw new Exception("ES���ݿ�����쳣");
		}catch (TransportSerializationException e) {
			e.printStackTrace();
			throw new Exception("û���ҵ�Ҫ�޸ĵ����ݼ�¼");
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("ES���ݿ�����쳣");
		}finally {
		   if (client != null) {
		    	ecclient.freeConnection("es", client);
		   }
		}
	}
	
	/**
	 * ���£ţӼ�¼��
	 */
	public Integer getES() throws Exception {
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		try {
			//����ʶ���¼id
			@SuppressWarnings("unused")
			GetResponse response = client.prepareGet("sb", "sb", "2013071723233301103A003001").execute().actionGet();
			return null;
		}catch (ReceiveTimeoutTransportException e) {
			e.printStackTrace();
			System.out.println("���ݿ����ӳ����쳣����ʱ"+e);
			if (client != null) {
				client.close();
			}
			return 0;
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("ES���ݿ��ѯ�쳣");
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
//			es.updateES(null,null,null);//����
//			es.getES();//��ѯ������¼
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
