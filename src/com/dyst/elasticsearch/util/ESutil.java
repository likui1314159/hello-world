package com.dyst.elasticsearch.util;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;

import com.dyst.util.Config;

public class ESutil {
	/**
	 * 根据给定条件生成Filter给ＥＳ查询使用
	 * @param kssj  开始时间
	 *            2013-01-02 10:10:10
	 * @param jssj    结束时间
	 * @param hpzl    号牌种类
	 * @param hphm    号牌号码
	 * @param cplx    车牌类型
	 * @param gcxh    过车序号
	 * @param jcdid   监测点id
	 * @param hmdCphm  红名单车牌号码<br>
	 * @return filter 过滤器
	 */
	public static FilterBuilder getFilterByCon(String kssj, String jssj,
			String hpzl, String hphm, String cplx, String gcxh, String jcdid, 
			String cd, String cb, String sd, String hmdCphm, String business) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		FilterBuilder filtersbsj = null;//识别时间
		FilterBuilder filtercd = null;//车道
		FilterBuilder filterhphm = null;//号牌
		FilterBuilder filtercplx = null;//车牌类型
		FilterBuilder filtergcxh = null;//过车序号
		FilterBuilder filterjcdid = null;//监测点id
		FilterBuilder filtersd = null;//速度
		FilterBuilder qpsfwc = null;//前拍是否完成
		FilterBuilder filtercb = null;//车标
//		FilterBuilder filterhmd = null;//红名单过滤
		Config config = Config.getInstance();
		String qz1 = config.getQz1();//图片调用前缀
		String qz2 = config.getQz2();//图片调用前缀
		List<FilterBuilder> listFilters = new ArrayList<FilterBuilder>();// filter集合
		
		// 时间,,条件苛刻的放在前面
		if (kssj != null && !"".equals(kssj.trim()) && jssj != null
				&& !"".equals(jssj.trim())) {
			// 开始和截止时间必填
			try {
				filtersbsj = FilterBuilders.boolFilter().must(
						FilterBuilders.rangeFilter("tgsj").from(sdf.parse(kssj).getTime())
						.to(sdf.parse(jssj).getTime()).includeLower(true).includeUpper(true));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			listFilters.add(filtersbsj);
		}
		// 监测点ID判断
		if (jcdid != null && !"".equals(jcdid.trim())) {
			jcdid = jcdid.replaceAll("'", "");
			String strjcdid[] = jcdid.split(",");//
			if (strjcdid.length == 1) {// 
				filterjcdid = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("jcdid", jcdid));
			} else {// 多个车牌号
				filterjcdid = FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("jcdid",strjcdid));
			}
			listFilters.add(filterjcdid);
		}
		if (hphm != null && !"".equals(hphm.trim())) {// 号牌号码判断
			if("04".equals(business)){//模糊查询
				filterhphm = FilterBuilders.boolFilter().must(FilterBuilders.queryFilter(QueryBuilders.wildcardQuery("cphm1", hphm)));//wildcardQuery 模糊查询
			}else if("05".equals(business)){//前缀查询
				if(hphm.startsWith("!")){//非前缀查询
					filterhphm =  FilterBuilders.boolFilter().mustNot(FilterBuilders.prefixFilter("cphm1", hphm.length() > 1? hphm.substring(1):""));
				}else {
					if(hphm.substring(0, 1).equals(qz1) ){
						//查询本省非本地车，如蒙开头，但非蒙C的车牌号
						filterhphm = FilterBuilders.boolFilter().must(FilterBuilders.prefixFilter("cphm1", hphm))
						.mustNot(FilterBuilders.prefixFilter("cphm1", qz2));
//						queryBuilder.mustNot(QueryBuilders.prefixQuery("cphm1", qz2));
					}else{
						filterhphm =  FilterBuilders.boolFilter().must(FilterBuilders.prefixFilter("cphm1", hphm));//prefixQuery 前缀
					}
				}
			}else if(!"03".equals(business)){//非未识别查询
				String strhphm[] = hphm.split(",");// 多个车牌号
				if (strhphm.length == 1) {// 只有一个车牌号
					filterhphm = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("cphm1", hphm));//termQuery 一项匹配
				} else {// 多个车牌号
					filterhphm = FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("cphm1", strhphm));//termsQuery 多项匹配
				}
			}
			listFilters.add(filterhphm);
		}
		// 车牌类型
		if (cplx != null && !"".equals(cplx.trim())) {
			String strcplx[] = cplx.split(",");//
			if (strcplx.length == 1) {// 只有一个车牌号
				filtercplx = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("cplx1", cplx));
			} else {// 多个车牌号
				filtercplx = FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("cplx1", strcplx));
			}
			listFilters.add(filtercplx);
		}
		// 图片
		if (gcxh != null && !"".equals(gcxh.trim())) {
			String strgcxh[] = gcxh.split(",");//
			if (strgcxh.length == 1) {// 只有一个图片
				filtergcxh = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("tpid1", gcxh));
			} else {// 多个图片
				filtergcxh = FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("tpid1", strgcxh));
			}
			listFilters.add(filtergcxh);
		}
		
		//车道
		if (cd != null && !"".equals(cd.trim())) {
			String strcd[] = cd.split(",");
			if (strcd.length == 1) {
				filtercd = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("cdid", cd));
			} else {// 多个车道
				filtercd = FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("cdid", strcd));
			}
			listFilters.add(filtercd);
		}
		
		//车标
		if (cb != null && !"".equals(cb.trim())) {
			String strcb[] = cb.split(",");
			if (strcb.length == 1) {
				filtercb = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("cb", cb));
			} else {// 多个车道
				filtercb = FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("cb", strcb));
			}
			listFilters.add(filtercb);
		}
		
		//速度
		if (sd != null && !"".equals(sd.trim()) && !",".equals(sd.trim())) {//速度一定要以逗号分隔，区间范围可以不写，如小于100的可写成 (,100)
			String strsd[] = sd.split(",", 2);
			if(strsd.length>1){
				if("".equals(strsd[0].trim())||strsd[0]==null){//速度起始为空
					if(!("".equals(strsd[1].trim())||strsd[1]==null)){//截止速度不为空
						filtersd = FilterBuilders.boolFilter().must(FilterBuilders.rangeFilter("sd").lt(strsd[1]).includeUpper(true));
					}					
				}
				if("".equals(strsd[1].trim())||strsd[1]==null){//开始速度不为空，截止速度为空
					filtersd = FilterBuilders.boolFilter().must(FilterBuilders.rangeFilter("sd").gt(strsd[0]).includeLower(true));
				}else {//范围查询
					filtersd = FilterBuilders.boolFilter().must(FilterBuilders.rangeFilter("sd").from(strsd[0]).to(strsd[1])
							.includeLower(true).includeUpper(true));
				}
				if(filtersd!=null){
					listFilters.add(filtersd);
				}
			}
		}
		
//		//排除一级红名单过车数据
//		if(hmdCphm != null && !"".equals(hmdCphm.trim())){
//			String strhphm[] = hmdCphm.split(",");
//			if (strhphm.length == 1) {// 只有一个车牌号码
//				filterhmd = FilterBuilders.boolFilter().mustNot(FilterBuilders.termFilter("cphm1", hmdCphm));
//			} else {// 多个车牌号码
//				filterhmd = FilterBuilders.boolFilter().mustNot(FilterBuilders.termFilter("cphm1", strhphm));
//			}
//			listFilters.add(filterhmd);
//		}
		
		//识别与未识别
		if("03".equals(business)){//未识别查询，添加前牌是否完成标志为0 
			qpsfwc = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("qpsfwc", "0"));
			listFilters.add(qpsfwc);
		}else {
			qpsfwc = FilterBuilders.boolFilter().mustNot(FilterBuilders.termFilter("qpsfwc", "0"));
			listFilters.add(qpsfwc);
		}
		return FilterBuilders.andFilter(listFilters.toArray(new FilterBuilder[] {})).cache(true);
	}
	
	/**
	 * @param kssj  开始时间
	 *            2013-01-02 10:10:10
	 * @param jssj    结束时间
	 * @param hpzl    号牌种类
	 * @param hphm    号牌号码
	 * @param cplx    车牌类型
	 * @param gcxh    过车序号
	 * @param jcdid   监测点id
	 * @param hmdCphm  红名单车牌号码<br>
	 * @return queryBuilder 查询条件
	 */
	public static BoolQueryBuilder getQueryBuilderByCon(String kssj, String jssj,
			String hpzl, String hphm, String cplx, String gcxh, String jcdid, 
			String cd, String cb, String sd, String hmdCphm, String business) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		BoolQueryBuilder queryBuilder = boolQuery();
		Config config = Config.getInstance();
		String qz1 = config.getQz1();//图片调用前缀
		String qz2 = config.getQz2();//图片调用前缀
		
		
		// 时间
		if (kssj != null && !"".equals(kssj.trim()) && jssj != null
				&& !"".equals(jssj.trim())) {
			// 开始和截止时间必填
			try {
				queryBuilder.must(QueryBuilders.rangeQuery("tgsj").from(sdf.parse(kssj).getTime())
						.to(sdf.parse(jssj).getTime()).includeLower(true).includeUpper(true));//rangeQuery 区间
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		// 监测点ID判断
		if (jcdid != null && !"".equals(jcdid.trim())) {
			jcdid = jcdid.replaceAll("'", "");
			String strjcdid[] = jcdid.split(",");//
			if (strjcdid.length == 1) {// 只有一个车牌号
				queryBuilder.must(QueryBuilders.termQuery("jcdid", jcdid));
			} else {// 多个监测点id
				//当多个监测点个数超过1024时，无法实现查询，采用should多个
				queryBuilder.should(QueryBuilders.termsQuery("jcdid", strjcdid));
//				int interval = 800;
//				for(int i=1;i<strjcdid.length;i+=interval){
//					String jcdidSub[] = new String[interval];
//					//拷贝数据源，拷贝开始位置，目标数组，开始位置，拷贝长度
//					System.out.println("数组长度---"+(strjcdid.length-i+1));
//					if((strjcdid.length-i+1)<interval){
//						jcdidSub = new String[strjcdid.length-i+1];
//						System.arraycopy(strjcdid, i, jcdidSub, 0, strjcdid.length-i);	
//					}else{
//						System.arraycopy(strjcdid, i, jcdidSub, 0, interval);	
//					}
//					queryBuilder.should(QueryBuilders.termsQuery("jcdid", jcdidSub));
//					System.out.println("截取监测点数据长度+++"+jcdidSub.length);
//				}
				System.out.println("查询监测点数据长度+++"+strjcdid.length);
			}
		}
		
		if (hphm != null && !"".equals(hphm.trim())) {// 号牌号码判断
			if("04".equals(business)){//模糊查询
				queryBuilder.must(QueryBuilders.wildcardQuery("cphm1", hphm));//wildcardQuery 模糊查询
			}else if("05".equals(business)){//前缀查询
				if(hphm.startsWith("!")){//非前缀查询
					queryBuilder.mustNot(QueryBuilders.prefixQuery("cphm1", hphm.length() > 1? hphm.substring(1):""));
				}else {
					if(hphm.substring(0, 1).equals(qz1) ){
						queryBuilder.must(QueryBuilders.prefixQuery("cphm1", hphm));
						queryBuilder.mustNot(QueryBuilders.prefixQuery("cphm1", qz2));
					}else{
						queryBuilder.must(QueryBuilders.prefixQuery("cphm1", hphm));//prefixQuery 前缀
					}
					
				}
			}else if(!"03".equals(business)){//非未识别查询
				String strhphm[] = hphm.split(",");// 多个车牌号
				if (strhphm.length == 1) {// 只有一个车牌号
					queryBuilder.must(QueryBuilders.termQuery("cphm1", hphm));//termQuery 一项匹配
				} else {// 多个车牌号
					queryBuilder.must(QueryBuilders.termsQuery("cphm1", strhphm));//termsQuery 多项匹配
				}
			}
		}
		
		// 车牌类型
		if (cplx != null && !"".equals(cplx.trim())) {
			String strcplx[] = cplx.split(",");//
			if (strcplx.length == 1) {// 只有一个车牌号
				queryBuilder.must(QueryBuilders.termQuery("cplx1", cplx));
			} else {// 多个车牌号
				queryBuilder.must(QueryBuilders.termsQuery("cplx1", strcplx));
			}
		}
		
		// 图片
		if (gcxh != null && !"".equals(gcxh.trim())) {
			String strgcxh[] = gcxh.split(",");//
			if (strgcxh.length == 1) {// 只有一个图片
				queryBuilder.must(QueryBuilders.termQuery("tpid1", gcxh));
			} else {// 多个图片
				queryBuilder.must(QueryBuilders.termsQuery("tpid1", strgcxh));
			}
		}
		
		//车道
		if (cd != null && !"".equals(cd.trim())) {
			String strcd[] = cd.split(",");
			if (strcd.length == 1) {
				queryBuilder.must(QueryBuilders.termQuery("cdid", cd));
			} else {// 多个车道
				queryBuilder.must(QueryBuilders.termsQuery("cdid", strcd));
			}
		}
		
		//车标
		if (cb != null && !"".equals(cb.trim())) {
			queryBuilder.must(QueryBuilders.wildcardQuery("cb", cb.trim()));//wildcardQuery 模糊查询
		}
		
		//速度
		if (sd != null && !"".equals(sd.trim())) {
			String strsd[] = sd.split(",");
			if(strsd.length == 1){
				queryBuilder.must(QueryBuilders.rangeQuery("sd").from(Integer.parseInt(strsd[0]))
						.to(999999999).includeLower(true).includeUpper(true));//rangeQuery 区间
			}else if(strsd.length > 1){
				if("".equals(strsd[0])){
					queryBuilder.must(QueryBuilders.rangeQuery("sd").from(-999999999)
							.to(Integer.parseInt(strsd[1])).includeLower(true).includeUpper(true));//rangeQuery 区间
				}else{
					queryBuilder.must(QueryBuilders.rangeQuery("sd").from(Integer.parseInt(strsd[0]))
							.to(Integer.parseInt(strsd[1])).includeLower(true).includeUpper(true));//rangeQuery 区间
				}
			}
		}
		
//		//排除一级红名单过车数据
//		if(hmdCphm != null && !"".equals(hmdCphm.trim())){
//			String strhphm[] = hmdCphm.split(",");
//			if (strhphm.length == 1) {// 只有一个车牌号码
//				queryBuilder.mustNot(QueryBuilders.termQuery("cphm1", hmdCphm));
//			} else {// 多个车牌号码
//				queryBuilder.mustNot(QueryBuilders.termsQuery("cphm1", strhphm));
//			}
//		}
		
//		识别与未识别
		if("03".equals(business)){//未识别查询，添加前牌是否完成标志为0 
			queryBuilder.mustNot(QueryBuilders.termQuery("qpsfwc", "1"));
		}else {
			queryBuilder.must(QueryBuilders.termQuery("qpsfwc", "1"));
		}
		return queryBuilder;
	}
	/**
	 * 分组统计查询filter生成filter
	 * @param kssj  开始时间
	 *            2013-01-02 10:10:10
	 * @param jssj    结束时间
	 * @param hpzl    号牌种类
	 * @param hphm    号牌号码
	 * @param cplx    车牌类型
	 * @param gcxh    过车序号
	 * @param jcdid   监测点id
	 * @param hmdCphm  红名单车牌号码<br>
	 * @return filter 过滤器
	 */
	public static BoolQueryBuilder getFacterQuery(String kssj, String jssj,
			String hpzl, String hphm, String cplx, String gcxh, String jcdid, 
			String cd, String cb, String sd, String hmdCphm, String business,String sbzt) {
		BoolQueryBuilder query = boolQuery();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 时间,,条件苛刻的放在前面
		if (kssj != null && !"".equals(kssj.trim()) && jssj != null&& !"".equals(jssj.trim())) {
			// 开始和截止时间必填
			try {
				query.must(rangeQuery("tgsj")
						.from(sdf.parse(kssj).getTime())
						.to(sdf.parse(jssj).getTime()).includeLower(true)
						.includeUpper(true));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		// 监测点ID判断
		if (jcdid != null && !"".equals(jcdid.trim())) {
			jcdid = jcdid.replaceAll("'", "");
			String strjcdid[] = jcdid.split(",");//
			if (strjcdid.length == 1) {// 
				query.must(termQuery("jcdid", jcdid));
			} else {// 多个车牌号
				query.must(termsQuery("jcdid", strjcdid));
			}
		}
		if (hphm != null && !"".equals(hphm.trim())) {// 号牌号码判断
			if("01".equals(business)){//模糊查询
				query.must(wildcardQuery("cphm1", hphm));
			}else if("02".equals(business)){//前缀查询
				if(hphm.startsWith("!")){//非前缀查询
					query.mustNot(prefixQuery("cphm1", hphm.length() > 1? hphm.substring(1):""));
				}else {
					query.must(prefixQuery("cphm1", hphm));
				}
			}else{//非未识别查询
				String strhphm[] = hphm.split(",");// 多个车牌号
				if (strhphm.length == 1) {// 只有一个车牌号
					query.must(termQuery("cphm1", hphm));
				} else {// 多个车牌号
					query.must(termsQuery("cphm1", strhphm));
				}
			}
		}
		// 车牌类型
		if (cplx != null && !"".equals(cplx.trim())) {
			String strcplx[] = cplx.split(",");//
			if (strcplx.length == 1) {// 只有一个车牌号
				query.must(termQuery("cplx1", cplx));
			} else {// 多个车牌号
				query.must(termsQuery("cplx1", strcplx));
			}
		}
		// 图片
		if (gcxh != null && !"".equals(gcxh.trim())) {
			String strgcxh[] = gcxh.split(",");//
			if (strgcxh.length == 1) {// 只有一个图片
				query.must(termQuery("tpid1", gcxh));
			} else {// 多个图片
				query.must(termsQuery("tpid1", strgcxh));
			}
		}
		
		//车道
		if (cd != null && !"".equals(cd.trim())) {
			String strcd[] = cd.split(",");
			if (strcd.length == 1) {
				query.must(termQuery("cdid", cd));
			} else {// 多个车道
				query.must(termsQuery("cdid", strcd));
			}
		}
		
		//车标
		if (cb != null && !"".equals(cb.trim())) {
			String strcb[] = cb.split(",");
			if (strcb.length == 1) {
				query.must(termQuery("cb", cb));
			} else {// 多个车道
				query.must(termsQuery("cb", strcb));
			}
		}
		
		//速度
		if (sd != null && !"".equals(sd.trim())) {//速度一定要以逗号分隔，区间范围可以不写，如小于100的可写成 (,100)
			String strsd[] = sd.split(",",2);
			if(strsd.length>1){
				if("".equals(strsd[0].trim())||strsd[0]==null){//速度起始为空
					if(!("".equals(strsd[1].trim())||strsd[1]==null)){//截止速度不为空
						query.must(rangeQuery("sd").lt(strsd[1]).includeUpper(true));
					}					
				}else if("".equals(strsd[1].trim())||strsd[1]==null){//开始速度不为空，截止速度为空
					query.must(rangeQuery("sd").gt(strsd[0]).includeLower(true));
				}else {//范围查询
					query.must(rangeQuery("sd").from(strsd[0]).to(strsd[1])
							.includeLower(true).includeUpper(true));
				}
			}
		}
		
//		//排除一级红名单过车数据
//		if(hmdCphm != null && !"".equals(hmdCphm.trim())){
//			String strhphm[] = hmdCphm.split(",");
//			if (strhphm.length == 1) {// 只有一个车牌号码
//				query.mustNot(termQuery("cphm1", hmdCphm));
//			} else {// 多个车牌号码
//				query.mustNot(termsQuery("cphm1", strhphm));
//			}
//		}
		//识别与未识别
		if(sbzt!=null&&!"".equals(sbzt)){ 
			query.must(termQuery("qpsfwc", sbzt));
		}
		return query;
	}
}