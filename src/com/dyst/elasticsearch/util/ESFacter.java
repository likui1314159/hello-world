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
//	public Client client= esclient.getclient();//获取连接客户端
//	ESClientManager ecclient = ESClientManager.getInstance();
//	Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
	
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
public  BoolQueryBuilder getQueryByCon(String cphid,String jcdid,String cplx,String begintime,String endtime){
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		BoolQueryBuilder query = boolQuery();
		// 车牌号id
		if (cphid != null && !"".equals(cphid)) {
			query.must(termQuery("cphm1", cphid));
		}
		// 车牌类型
		if (cplx != null && !"".equals(cplx)) {
			query.must(termQuery("cplx1", cplx));
		}
//		// 监测点
//		if (jcdid != null && !"".equals(jcdid)) {
//			query.must(termQuery("jcdid", jcdid));
//		}
		// 监测点
		if (jcdid != null && !"".equals(jcdid)) {
			query.must(wildcardQuery("jcdid", jcdid));
		}
		// 识别时间,时间段查询
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
	 * 按照字段值统计
	 * @param filed  查询字段
	 * @param value  查询值
	 * @throws ParseException 
	 */
	public TermsFacet facetByCon(BoolQueryBuilder query, String groupName){
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		FacetBuilder facet = FacetBuilders.termsFacet("tj").field(groupName).size(10000);
		
		SearchResponse response = client.prepareSearch("sb")
				.setQuery(query)
		        .addFacet(facet)
				.execute().actionGet();
		TermsFacet f = (TermsFacet)response.getFacets().facetsAsMap().get("tj");
		if (client != null) {//关闭连接
	    	ecclient.freeConnection("es", client);
	   }
	   return f;
	}
	/**
	 * 按照字段值统计
	 * @param filed  查询字段
	 * @param value  查询值
	 * @throws ParseException 
	 */
	public void HistogfacetByCon(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		//terms 单个字段统计
		HistogramFacetBuilder facet = FacetBuilders.histogramFacet("p")
		.field("tgsj")//柱状图统计
        .interval(86400000);//给定毫秒级的数据间隔统计
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
			System.out.print((i++)+"字段值:"+sdf.format(new Date(entry.getKey()))+"   ");
			System.out.println("出现频率:"+entry.getCount());
		}
		SearchHits hits = response.getHits();  
		System.out.println("记录总数:"+hits.getTotalHits());
	}
	/**
	 * 按照字段值统计 指定时间段，统计结果从有记过的统计字段开始计算
	 * @param filed  查询字段
	 * @param value  查询值
	 * @throws ParseException 
	 */
	public void dateHistogramFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		
		DateHistogramFacetBuilder facet = FacetBuilders.dateHistogramFacet("p")
		.field("tgsj")
		.interval("hour");// week month week day hour minute or 1.5h or 2w
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
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
			System.out.print((i++)+"字段值:"+sdf.format(new Date(entry.getTime()))+"   ");
			System.out.println("出现频率:"+entry.getCount());
		}
		SearchHits hits = response.getHits();  
//		System.out.println(sdf.parse("2014-01-04 00:00:00").getTime());
//		System.out.println(sdf.parse("2014-01-05 00:00:00").getTime());
		System.out.println("记录总数:"+hits.getTotalHits());
	}
	/**
	 * 按照字段值统计 指定时间段，统计结果从有记过的统计字段开始计算
	 * @param filed  查询字段
	 * @param value  查询值
	 * @throws ParseException 
	 */
	public void rangFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		RangeFacetBuilder  facet = FacetBuilders.rangeFacet("p")//统计名字
		.field("tgsj")//统计字段
//		.addUnboundedFrom("1388764800000")
		.addRange("1388764800000", "1393948800000");///统计短时间，字段范围
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
		System.out.println("记录总数:"+hits.getTotalHits());
	}
	/**
	 * 查询符合条件的记录总数
	 * @param cphid
	 * @param jcdid
	 * @param cplx
	 * @param begintime
	 * @param endtime
	 * @throws ParseException
	 */
	public void filterFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		//在查询结果中统计粤B开始的车牌号总数
//		FilterFacetBuilder  facet = FacetBuilders.filterFacet("p", FilterBuilders.prefixFilter("cphm1", "粤B"));
		//统计车牌号为粤B77686  监测点为 20602602的记录数
		FilterFacetBuilder  facet = FacetBuilders.filterFacet("p", FilterBuilders.andFilter(
//				 FilterBuilders.termFilter("cphm1","粤B77686"),
				 FilterBuilders.termFilter("jcdid","20602602")));
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		//类似的符合条件的filter都可以统计
		SearchResponse response = client.prepareSearch("sb")
				//可以指定查询query或者不指定都可以。。。。。。。。。
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		FilterFacet f = (FilterFacet)response.getFacets().facetsAsMap().get("p");//获取切片
		System.out.println("符合条件记录数："+f.getCount());
		SearchHits hits = response.getHits();  
		System.out.println("记录总数:"+hits.getTotalHits());
	}
	
	/**
	 * 统计 数字字段相关信息，如最大值，最小值，平均值，
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
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		SearchResponse response = client.prepareSearch("sb")
				//可以指定查询query或者不指定都可以。。。。。。。。。
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
//				.setPostFilter(FilterBuilder)
		        .addFacet(facet)
				.execute().actionGet();
		StatisticalFacet f = (StatisticalFacet)response.getFacets().
		facetsAsMap().get("p");//获取切片
		System.out.println("符合条件记录数："+f.getCount());
		System.out.println(f.getMax());//最大值 字段
		System.out.println(f.getMin());//最小值
		System.out.println(f.getMean());//平均值
		System.out.println(f.getTotal());//总数
		System.out.println(f.getStdDeviation());//背离值
		System.out.println(f.getSumOfSquares());
		System.out.println(f.getVariance());//变化值
		
		SearchHits hits = response.getHits();  
		System.out.println("记录总数:"+hits.getTotalHits());
	}
	/**
	 * //按照车牌号统计通过时间的统计值
	 * @param cphid
	 * @param jcdid
	 * @param cplx
	 * @param begintime
	 * @param endtime
	 * @throws ParseException
	 */
	public void termsStatsFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		TermsStatsFacetBuilder facet = FacetBuilders.termsStatsFacet("p")
		.keyField("cphm1")//按照车牌号统计通过时间的统计值
		.valueField("tgsj")//只要是数字类型的都可以统计
		.size(3);
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		SearchResponse response = client.prepareSearch("sb")
				//可以指定查询query或者不指定都可以。。。。。。。。。
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		TermsStatsFacet f = (TermsStatsFacet)response.getFacets().
		facetsAsMap().get("p");//获取切片
		for(TermsStatsFacet.Entry e :f){
			System.out.println("Term: "+e.getTerm());
			System.out.println("count: "+e.getCount());
			System.out.println("min: "+e.getMin());
			System.out.println("max: "+e.getMax());
			System.out.println("mean: "+e.getMean());//平均值
			System.out.println("Totle: "+e.getTotal());//统计值加起来的总数
			System.out.println("TotleCount: "+e.getTotalCount());//记录总数
		}
		
		SearchHits hits = response.getHits();  
		System.out.println("记录总数:"+hits.getTotalHits());
	}
	/**
	 * 距离统计，暂时测试不通过
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
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		SearchResponse response = client.prepareSearch("sb")
				//可以指定查询query或者不指定都可以。。。。。。。。。
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		GeoDistanceFacet f = (GeoDistanceFacet)response.getFacets().
		facetsAsMap().get("p");//获取切片
		for(GeoDistanceFacet.Entry e :f){
			System.out.println("from: "+e.getFrom());
			System.out.println("to: "+e.getTo());
			System.out.println("count: "+e.getCount());
			System.out.println("min: "+e.getMin());
			System.out.println("max: "+e.getMax());
			System.out.println("mean: "+e.getMean());//平均值
			System.out.println("Totle: "+e.getTotal());//统计值加起来的总数
			System.out.println("TotleCount: "+e.getTotalCount());//记录总数
		}
		SearchHits hits = response.getHits();  
		System.out.println("记录总数:"+hits.getTotalHits());
	}
	/**
	 * Facets测试
	 * @throws ParseException 
	 */
	@org.junit.Test
	public void facetTest() throws Exception {
		ESFacter esf = new ESFacter();
		Date date1 = new Date();
    	System.out.println("初始化时间："+sdf.format(date1));
		try {
//			esf.facetByCon(//字段，单个、多个统计
//					"", "", "", "2014-03-11 00:00:00","2014-04-11 00:25:33"
//			);
//			esf.HistogfacetByCon (//柱状图统计
//					"", "", "", "2014-01-01 00:00:00","2014-03-25 00:25:33"
//			);
//			esf.dateHistogramFacet (//按照日期统计
//					"", "", "", "2014-01-01 00:00:00","2014-03-25 00:00:00"
//			);
//			esf.rangFacet (//范围统计
//					"", "", "", "2014-01-01 00:00:00","2014-03-29 00:00:00"
//			);
//			esf.filterFacet (//filter统计
//					"", "", "", "2014-01-01 00:00:00","2014-03-29 00:00:00"
//			);
//			esf.statisticalFacet(//statistical 统计
//					"", "", "", "2014-01-01 00:00:00","2014-03-29 00:00:00"
//			);
//			esf.termsStatsFacet(//statistical 统计
//					"", "", "", "2014-01-01 00:00:00","2014-03-29 00:00:00"
//			);
//			esf.distanceFacet(//statistical 统计
//					"", "", "", "2014-01-01 00:00:00","2014-03-29 00:00:00"
//			);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println(sdf.parse("2014-01-04 00:00:00").getTime());
//		System.out.println(sdf.parse("2014-03-05 00:00:00").getTime());
		Date date2 = new Date();
		System.out.println("查询结束时间："+sdf.format(date2));
		double d = (date2.getTime()-date1.getTime());
		System.out.println(d/1000+"秒");
		
	}
	
	/**
	 * 查询停车场历史上传唯一车牌数据
	 * @param filed  查询字段
	 * @param value  查询值
	 * @throws ParseException 
	 */
	public void distinctCphm(BoolQueryBuilder q, String groupName){
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		FacetBuilder facet = FacetBuilders.termsFacet("tj")
		.field(groupName).size(3000000);
		BoolQueryBuilder query = getQueryByCon("", "*A*", "", "", "");
		SearchResponse response = client.prepareSearch("sb")
				.setQuery(query)
		        .addFacet(facet)
				.execute().actionGet();
		TermsFacet f = (TermsFacet)response.getFacets().facetsAsMap().get("tj");
		int i = 0;
		System.out.println("唯一车牌数：：："+f.getEntries().size());
		
//		for(TermsFacet.Entry entry:f){
//			System.out.print((i++)+"字段值:"+entry.getTerm()+"   ");
//			System.out.println("出现频率:"+entry.getCount());
//		}
//		SearchHits hits = response.getHits();  
//		System.out.println("记录总数:"+ f.getTotalCount());
		if (client != null) {//关闭连接
	    	ecclient.freeConnection("es", client);
	   }
	}
	public static void main(String[] args) {
		ESFacter es = new ESFacter();
		es.distinctCphm(null, "cphm1");
	}
}
