package com.dyst.test;

import static org.elasticsearch.index.query.QueryBuilders.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.EsAbortPolicy;
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

import com.dyst.elasticsearch.util.ESClientManager;


public class ESFacter {
//	ESClient esclient = new ESClient();
//	public Client client= esclient.getclient();//��ȡ���ӿͻ���
	TransportClient clientTrans = null;
	ESClientManager ecclient = ESClientManager.getInstance();
	Client client = ecclient.getConnection("es");// ES���ݿ����ӳ�,��ȡ��������
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
		// ����
		if (jcdid != null && !"".equals(jcdid)) {
			query.must(termQuery("jcdid", jcdid));
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
	 * ����ָ���ֶη���ͳ�Ƽ�¼��
	 * @throws ParseException 
	 */
	public void facetByCon(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		
		//terms �����ֶ�ͳ��
		FacetBuilder facet = FacetBuilders.termsFacet("cph")
//		Sets all possible terms to be loaded, even ones with 0 count. Note, this *should not* be used with a field that has many possible terms.
//		.allTerms(true)//ȫ����ʾ���ܵ�ѡ�����û��ֵ����ĳ���ֶ�ֵѡ��ܶ�ʱ������ʹ��
		//The fields the terms will be collected from.
		//����ֶηֱ�ͳ�Ʒ��飬������ճ��ֵ�Ƶ�ʴ�С���
//		.fields("jcdid","cphm1")
//		.field("cphm1")
		.field("jcdid")
		.size(100);//��෵�ؼ�¼��
		
		System.out.println("��ʼ��ѯʱ�䣺"+sdf.format(new Date()));
		SearchResponse response = client.prepareSearch("sb")
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		TermsFacet f = (TermsFacet)response.getFacets().facetsAsMap().get("cph");
		int i = 0;
		for(TermsFacet.Entry entry:f){
			System.out.print((i++)+"�ֶ�ֵ:"+entry.getTerm()+"   ");
			System.out.println("����Ƶ��:"+entry.getCount());
		}
		SearchHits hits = response.getHits();  
		System.out.println("��¼����:"+hits.getTotalHits());
	}
	/**
	 * �����ֶ�ֵ�ļ������ͳ�ƣ����簴��ʱ�䣬ÿһ��Сʱ���ͳ�Ƽ�¼��
	 * @throws ParseException 
	 */
	public void HistogfacetByCon(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		
		//terms �����ֶ�ͳ��
		HistogramFacetBuilder facet = FacetBuilders.histogramFacet("p")
		/**
		 1.������ͳ���ֶΡ�����������ͳ��ʱ����ֱ�Ӱ���interval�������ֵͳ�ơ�
		 2.��ͳ���ֶ�boost��ÿ10��ֵΪһ�������ͳ��ֵΪ���ڵ���ǰһ�������ĩβֵ��
		С�ں�һ�������ʼֵ���翪ʼͳ��ֵΪ13900�����¼��13900��13910�ļ�¼��Ϊ �ֶ�boost
		13900<=boost<13910.
		 3.���ͳ���ֶ�Ϊ�������ͣ�ʱ����ʼֵΪÿ��8�㣬��ͳ��2014-05-20�����ݣ��ֶ�ֵΪ����
		 8����21��8������ݡ���Сʱͳ��ֵ��ȷ����Ԥ�ڽ��һ��.��ˣ����Ҫ����ʱ��ͳ����ʹ��
		 DateHistogramFacetBuilder ����
		 */
		.field("tgsj")
		//�������ݼ��ͳ�ƣ���86400000��Сʱ3600000
        .interval(3600000);
		
		SearchResponse response = client.prepareSearch("sb")
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
//				.setFrom(0).setSize(50).setExplain(true)
				.execute().actionGet();
		HistogramFacet f = (HistogramFacet)response.getFacets().facetsAsMap().get("p");
		int i = 0;
		for(HistogramFacet.Entry entry:f){
			System.out.print((i++)+"�ֶ�ֵ:"+sdf.format(new Date(entry.getKey()))+"   "+entry.getKey());
			System.out.println("����Ƶ��:"+entry.getCount());
		}
		SearchHits hits = response.getHits();  
		System.out.println("��¼����:"+hits.getTotalHits());
	}
	/**
	 * �����ֶ�ֵͳ��
	 *  ָ��ʱ��Σ�ͳ�ƽ�����н����ͳ���ֶο�ʼ����
	 * @throws ParseException 
	 */
	public void dateHistogramFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		
		DateHistogramFacetBuilder facet = FacetBuilders.dateHistogramFacet("p")
		.field("tgsj")//Longֵ��ʱ�����ͣ�Date �ֶ�
		/*ͳ��ʱ����������ʹ���ڡ��졢�¡�Сʱ������   ����
		 year quarter month week day hour minute or 1.5h or 2w minute second
		  ����ͳ�Ƶ�ʱ�����ڸ�ʽ���õ�ʱ2014-01-01 08:00:00�ĸ�ʽ��ͳ�ƽ����ʱ������Ҫ��
		 �磺2014-01-04�յ�����ͳ�Ƶ�ʱ���ʱ��2014-01-04 08:00:00��2014-01-05 08:00:00�ε�����
		��̨����8����Ϊһ��Ŀ�ʼ
		
		���������ͨ������һ�²������Եõ���ȷֵ��
		�������Ϊ2014-03-03 00:00:00ֵΪ2014-03-02�յ�ֵ����ǰһ��ļ�¼��ֵ
		.preZone("-8:00")��ȥ8Сʱ
		//1970-01-01 00:00:00�����������ֵ����ʲôʱ��ʼ 
		 ʱ���ͳ��ʱ����Ҫ����һ��ĺ���ֵ��������ӵĻ���Ĭ�ϻ��ǰһ������������ǽ����
		 -28800000Ϊ1970-1-1���ں���ֵ��86400000Ϊһ��ĺ���ֵ
		.preOffset(new TimeValue(-28800000+86400000))
		.postZone("-8:00")
		*/
		/*
		 * Sets the pre time zone to use when bucketing the values. This timezone will be applied before rounding off the result. 
		   Can either be in the form of "-10:00" or one of the values listed here: http://joda-time.sourceforge.net/timezones.html.
		 */
		.preZone("-8:00")
		/**
		 * 1970-01-01 00:00:00�����������ֵ����ʲôʱ��ʼ 
		 * ʱ���ͳ��ʱ����Ҫ����һ��ĺ���ֵ��������ӵĻ���Ĭ�ϻ��ǰһ������������ǽ����
		 */
		.preOffset(new TimeValue(-28800000+86400000))
		.postZone("-8:00")
		.interval("day");
		
		
		SearchResponse response = client.prepareSearch("sb")
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		DateHistogramFacet f = (DateHistogramFacet)response.getFacets().facetsAsMap().get("p");
		int i = 0;
		for(DateHistogramFacet.Entry entry:f){
			System.out.print((i++)+"�ֶ�ֵ:"+sdf.format(new Date(entry.getTime()))+"   ");
			System.out.println("����Ƶ��:"+entry.getCount());
		}
		SearchHits hits = response.getHits();  
		System.out.println("��¼����:"+hits.getTotalHits());
	}
	/**
	 * �����ֶ�ͳ��ÿһ������ֵ���ڵļ�¼����
	 * �磺����ͨ��ʱ��ͳ��С��2014-03-01֮ǰ��2014-03-01��2014-05-01��2014-05-01֮��ĸ�ʱ��ε����ݼ�¼����
	 * �ֶο��Զ���ֶ�
	 * @throws ParseException 
	 */
	public void rangFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		
		RangeFacetBuilder  facet = FacetBuilders.rangeFacet("p")//ͳ������
		.field("tgsj")//ͳ���ֶ�
//		����tgsj���ֽ׶�ͳ�Ƹ��εĿ�ʼ�������Сֵ����ƽ��ֵ���������ֶΣ�
		.addUnboundedFrom("1393603200000")//����ֵ������������ݣ���query��ѯ�����Χ��
		.addRange("1393603200000", "1395603200000")///ͳ��ʱ�䣬�ֶη�Χ,����Ӷ��ֵ
		.addRange("1395603200000", "1401292800000")///ͳ��ʱ�䣬�ֶη�Χ
		.addUnboundedTo("1401292800000");//��to��������
		SearchResponse response = client.prepareSearch("sb")
//			query�����ڷ���query��ѯ������ͳ�ƣ����Ҫͳ�����м�¼�����Բ�ָ����ע�͵�setQuery��
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		RangeFacet f = (RangeFacet)response.getFacets().facetsAsMap().get("p");
		int i=1;
		for(RangeFacet.Entry entry:f){
			System.out.println("��"+i+"����ֵ----------------begin---------");
			System.out.println("count:"+entry.getCount()+"   ");//����ͳ�ƶεļ�¼����
			System.out.println("from:"+entry.getFromAsString());
			System.out.println("to:"+entry.getToAsString());
			System.out.println("min:"+entry.getMin());
			System.out.println("max:"+entry.getMax());
			//tgsj�ֶε�ƽ��ֵ
			System.out.println("mean:"+entry.getMean());
			//ͨ��ʱ���ֶε����м�¼�ܺ�
			System.out.println("Total:"+entry.getTotal());
			//��from��toͳ���ֶ�֮��ļ�¼����
			System.out.println("TotalCount:"+entry.getTotalCount());
			i++;
		}
		SearchHits hits = response.getHits();  
		//����query��ѯ�����ļ�¼����
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
	 */
	public void statisticalFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		StatisticalFacetBuilder facet = FacetBuilders.statisticalFacet("p")
		.field("boost");
 
		SearchResponse response = client.prepareSearch("sb")
				//����ָ����ѯquery���߲�ָ�������ԡ�����������������
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		StatisticalFacet f = (StatisticalFacet)response.getFacets().
		facetsAsMap().get("p");//��ȡ��Ƭ
		//��ͳ��ֵΪ13956�ļ�¼��100����getCountֻȡһ����ȥ��
		System.out.println("����������¼����ȡͳ��ֵΨһ�ļ�¼����"+f.getCount());
		System.out.println("ͳ���ֶ����ֵ��"+f.getMax());//���ֵ �ֶ�
		System.out.println("ͳ���ֶ���Сֵ��"+f.getMin());//��Сֵ
		System.out.println("ͳ���ֶ�ƽ��ֵ��"+f.getMean());//ƽ��ֵ
		System.out.println("ͳ���ֶ�ֵ�ܺ�����"+f.getTotal());//ͳ���ֶ�ֵ���ܺ�
		System.out.println(f.getStdDeviation());//����ֵ
		System.out.println(f.getSumOfSquares());
		System.out.println(f.getVariance());//�仯ֵ
		
		SearchHits hits = response.getHits();  
		System.out.println("���ϲ�ѯ������¼����:"+hits.getTotalHits());
	}
	/**
	 * //���ճ��ƺ�ͳ��ͨ��ʱ���ͳ��ֵ
	 * ͨ�ã�����ĳһ�ֶη��飬ͳ�Ƹ÷���������Сƽ��ֵ����Ϣ
	 * @throws ParseException
	 */
	public void termsStatsFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		TermsStatsFacetBuilder facet = FacetBuilders.termsStatsFacet("p")
		//����cphm1����ͳ�ƣ���¼tgsj�ֶ�ֵ����С������ƽ��ֵ��
		.keyField("cphm1")//���ճ��ƺ�ͳ��ͨ��ʱ���ͳ��ֵ
		.valueField("tgsj")//ֻҪ���������͵Ķ�����ͳ��
		.size(3);
 
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
	 * @throws ParseException
	 */
	public void distanceFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
	
		GeoDistanceFacetBuilder facet = FacetBuilders.geoDistanceFacet("p")
		.field("boost")
//		.point(121, 231)
		.addRange(10956, 13756)
		.addRange(13756,13956)
//		.unit(DistanceUnit.KILOMETERS)
		;
		
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
	 * �����ֶ�ͳ��ÿһ������ֵ���ڵļ�¼����
	 * �磺����ͨ��ʱ��ͳ��С��2014-03-01֮ǰ��2014-03-01��2014-05-01��2014-05-01֮��ĸ�ʱ��ε����ݼ�¼����
	 * �ֶο��Զ���ֶ�
	 * @throws ParseException 
	 */
	public void  rangFacet1111(String cphid,String jcdid,String cplx,String begintime,String endtime ,String jcdmc) throws ParseException{
		
		RangeFacetBuilder  facet = FacetBuilders.rangeFacet("p")//ͳ������
		.field("tgsj");//ͳ���ֶ�
//		����tgsj���ֽ׶�ͳ�Ƹ��εĿ�ʼ�������Сֵ����ƽ��ֵ���������ֶΣ�
//		.addUnboundedFrom("1393603200000")//����ֵ������������ݣ���query��ѯ�����Χ��
//		.addRange("1393603200000", "1395603200000")///ͳ��ʱ�䣬�ֶη�Χ,����Ӷ��ֵ
//		.addRange("1395603200000", "1401292800000");///ͳ��ʱ�䣬�ֶη�Χ
//		.addUnboundedTo("1401292800000");//��to��������
		long interval = 86400000l;
		long end = 1405094400000l;
		
		long begin = sdf.parse("2014-03-01 07:30:00").getTime();
		long begin1 = sdf.parse("2014-03-01 09:30:00").getTime();
		
		long begin2 = sdf.parse("2014-03-01 17:30:00").getTime();
		long begin3 = sdf.parse("2014-03-01 19:30:00").getTime();
		
		while(begin<end){
			facet.addRange(begin, begin1);
			facet.addRange(begin2, begin3);
			begin += interval;
			begin1 += interval;
			begin2 += interval;
			begin3 += interval;
		}
		SearchResponse response = client.prepareSearch("sb")
//			query�����ڷ���query��ѯ������ͳ�ƣ����Ҫͳ�����м�¼�����Բ�ָ����ע�͵�setQuery��
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		RangeFacet f = (RangeFacet)response.getFacets().facetsAsMap().get("p");
		int i=1;
		SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd");
		//д�����
		ESFacter.writerTXT("D://tj//"+jcdmc+".txt", "ͳ��ʱ��("+jcdmc+"),7:30-9:30,17:30-19:30");
		//д���µ�һ��
		ESFacter.writerTXT("D://tj//"+jcdmc+".txt");
		for(RangeFacet.Entry entry:f){
//			System.out.println("��"+i+"����ֵ----------------begin---------");
			long dd = ((Double)entry.getFrom()).longValue();
			String formatString  = sdf1.format(new Date(dd))+","+entry.getCount();
			
			//�ڶ���
			if(i%2==0){
				ESFacter.writerTXT("D://tj//"+jcdmc+".txt", ","+entry.getCount());
				ESFacter.writerTXT("D://tj//"+jcdmc+".txt");
			}else{
				ESFacter.writerTXT("D://tj//"+jcdmc+".txt", formatString);
			}
//			ESFacter.writerTXT("D://tj//"+jcdid+".txt", ""+entry.getCount());
//			System.out.println(sdf.format(new Date(dd+7200000)));
			System.out.println("count:"+entry.getCount()+"   ");//����ͳ�ƶεļ�¼����
//			System.out.println("Total:"+entry.getTotal());
			//��from��toͳ���ֶ�֮��ļ�¼����
//			System.out.println("TotalCount:"+entry.getTotalCount());
			i++;
		}
		SearchHits hits = response.getHits();  
		//����query��ѯ�����ļ�¼����
		System.out.println("��¼����:"+hits.getTotalHits());
	}
	/**
	 * @param filePath  ��־�ļ�����ļ�·��
	 * @param conent    д������
	 */
	public static void writerTXT(String filePath, String conent) {
		try {
			File fileFolder = new File(filePath);
			if (!fileFolder.getParentFile().exists()) {
				fileFolder.getParentFile().mkdirs();
			}
			File file = new File(filePath);// д���ļ�
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fileWriter);
//			bw.newLine();
			bw.write(conent);
			fileWriter.flush();
			bw.close();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * @param filePath  ��־�ļ�����ļ�·��
	 * @param conent    д������
	 */
	public static void writerTXT(String filePath) {
		try {
			File fileFolder = new File(filePath);
			if (!fileFolder.getParentFile().exists()) {
				fileFolder.getParentFile().mkdirs();
			}
			File file = new File(filePath);// д���ļ�
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fileWriter);
			bw.newLine();
			fileWriter.flush();
			bw.close();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@org.junit.Test
	public void Test(){
		try {
			/*
			10100203	��������ʸ�·������
			10100204	�������������������
			10100405	���Ӵ������������������
			10100406	���Ӵ��������������
			10300503	��������ȳǶ���������
			10300504	��������ȳǶ���������
			10100609	���ϴ��������������
			10100610	���ϴ��������������
			20100301	÷�۹�·����·�ڱ���
			20100306	÷�۹�·��ƺ�����ϲ�����
			20104606	����·����ɽ���������ƺ����
			20104607	����·����ɽ�����������
			 */
			Map<String, String> jcdMap = new HashMap<String, String>();
			jcdMap.put("10100203", "��������ʸ�·������");
			jcdMap.put("10100204", "�������������������");
			jcdMap.put("10100405", "���Ӵ������������������");
			jcdMap.put("10100406", "���Ӵ��������������");
			jcdMap.put("10300503", "��������ȳǶ���������");
			jcdMap.put("10300504", "��������ȳǶ���������");
			jcdMap.put("10100609", "���ϴ��������������");
			jcdMap.put("10100610", "���ϴ��������������");
			jcdMap.put("20100301", "÷�۹�·����·�ڱ���");
			jcdMap.put("20100306", "÷�۹�·��ƺ�����ϲ�����");
			jcdMap.put("20104606", "����·����ɽ���������ƺ����");
			jcdMap.put("20104607", "����·����ɽ�����������");
			
			ESFacter es = new ESFacter();
//			String jcdids[] = "10100203,10100204,10100405,10100406,10300503,10300504,10100609,10100610,20100301,20100306,20104606,20104607".split(",");
			String jcdids[] = "10100203".split(",");
			for(String jcdid:jcdids){
				es.rangFacet1111("", jcdid, "", "", "",jcdMap.get(jcdid));
			}
//			System.out.println(sdf.parse("2014-03-08 07:30:00").getTime());
//			System.out.println(sdf.parse("2014-03-08 09:30:00").getTime());
//			System.out.println(sdf.parse("2014-07- 00:00:00").getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
