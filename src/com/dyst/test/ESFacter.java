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
//	public Client client= esclient.getclient();//获取连接客户端
	TransportClient clientTrans = null;
	ESClientManager ecclient = ESClientManager.getInstance();
	Client client = ecclient.getConnection("es");// ES数据库连接池,获取数据连接
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
		// 监测点
		if (jcdid != null && !"".equals(jcdid)) {
			query.must(termQuery("jcdid", jcdid));
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
	 * 按照指定字段分组统计记录数
	 * @throws ParseException 
	 */
	public void facetByCon(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		
		//terms 单个字段统计
		FacetBuilder facet = FacetBuilders.termsFacet("cph")
//		Sets all possible terms to be loaded, even ones with 0 count. Note, this *should not* be used with a field that has many possible terms.
//		.allTerms(true)//全部显示可能的选项，就算没有值。当某个字段值选项很多时，谨慎使用
		//The fields the terms will be collected from.
		//多个字段分别统计分组，结果按照出现的频率大小输出
//		.fields("jcdid","cphm1")
//		.field("cphm1")
		.field("jcdid")
		.size(100);//最多返回记录数
		
		System.out.println("开始查询时间："+sdf.format(new Date()));
		SearchResponse response = client.prepareSearch("sb")
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		TermsFacet f = (TermsFacet)response.getFacets().facetsAsMap().get("cph");
		int i = 0;
		for(TermsFacet.Entry entry:f){
			System.out.print((i++)+"字段值:"+entry.getTerm()+"   ");
			System.out.println("出现频率:"+entry.getCount());
		}
		SearchHits hits = response.getHits();  
		System.out.println("记录总数:"+hits.getTotalHits());
	}
	/**
	 * 按照字段值的间隔进行统计，比如按照时间，每一个小时间隔统计记录数
	 * @throws ParseException 
	 */
	public void HistogfacetByCon(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		
		//terms 单个字段统计
		HistogramFacetBuilder facet = FacetBuilders.histogramFacet("p")
		/**
		 1.数字型统计字段。非日期类型统计时，可直接按照interval给定间隔值统计。
		 2.如统计字段boost，每10个值为一个间隔，统计值为大于等于前一个间隔的末尾值，
		小于后一个间隔初始值。如开始统计值为13900，则记录数13900至13910的记录数为 字段boost
		13900<=boost<13910.
		 3.如果统计字段为日期类型，时间起始值为每天8点，如统计2014-05-20的数据，字段值为今天
		 8点至21日8点的数据。按小时统计值正确，与预期结果一致.因此，如果要按照时间统计请使用
		 DateHistogramFacetBuilder 方法
		 */
		.field("tgsj")
		//给定数据间隔统计，天86400000，小时3600000
        .interval(3600000);
		
		SearchResponse response = client.prepareSearch("sb")
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
//				.setFrom(0).setSize(50).setExplain(true)
				.execute().actionGet();
		HistogramFacet f = (HistogramFacet)response.getFacets().facetsAsMap().get("p");
		int i = 0;
		for(HistogramFacet.Entry entry:f){
			System.out.print((i++)+"字段值:"+sdf.format(new Date(entry.getKey()))+"   "+entry.getKey());
			System.out.println("出现频率:"+entry.getCount());
		}
		SearchHits hits = response.getHits();  
		System.out.println("记录总数:"+hits.getTotalHits());
	}
	/**
	 * 按照字段值统计
	 *  指定时间段，统计结果从有结果的统计字段开始计算
	 * @throws ParseException 
	 */
	public void dateHistogramFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		
		DateHistogramFacetBuilder facet = FacetBuilders.dateHistogramFacet("p")
		.field("tgsj")//Long值的时间类型，Date 字段
		/*统计时间间隔，可以使星期、天、月、小时、分钟   、秒
		 year quarter month week day hour minute or 1.5h or 2w minute second
		  按天统计的时候，日期格式采用的时2014-01-01 08:00:00的格式，统计结果暂时不是需要的
		 如：2014-01-04日的数据统计的时间段时从2014-01-04 08:00:00到2014-01-05 08:00:00段的数据
		后台是以8点作为一天的开始
		
		解决方法，通过设置一下参数可以得到正确值，
		但是输出为2014-03-03 00:00:00值为2014-03-02日的值，即前一天的记录数值
		.preZone("-8:00")减去8小时
		//1970-01-01 00:00:00点的日期生成值，从什么时候开始 
		 时间段统计时，需要加上一天的毫秒值，如果不加的话，默认会把前一天的数据算作是今天的
		 -28800000为1970-1-1日期毫秒值，86400000为一天的毫秒值
		.preOffset(new TimeValue(-28800000+86400000))
		.postZone("-8:00")
		*/
		/*
		 * Sets the pre time zone to use when bucketing the values. This timezone will be applied before rounding off the result. 
		   Can either be in the form of "-10:00" or one of the values listed here: http://joda-time.sourceforge.net/timezones.html.
		 */
		.preZone("-8:00")
		/**
		 * 1970-01-01 00:00:00点的日期生成值，从什么时候开始 
		 * 时间段统计时，需要加上一天的毫秒值，如果不加的话，默认会把前一天的数据算作是今天的
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
			System.out.print((i++)+"字段值:"+sdf.format(new Date(entry.getTime()))+"   ");
			System.out.println("出现频率:"+entry.getCount());
		}
		SearchHits hits = response.getHits();  
		System.out.println("记录总数:"+hits.getTotalHits());
	}
	/**
	 * 按照字段统计每一个给定值段内的记录总数
	 * 如：按照通过时间统计小于2014-03-01之前、2014-03-01至2014-05-01、2014-05-01之后的个时间段的数据记录总数
	 * 分段可以多个分段
	 * @throws ParseException 
	 */
	public void rangFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		
		RangeFacetBuilder  facet = FacetBuilders.rangeFacet("p")//统计名字
		.field("tgsj")//统计字段
//		按照tgsj，分阶段统计个段的开始，最大最小值，和平均值（数字型字段）
		.addUnboundedFrom("1393603200000")//给定值到正无穷的数据，在query查询结果范围内
		.addRange("1393603200000", "1395603200000")///统计时间，字段范围,可添加多个值
		.addRange("1395603200000", "1401292800000")///统计时间，字段范围
		.addUnboundedTo("1401292800000");//从to到正无穷
		SearchResponse response = client.prepareSearch("sb")
//			query给定在符合query查询条件下统计，如果要统计所有记录，可以不指定，注释掉setQuery行
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		RangeFacet f = (RangeFacet)response.getFacets().facetsAsMap().get("p");
		int i=1;
		for(RangeFacet.Entry entry:f){
			System.out.println("第"+i+"个段值----------------begin---------");
			System.out.println("count:"+entry.getCount()+"   ");//符合统计段的记录总数
			System.out.println("from:"+entry.getFromAsString());
			System.out.println("to:"+entry.getToAsString());
			System.out.println("min:"+entry.getMin());
			System.out.println("max:"+entry.getMax());
			//tgsj字段的平均值
			System.out.println("mean:"+entry.getMean());
			//通过时间字段的所有记录总和
			System.out.println("Total:"+entry.getTotal());
			//在from到to统计字段之间的记录总数
			System.out.println("TotalCount:"+entry.getTotalCount());
			i++;
		}
		SearchHits hits = response.getHits();  
		//满足query查询条件的记录总数
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
	 */
	public void statisticalFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		StatisticalFacetBuilder facet = FacetBuilders.statisticalFacet("p")
		.field("boost");
 
		SearchResponse response = client.prepareSearch("sb")
				//可以指定查询query或者不指定都可以。。。。。。。。。
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		StatisticalFacet f = (StatisticalFacet)response.getFacets().
		facetsAsMap().get("p");//获取切片
		//如统计值为13956的记录有100条，getCount只取一条，去重
		System.out.println("符合条件记录数（取统计值唯一的记录）："+f.getCount());
		System.out.println("统计字段最大值："+f.getMax());//最大值 字段
		System.out.println("统计字段最小值："+f.getMin());//最小值
		System.out.println("统计字段平均值："+f.getMean());//平均值
		System.out.println("统计字段值总和数："+f.getTotal());//统计字段值的总和
		System.out.println(f.getStdDeviation());//背离值
		System.out.println(f.getSumOfSquares());
		System.out.println(f.getVariance());//变化值
		
		SearchHits hits = response.getHits();  
		System.out.println("符合查询条件记录总数:"+hits.getTotalHits());
	}
	/**
	 * //按照车牌号统计通过时间的统计值
	 * 通用：按照某一字段分组，统计该分组的最大最小平均值等信息
	 * @throws ParseException
	 */
	public void termsStatsFacet(String cphid,String jcdid,String cplx,String begintime,String endtime) throws ParseException{
		TermsStatsFacetBuilder facet = FacetBuilders.termsStatsFacet("p")
		//按照cphm1分组统计，记录tgsj字段值的最小、最大和平均值等
		.keyField("cphm1")//按照车牌号统计通过时间的统计值
		.valueField("tgsj")//只要是数字类型的都可以统计
		.size(3);
 
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
	 * 按照字段统计每一个给定值段内的记录总数
	 * 如：按照通过时间统计小于2014-03-01之前、2014-03-01至2014-05-01、2014-05-01之后的个时间段的数据记录总数
	 * 分段可以多个分段
	 * @throws ParseException 
	 */
	public void  rangFacet1111(String cphid,String jcdid,String cplx,String begintime,String endtime ,String jcdmc) throws ParseException{
		
		RangeFacetBuilder  facet = FacetBuilders.rangeFacet("p")//统计名字
		.field("tgsj");//统计字段
//		按照tgsj，分阶段统计个段的开始，最大最小值，和平均值（数字型字段）
//		.addUnboundedFrom("1393603200000")//给定值到正无穷的数据，在query查询结果范围内
//		.addRange("1393603200000", "1395603200000")///统计时间，字段范围,可添加多个值
//		.addRange("1395603200000", "1401292800000");///统计时间，字段范围
//		.addUnboundedTo("1401292800000");//从to到正无穷
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
//			query给定在符合query查询条件下统计，如果要统计所有记录，可以不指定，注释掉setQuery行
		        .setQuery(getQueryByCon(cphid, jcdid, cplx, begintime, endtime))
		        .addFacet(facet)
				.execute().actionGet();
		RangeFacet f = (RangeFacet)response.getFacets().facetsAsMap().get("p");
		int i=1;
		SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd");
		//写入标题
		ESFacter.writerTXT("D://tj//"+jcdmc+".txt", "统计时间("+jcdmc+"),7:30-9:30,17:30-19:30");
		//写入新的一行
		ESFacter.writerTXT("D://tj//"+jcdmc+".txt");
		for(RangeFacet.Entry entry:f){
//			System.out.println("第"+i+"个段值----------------begin---------");
			long dd = ((Double)entry.getFrom()).longValue();
			String formatString  = sdf1.format(new Date(dd))+","+entry.getCount();
			
			//第二行
			if(i%2==0){
				ESFacter.writerTXT("D://tj//"+jcdmc+".txt", ","+entry.getCount());
				ESFacter.writerTXT("D://tj//"+jcdmc+".txt");
			}else{
				ESFacter.writerTXT("D://tj//"+jcdmc+".txt", formatString);
			}
//			ESFacter.writerTXT("D://tj//"+jcdid+".txt", ""+entry.getCount());
//			System.out.println(sdf.format(new Date(dd+7200000)));
			System.out.println("count:"+entry.getCount()+"   ");//符合统计段的记录总数
//			System.out.println("Total:"+entry.getTotal());
			//在from到to统计字段之间的记录总数
//			System.out.println("TotalCount:"+entry.getTotalCount());
			i++;
		}
		SearchHits hits = response.getHits();  
		//满足query查询条件的记录总数
		System.out.println("记录总数:"+hits.getTotalHits());
	}
	/**
	 * @param filePath  日志文件存放文件路径
	 * @param conent    写入内容
	 */
	public static void writerTXT(String filePath, String conent) {
		try {
			File fileFolder = new File(filePath);
			if (!fileFolder.getParentFile().exists()) {
				fileFolder.getParentFile().mkdirs();
			}
			File file = new File(filePath);// 写入文件
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
	 * @param filePath  日志文件存放文件路径
	 * @param conent    写入内容
	 */
	public static void writerTXT(String filePath) {
		try {
			File fileFolder = new File(filePath);
			if (!fileFolder.getParentFile().exists()) {
				fileFolder.getParentFile().mkdirs();
			}
			File file = new File(filePath);// 写入文件
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
			10100203	北环大道皇岗路口西行
			10100204	北环大道彩田立交东行
			10100405	滨河大道福滨人行天桥西行
			10100406	滨河大道彩田立交东行
			10300503	滨海大道侨城东立交西行
			10300504	滨海大道侨城东立交东行
			10100609	深南大道新洲立交西行
			10100610	深南大道新洲立交东行
			20100301	梅观公路彩田路口北行
			20100306	梅观公路南坪立交南侧南行
			20104606	福龙路横龙山隧道北行南坪出口
			20104607	福龙路横龙山隧道出口南行
			 */
			Map<String, String> jcdMap = new HashMap<String, String>();
			jcdMap.put("10100203", "北环大道皇岗路口西行");
			jcdMap.put("10100204", "北环大道彩田立交东行");
			jcdMap.put("10100405", "滨河大道福滨人行天桥西行");
			jcdMap.put("10100406", "滨河大道彩田立交东行");
			jcdMap.put("10300503", "滨海大道侨城东立交西行");
			jcdMap.put("10300504", "滨海大道侨城东立交东行");
			jcdMap.put("10100609", "深南大道新洲立交西行");
			jcdMap.put("10100610", "深南大道新洲立交东行");
			jcdMap.put("20100301", "梅观公路彩田路口北行");
			jcdMap.put("20100306", "梅观公路南坪立交南侧南行");
			jcdMap.put("20104606", "福龙路横龙山隧道北行南坪出口");
			jcdMap.put("20104607", "福龙路横龙山隧道出口南行");
			
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
