package com.dyst.elasticsearch.util;

import static org.elasticsearch.index.query.QueryBuilders.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.datehistogram.DateHistogramFacet;
import org.elasticsearch.search.facet.datehistogram.DateHistogramFacetBuilder;
import org.elasticsearch.search.facet.filter.FilterFacet;
import org.elasticsearch.search.facet.filter.FilterFacetBuilder;
import org.elasticsearch.search.facet.geodistance.GeoDistanceFacet;
import org.elasticsearch.search.facet.geodistance.GeoDistanceFacetBuilder;
import org.elasticsearch.search.facet.histogram.HistogramFacet;
import org.elasticsearch.search.facet.histogram.HistogramFacetBuilder;
import org.elasticsearch.search.facet.range.RangeFacet;
import org.elasticsearch.search.facet.range.RangeFacetBuilder;
import org.elasticsearch.search.facet.statistical.StatisticalFacet;
import org.elasticsearch.search.facet.statistical.StatisticalFacetBuilder;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.termsstats.TermsStatsFacet;
import org.elasticsearch.search.facet.termsstats.TermsStatsFacetBuilder;


public class ESFacter {
//	ESClient esclient = new ESClient();
//	public Client client= esclient.getclient();//��ȡ���ӿͻ���
//	ESClientManager ecclient = ESClientManager.getInstance();
//	Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
	
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
public  BoolQueryBuilder getQueryByCon(String cphid,String jcdid,String cplx,String begintime,String endtime){
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		BoolQueryBuilder query = boolQuery();
		// ���ƺ�id
		if (cphid != null && !"".equals(cphid)) {
			query.must(termQuery("cphm1", cphid));
		}
		// ��������
		if (cplx != null && !"".equals(cplx)) {
			query.must(termQuery("cplx1", cplx));
		}
//		// ����
//		if (jcdid != null && !"".equals(jcdid)) {
//			query.must(termQuery("jcdid", jcdid));
//		}
		// ����
		if (jcdid != null && !"".equals(jcdid)) {
			query.must(wildcardQuery("jcdid", jcdid));
		}
		// ʶ��ʱ��,ʱ��β�ѯ
		if (begintime != null && !"".equals(begintime) && endtime != null
				&& !"".equals(endtime)) {
			    try {
					query.must(rangeQuery("tgsj")
						.from(sdf.parse(begintime).getTime())
						.to(sdf.parse(endtime).getTime()).includeLower(true)
						.includeUpper(false))
						;
				} catch (ParseException e) {
					e.printStackTrace();
				}
		}
		return query;
	}
	/**
	 * �����ֶ�ֵͳ��
	 * @param filed  ��ѯ�ֶ�
	 * @param value  ��ѯֵ
	 * @throws ParseException 
	 */
	public TermsFacet facetByCon(BoolQueryBuilder query, String groupName){
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		FacetBuilder facet = FacetBuilders.termsFacet("tj").field(groupName).size(10000);
		
		SearchResponse response = client.prepareSearch("sb")
				.setQuery(query)
		        .addFacet(facet)
				.execute().actionGet();
		TermsFacet f = (TermsFacet)response.getFacets().facetsAsMap().get("tj");
		if (client != null) {//�ر�����
	    	ecclient.freeConnection("es", client);
	   }
	   return f;
	}
	/**
	 * �����ֶ�ֵͳ��
	 * @param filed  ��ѯ�ֶ�
	 * @param value  ��ѯֵ
	 * @throws ParseException 
	 */
	public void HistogfacetByCon(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		//terms �����ֶ�ͳ��
		HistogramFacetBuilder facet = FacetBuilders.histogramFacet("p")
		.field("tgsj")//��״ͼͳ��
        .interval(86400000);//�������뼶�����ݼ��ͳ��
		SearchResponse response = client.prepareSearch("sb")
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
//				.setFrom(0).setSize(50).setExplain(true)
				.execute().actionGet();
		HistogramFacet f = (HistogramFacet)response.getFacets().facetsAsMap().get("p");
//		System.out.println(f.getTotalCount());
//		System.out.println(f.getOtherCount());
//		System.out.println(f.getMissingCount());
		int i = 0;
		for(HistogramFacet.Entry entry:f){
			System.out.print((i++)+"�ֶ�ֵ:"+sdf.format(new Date(entry.getKey()))+"   ");
			System.out.println("����Ƶ��:"+entry.getCount());
		}
		SearchHits hits = response.getHits();  
		System.out.println("��¼����:"+hits.getTotalHits());
	}
	/**
	 * �����ֶ�ֵͳ�� ָ��ʱ��Σ�ͳ�ƽ�����мǹ���ͳ���ֶο�ʼ����
	 * @param filed  ��ѯ�ֶ�
	 * @param value  ��ѯֵ
	 * @throws ParseException 
	 */
	public void dateHistogramFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		
		DateHistogramFacetBuilder facet = FacetBuilders.dateHistogramFacet("p")
		.field("tgsj")
		.interval("hour");// week month week day hour minute or 1.5h or 2w
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		SearchResponse response = client.prepareSearch("sb")
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
//				.setFrom(0).setSize(50).setExplain(true)
				.execute().actionGet();
		DateHistogramFacet f = (DateHistogramFacet)response.getFacets().facetsAsMap().get("p");
//		System.out.println(f.getTotalCount());
//		System.out.println(f.getOtherCount());
//		System.out.println(f.getMissingCount());
		int i = 0;
		for(DateHistogramFacet.Entry entry:f){
			System.out.print((i++)+"�ֶ�ֵ:"+sdf.format(new Date(entry.getTime()))+"   ");
			System.out.println("����Ƶ��:"+entry.getCount());
		}
		SearchHits hits = response.getHits();  
//		System.out.println(sdf.parse("2014-01-04 00:00:00").getTime());
//		System.out.println(sdf.parse("2014-01-05 00:00:00").getTime());
		System.out.println("��¼����:"+hits.getTotalHits());
	}
	/**
	 * �����ֶ�ֵͳ�� ָ��ʱ��Σ�ͳ�ƽ�����мǹ���ͳ���ֶο�ʼ����
	 * @param filed  ��ѯ�ֶ�
	 * @param value  ��ѯֵ
	 * @throws ParseException 
	 */
	public void rangFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		RangeFacetBuilder  facet = FacetBuilders.rangeFacet("p")//ͳ������
		.field("tgsj")//ͳ���ֶ�
//		.addUnboundedFrom("1388764800000")
		.addRange("1388764800000", "1393948800000");///ͳ�ƶ�ʱ�䣬�ֶη�Χ
//		.addUnboundedTo("1388851200000");
		SearchResponse response = client.prepareSearch("sb")
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
//				.setFrom(0).setSize(50).setExplain(true)
				.execute().actionGet();
		RangeFacet f = (RangeFacet)response.getFacets().facetsAsMap().get("p");
//		System.out.println(f.getTotalCount());
//		System.out.println(f.getOtherCount());
//		System.out.println(f.getMissingCount());
		int i = 0;
		for(RangeFacet.Entry entry:f){
			System.out.print("count:"+entry.getCount()+"   ");
			System.out.println("from:"+entry.getFrom());
			System.out.println("to:"+entry.getTo());
			System.out.println("min:"+entry.getMin());
			System.out.println("max:"+entry.getMax());
		}
		SearchHits hits = response.getHits();  
//		System.out.println(sdf.parse("2014-01-04 00:00:00").getTime());
//		System.out.println(sdf.parse("2014-01-05 00:00:00").getTime());
		System.out.println("��¼����:"+hits.getTotalHits());
	}
	/**
	 * ��ѯ���������ļ�¼����
	 * @param cphid
	 * @param jcdid
	 * @param cplx
	 * @param begintime
	 * @param endtime
	 * @throws ParseException
	 */
	public void filterFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		//�ڲ�ѯ�����ͳ����B��ʼ�ĳ��ƺ�����
//		FilterFacetBuilder  facet = FacetBuilders.filterFacet("p", FilterBuilders.prefixFilter("cphm1", "��B"));
		//ͳ�Ƴ��ƺ�Ϊ��B77686  ����Ϊ 20602602�ļ�¼��
		FilterFacetBuilder  facet = FacetBuilders.filterFacet("p", FilterBuilders.andFilter(
//				 FilterBuilders.termFilter("cphm1","��B77686"),
				 FilterBuilders.termFilter("jcdid","20602602")));
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		//���Ƶķ���������filter������ͳ��
		SearchResponse response = client.prepareSearch("sb")
				//����ָ����ѯquery���߲�ָ�������ԡ�����������������
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		FilterFacet f = (FilterFacet)response.getFacets().facetsAsMap().get("p");//��ȡ��Ƭ
		System.out.println("����������¼����"+f.getCount());
		SearchHits hits = response.getHits();  
		System.out.println("��¼����:"+hits.getTotalHits());
	}
	
	/**
	 * ͳ�� �����ֶ������Ϣ�������ֵ����Сֵ��ƽ��ֵ��
	 * @param cphid
	 * @param jcdid
	 * @param cplx
	 * @param begintime
	 * @param endtime
	 * @throws ParseException
	 */
	public void statisticalFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		StatisticalFacetBuilder facet = FacetBuilders.statisticalFacet("p")
		.field("sd");
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		SearchResponse response = client.prepareSearch("sb")
				//����ָ����ѯquery���߲�ָ�������ԡ�����������������
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
//				.setPostFilter(FilterBuilder)
		        .addFacet(facet)
				.execute().actionGet();
		StatisticalFacet f = (StatisticalFacet)response.getFacets().
		facetsAsMap().get("p");//��ȡ��Ƭ
		System.out.println("����������¼����"+f.getCount());
		System.out.println(f.getMax());//���ֵ �ֶ�
		System.out.println(f.getMin());//��Сֵ
		System.out.println(f.getMean());//ƽ��ֵ
		System.out.println(f.getTotal());//����
		System.out.println(f.getStdDeviation());//����ֵ
		System.out.println(f.getSumOfSquares());
		System.out.println(f.getVariance());//�仯ֵ
		
		SearchHits hits = response.getHits();  
		System.out.println("��¼����:"+hits.getTotalHits());
	}
	/**
	 * //���ճ��ƺ�ͳ��ͨ��ʱ���ͳ��ֵ
	 * @param cphid
	 * @param jcdid
	 * @param cplx
	 * @param begintime
	 * @param endtime
	 * @throws ParseException
	 */
	public void termsStatsFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		TermsStatsFacetBuilder facet = FacetBuilders.termsStatsFacet("p")
		.keyField("cphm1")//���ճ��ƺ�ͳ��ͨ��ʱ���ͳ��ֵ
		.valueField("tgsj")//ֻҪ���������͵Ķ�����ͳ��
		.size(3);
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		SearchResponse response = client.prepareSearch("sb")
				//����ָ����ѯquery���߲�ָ�������ԡ�����������������
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		TermsStatsFacet f = (TermsStatsFacet)response.getFacets().
		facetsAsMap().get("p");//��ȡ��Ƭ
		for(TermsStatsFacet.Entry e :f){
			System.out.println("Term: "+e.getTerm());
			System.out.println("count: "+e.getCount());
			System.out.println("min: "+e.getMin());
			System.out.println("max: "+e.getMax());
			System.out.println("mean: "+e.getMean());//ƽ��ֵ
			System.out.println("Totle: "+e.getTotal());//ͳ��ֵ������������
			System.out.println("TotleCount: "+e.getTotalCount());//��¼����
		}
		
		SearchHits hits = response.getHits();  
		System.out.println("��¼����:"+hits.getTotalHits());
	}
	/**
	 * ����ͳ�ƣ���ʱ���Բ�ͨ��
	 * @param cphid
	 * @param jcdid
	 * @param cplx
	 * @param begintime
	 * @param endtime
	 * @throws ParseException
	 */
	public void distanceFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
	
		GeoDistanceFacetBuilder facet = FacetBuilders.geoDistanceFacet("p")
		.field("scsj")
		.point(121, 231)
		.addRange(20, 80)
		.addRange(100,180)
		.unit(DistanceUnit.KILOMETERS)
		;
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		SearchResponse response = client.prepareSearch("sb")
				//����ָ����ѯquery���߲�ָ�������ԡ�����������������
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		GeoDistanceFacet f = (GeoDistanceFacet)response.getFacets().
		facetsAsMap().get("p");//��ȡ��Ƭ
		for(GeoDistanceFacet.Entry e :f){
			System.out.println("from: "+e.getFrom());
			System.out.println("to: "+e.getTo());
			System.out.println("count: "+e.getCount());
			System.out.println("min: "+e.getMin());
			System.out.println("max: "+e.getMax());
			System.out.println("mean: "+e.getMean());//ƽ��ֵ
			System.out.println("Totle: "+e.getTotal());//ͳ��ֵ������������
			System.out.println("TotleCount: "+e.getTotalCount());//��¼����
		}
		SearchHits hits = response.getHits();  
		System.out.println("��¼����:"+hits.getTotalHits());
	}
	/**
	 * Facets����
	 * @throws ParseException 
	 */
	@org.junit.Test
	public void facetTest() throws Exception {
		ESFacter esf = new ESFacter();
		Date date1 = new Date();
    	System.out.println("��ʼ��ʱ�䣺"+sdf.format(date1));
		try {
//			esf.facetByCon(//�ֶΣ����������ͳ��
//					"", "", "", "2014-03-11 00:00:00","2014-04-11 00:25:33"
//			);
//			esf.HistogfacetByCon (//��״ͼͳ��
//					"", "", "", "2014-01-01 00:00:00","2014-03-25 00:25:33"
//			);
//			esf.dateHistogramFacet (//��������ͳ��
//					"", "", "", "2014-01-01 00:00:00","2014-03-25 00:00:00"
//			);
//			esf.rangFacet (//��Χͳ��
//					"", "", "", "2014-01-01 00:00:00","2014-03-29 00:00:00"
//			);
//			esf.filterFacet (//filterͳ��
//					"", "", "", "2014-01-01 00:00:00","2014-03-29 00:00:00"
//			);
//			esf.statisticalFacet(//statistical ͳ��
//					"", "", "", "2014-01-01 00:00:00","2014-03-29 00:00:00"
//			);
//			esf.termsStatsFacet(//statistical ͳ��
//					"", "", "", "2014-01-01 00:00:00","2014-03-29 00:00:00"
//			);
//			esf.distanceFacet(//statistical ͳ��
//					"", "", "", "2014-01-01 00:00:00","2014-03-29 00:00:00"
//			);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println(sdf.parse("2014-01-04 00:00:00").getTime());
//		System.out.println(sdf.parse("2014-03-05 00:00:00").getTime());
		Date date2 = new Date();
		System.out.println("��ѯ����ʱ�䣺"+sdf.format(date2));
		double d = (date2.getTime()-date1.getTime());
		System.out.println(d/1000+"��");
		
	}
	
	/**
	 * ��ѯͣ������ʷ�ϴ�Ψһ��������
	 * @param filed  ��ѯ�ֶ�
	 * @param value  ��ѯֵ
	 * @throws ParseException 
	 */
	public void distinctCphm(BoolQueryBuilder q, String groupName){
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		FacetBuilder facet = FacetBuilders.termsFacet("tj")
		.field(groupName).size(3000000);
		BoolQueryBuilder query = getQueryByCon("", "*A*", "", "", "");
		SearchResponse response = client.prepareSearch("sb")
				.setQuery(query)
		        .addFacet(facet)
				.execute().actionGet();
		TermsFacet f = (TermsFacet)response.getFacets().facetsAsMap().get("tj");
		int i = 0;
		System.out.println("Ψһ������������"+f.getEntries().size());
		
//		for(TermsFacet.Entry entry:f){
//			System.out.print((i++)+"�ֶ�ֵ:"+entry.getTerm()+"   ");
//			System.out.println("����Ƶ��:"+entry.getCount());
//		}
//		SearchHits hits = response.getHits();  
//		System.out.println("��¼����:"+ f.getTotalCount());
		if (client != null) {//�ر�����
	    	ecclient.freeConnection("es", client);
	   }
	}
	public static void main(String[] args) {
		ESFacter es = new ESFacter();
		es.distinctCphm(null, "cphm1");
	}
}
