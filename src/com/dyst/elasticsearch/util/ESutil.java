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
	 * ���ݸ�����������Filter���ţӲ�ѯʹ��
	 * @param kssj  ��ʼʱ��
	 *            2013-01-02 10:10:10
	 * @param jssj    ����ʱ��
	 * @param hpzl    ��������
	 * @param hphm    ���ƺ���
	 * @param cplx    ��������
	 * @param gcxh    �������
	 * @param jcdid   ����id
	 * @param hmdCphm  ���������ƺ���<br>
	 * @return filter ������
	 */
	public static FilterBuilder getFilterByCon(String kssj, String jssj,
			String hpzl, String hphm, String cplx, String gcxh, String jcdid, 
			String cd, String cb, String sd, String hmdCphm, String business) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		FilterBuilder filtersbsj = null;//ʶ��ʱ��
		FilterBuilder filtercd = null;//����
		FilterBuilder filterhphm = null;//����
		FilterBuilder filtercplx = null;//��������
		FilterBuilder filtergcxh = null;//�������
		FilterBuilder filterjcdid = null;//����id
		FilterBuilder filtersd = null;//�ٶ�
		FilterBuilder qpsfwc = null;//ǰ���Ƿ����
		FilterBuilder filtercb = null;//����
//		FilterBuilder filterhmd = null;//����������
		Config config = Config.getInstance();
		String qz1 = config.getQz1();//ͼƬ����ǰ׺
		String qz2 = config.getQz2();//ͼƬ����ǰ׺
		List<FilterBuilder> listFilters = new ArrayList<FilterBuilder>();// filter����
		
		// ʱ��,,�������̵ķ���ǰ��
		if (kssj != null && !"".equals(kssj.trim()) && jssj != null
				&& !"".equals(jssj.trim())) {
			// ��ʼ�ͽ�ֹʱ�����
			try {
				filtersbsj = FilterBuilders.boolFilter().must(
						FilterBuilders.rangeFilter("tgsj").from(sdf.parse(kssj).getTime())
						.to(sdf.parse(jssj).getTime()).includeLower(true).includeUpper(true));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			listFilters.add(filtersbsj);
		}
		// ����ID�ж�
		if (jcdid != null && !"".equals(jcdid.trim())) {
			jcdid = jcdid.replaceAll("'", "");
			String strjcdid[] = jcdid.split(",");//
			if (strjcdid.length == 1) {// 
				filterjcdid = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("jcdid", jcdid));
			} else {// ������ƺ�
				filterjcdid = FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("jcdid",strjcdid));
			}
			listFilters.add(filterjcdid);
		}
		if (hphm != null && !"".equals(hphm.trim())) {// ���ƺ����ж�
			if("04".equals(business)){//ģ����ѯ
				filterhphm = FilterBuilders.boolFilter().must(FilterBuilders.queryFilter(QueryBuilders.wildcardQuery("cphm1", hphm)));//wildcardQuery ģ����ѯ
			}else if("05".equals(business)){//ǰ׺��ѯ
				if(hphm.startsWith("!")){//��ǰ׺��ѯ
					filterhphm =  FilterBuilders.boolFilter().mustNot(FilterBuilders.prefixFilter("cphm1", hphm.length() > 1? hphm.substring(1):""));
				}else {
					if(hphm.substring(0, 1).equals(qz1) ){
						//��ѯ��ʡ�Ǳ��س������ɿ�ͷ��������C�ĳ��ƺ�
						filterhphm = FilterBuilders.boolFilter().must(FilterBuilders.prefixFilter("cphm1", hphm))
						.mustNot(FilterBuilders.prefixFilter("cphm1", qz2));
//						queryBuilder.mustNot(QueryBuilders.prefixQuery("cphm1", qz2));
					}else{
						filterhphm =  FilterBuilders.boolFilter().must(FilterBuilders.prefixFilter("cphm1", hphm));//prefixQuery ǰ׺
					}
				}
			}else if(!"03".equals(business)){//��δʶ���ѯ
				String strhphm[] = hphm.split(",");// ������ƺ�
				if (strhphm.length == 1) {// ֻ��һ�����ƺ�
					filterhphm = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("cphm1", hphm));//termQuery һ��ƥ��
				} else {// ������ƺ�
					filterhphm = FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("cphm1", strhphm));//termsQuery ����ƥ��
				}
			}
			listFilters.add(filterhphm);
		}
		// ��������
		if (cplx != null && !"".equals(cplx.trim())) {
			String strcplx[] = cplx.split(",");//
			if (strcplx.length == 1) {// ֻ��һ�����ƺ�
				filtercplx = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("cplx1", cplx));
			} else {// ������ƺ�
				filtercplx = FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("cplx1", strcplx));
			}
			listFilters.add(filtercplx);
		}
		// ͼƬ
		if (gcxh != null && !"".equals(gcxh.trim())) {
			String strgcxh[] = gcxh.split(",");//
			if (strgcxh.length == 1) {// ֻ��һ��ͼƬ
				filtergcxh = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("tpid1", gcxh));
			} else {// ���ͼƬ
				filtergcxh = FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("tpid1", strgcxh));
			}
			listFilters.add(filtergcxh);
		}
		
		//����
		if (cd != null && !"".equals(cd.trim())) {
			String strcd[] = cd.split(",");
			if (strcd.length == 1) {
				filtercd = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("cdid", cd));
			} else {// �������
				filtercd = FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("cdid", strcd));
			}
			listFilters.add(filtercd);
		}
		
		//����
		if (cb != null && !"".equals(cb.trim())) {
			String strcb[] = cb.split(",");
			if (strcb.length == 1) {
				filtercb = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("cb", cb));
			} else {// �������
				filtercb = FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("cb", strcb));
			}
			listFilters.add(filtercb);
		}
		
		//�ٶ�
		if (sd != null && !"".equals(sd.trim()) && !",".equals(sd.trim())) {//�ٶ�һ��Ҫ�Զ��ŷָ������䷶Χ���Բ�д����С��100�Ŀ�д�� (,100)
			String strsd[] = sd.split(",", 2);
			if(strsd.length>1){
				if("".equals(strsd[0].trim())||strsd[0]==null){//�ٶ���ʼΪ��
					if(!("".equals(strsd[1].trim())||strsd[1]==null)){//��ֹ�ٶȲ�Ϊ��
						filtersd = FilterBuilders.boolFilter().must(FilterBuilders.rangeFilter("sd").lt(strsd[1]).includeUpper(true));
					}					
				}
				if("".equals(strsd[1].trim())||strsd[1]==null){//��ʼ�ٶȲ�Ϊ�գ���ֹ�ٶ�Ϊ��
					filtersd = FilterBuilders.boolFilter().must(FilterBuilders.rangeFilter("sd").gt(strsd[0]).includeLower(true));
				}else {//��Χ��ѯ
					filtersd = FilterBuilders.boolFilter().must(FilterBuilders.rangeFilter("sd").from(strsd[0]).to(strsd[1])
							.includeLower(true).includeUpper(true));
				}
				if(filtersd!=null){
					listFilters.add(filtersd);
				}
			}
		}
		
//		//�ų�һ����������������
//		if(hmdCphm != null && !"".equals(hmdCphm.trim())){
//			String strhphm[] = hmdCphm.split(",");
//			if (strhphm.length == 1) {// ֻ��һ�����ƺ���
//				filterhmd = FilterBuilders.boolFilter().mustNot(FilterBuilders.termFilter("cphm1", hmdCphm));
//			} else {// ������ƺ���
//				filterhmd = FilterBuilders.boolFilter().mustNot(FilterBuilders.termFilter("cphm1", strhphm));
//			}
//			listFilters.add(filterhmd);
//		}
		
		//ʶ����δʶ��
		if("03".equals(business)){//δʶ���ѯ�����ǰ���Ƿ���ɱ�־Ϊ0 
			qpsfwc = FilterBuilders.boolFilter().must(FilterBuilders.termFilter("qpsfwc", "0"));
			listFilters.add(qpsfwc);
		}else {
			qpsfwc = FilterBuilders.boolFilter().mustNot(FilterBuilders.termFilter("qpsfwc", "0"));
			listFilters.add(qpsfwc);
		}
		return FilterBuilders.andFilter(listFilters.toArray(new FilterBuilder[] {})).cache(true);
	}
	
	/**
	 * @param kssj  ��ʼʱ��
	 *            2013-01-02 10:10:10
	 * @param jssj    ����ʱ��
	 * @param hpzl    ��������
	 * @param hphm    ���ƺ���
	 * @param cplx    ��������
	 * @param gcxh    �������
	 * @param jcdid   ����id
	 * @param hmdCphm  ���������ƺ���<br>
	 * @return queryBuilder ��ѯ����
	 */
	public static BoolQueryBuilder getQueryBuilderByCon(String kssj, String jssj,
			String hpzl, String hphm, String cplx, String gcxh, String jcdid, 
			String cd, String cb, String sd, String hmdCphm, String business) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		BoolQueryBuilder queryBuilder = boolQuery();
		Config config = Config.getInstance();
		String qz1 = config.getQz1();//ͼƬ����ǰ׺
		String qz2 = config.getQz2();//ͼƬ����ǰ׺
		
		
		// ʱ��
		if (kssj != null && !"".equals(kssj.trim()) && jssj != null
				&& !"".equals(jssj.trim())) {
			// ��ʼ�ͽ�ֹʱ�����
			try {
				queryBuilder.must(QueryBuilders.rangeQuery("tgsj").from(sdf.parse(kssj).getTime())
						.to(sdf.parse(jssj).getTime()).includeLower(true).includeUpper(true));//rangeQuery ����
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		// ����ID�ж�
		if (jcdid != null && !"".equals(jcdid.trim())) {
			jcdid = jcdid.replaceAll("'", "");
			String strjcdid[] = jcdid.split(",");//
			if (strjcdid.length == 1) {// ֻ��һ�����ƺ�
				queryBuilder.must(QueryBuilders.termQuery("jcdid", jcdid));
			} else {// �������id
				//����������������1024ʱ���޷�ʵ�ֲ�ѯ������should���
				queryBuilder.should(QueryBuilders.termsQuery("jcdid", strjcdid));
//				int interval = 800;
//				for(int i=1;i<strjcdid.length;i+=interval){
//					String jcdidSub[] = new String[interval];
//					//��������Դ��������ʼλ�ã�Ŀ�����飬��ʼλ�ã���������
//					System.out.println("���鳤��---"+(strjcdid.length-i+1));
//					if((strjcdid.length-i+1)<interval){
//						jcdidSub = new String[strjcdid.length-i+1];
//						System.arraycopy(strjcdid, i, jcdidSub, 0, strjcdid.length-i);	
//					}else{
//						System.arraycopy(strjcdid, i, jcdidSub, 0, interval);	
//					}
//					queryBuilder.should(QueryBuilders.termsQuery("jcdid", jcdidSub));
//					System.out.println("��ȡ�������ݳ���+++"+jcdidSub.length);
//				}
				System.out.println("��ѯ�������ݳ���+++"+strjcdid.length);
			}
		}
		
		if (hphm != null && !"".equals(hphm.trim())) {// ���ƺ����ж�
			if("04".equals(business)){//ģ����ѯ
				queryBuilder.must(QueryBuilders.wildcardQuery("cphm1", hphm));//wildcardQuery ģ����ѯ
			}else if("05".equals(business)){//ǰ׺��ѯ
				if(hphm.startsWith("!")){//��ǰ׺��ѯ
					queryBuilder.mustNot(QueryBuilders.prefixQuery("cphm1", hphm.length() > 1? hphm.substring(1):""));
				}else {
					if(hphm.substring(0, 1).equals(qz1) ){
						queryBuilder.must(QueryBuilders.prefixQuery("cphm1", hphm));
						queryBuilder.mustNot(QueryBuilders.prefixQuery("cphm1", qz2));
					}else{
						queryBuilder.must(QueryBuilders.prefixQuery("cphm1", hphm));//prefixQuery ǰ׺
					}
					
				}
			}else if(!"03".equals(business)){//��δʶ���ѯ
				String strhphm[] = hphm.split(",");// ������ƺ�
				if (strhphm.length == 1) {// ֻ��һ�����ƺ�
					queryBuilder.must(QueryBuilders.termQuery("cphm1", hphm));//termQuery һ��ƥ��
				} else {// ������ƺ�
					queryBuilder.must(QueryBuilders.termsQuery("cphm1", strhphm));//termsQuery ����ƥ��
				}
			}
		}
		
		// ��������
		if (cplx != null && !"".equals(cplx.trim())) {
			String strcplx[] = cplx.split(",");//
			if (strcplx.length == 1) {// ֻ��һ�����ƺ�
				queryBuilder.must(QueryBuilders.termQuery("cplx1", cplx));
			} else {// ������ƺ�
				queryBuilder.must(QueryBuilders.termsQuery("cplx1", strcplx));
			}
		}
		
		// ͼƬ
		if (gcxh != null && !"".equals(gcxh.trim())) {
			String strgcxh[] = gcxh.split(",");//
			if (strgcxh.length == 1) {// ֻ��һ��ͼƬ
				queryBuilder.must(QueryBuilders.termQuery("tpid1", gcxh));
			} else {// ���ͼƬ
				queryBuilder.must(QueryBuilders.termsQuery("tpid1", strgcxh));
			}
		}
		
		//����
		if (cd != null && !"".equals(cd.trim())) {
			String strcd[] = cd.split(",");
			if (strcd.length == 1) {
				queryBuilder.must(QueryBuilders.termQuery("cdid", cd));
			} else {// �������
				queryBuilder.must(QueryBuilders.termsQuery("cdid", strcd));
			}
		}
		
		//����
		if (cb != null && !"".equals(cb.trim())) {
			queryBuilder.must(QueryBuilders.wildcardQuery("cb", cb.trim()));//wildcardQuery ģ����ѯ
		}
		
		//�ٶ�
		if (sd != null && !"".equals(sd.trim())) {
			String strsd[] = sd.split(",");
			if(strsd.length == 1){
				queryBuilder.must(QueryBuilders.rangeQuery("sd").from(Integer.parseInt(strsd[0]))
						.to(999999999).includeLower(true).includeUpper(true));//rangeQuery ����
			}else if(strsd.length > 1){
				if("".equals(strsd[0])){
					queryBuilder.must(QueryBuilders.rangeQuery("sd").from(-999999999)
							.to(Integer.parseInt(strsd[1])).includeLower(true).includeUpper(true));//rangeQuery ����
				}else{
					queryBuilder.must(QueryBuilders.rangeQuery("sd").from(Integer.parseInt(strsd[0]))
							.to(Integer.parseInt(strsd[1])).includeLower(true).includeUpper(true));//rangeQuery ����
				}
			}
		}
		
//		//�ų�һ����������������
//		if(hmdCphm != null && !"".equals(hmdCphm.trim())){
//			String strhphm[] = hmdCphm.split(",");
//			if (strhphm.length == 1) {// ֻ��һ�����ƺ���
//				queryBuilder.mustNot(QueryBuilders.termQuery("cphm1", hmdCphm));
//			} else {// ������ƺ���
//				queryBuilder.mustNot(QueryBuilders.termsQuery("cphm1", strhphm));
//			}
//		}
		
//		ʶ����δʶ��
		if("03".equals(business)){//δʶ���ѯ�����ǰ���Ƿ���ɱ�־Ϊ0 
			queryBuilder.mustNot(QueryBuilders.termQuery("qpsfwc", "1"));
		}else {
			queryBuilder.must(QueryBuilders.termQuery("qpsfwc", "1"));
		}
		return queryBuilder;
	}
	/**
	 * ����ͳ�Ʋ�ѯfilter����filter
	 * @param kssj  ��ʼʱ��
	 *            2013-01-02 10:10:10
	 * @param jssj    ����ʱ��
	 * @param hpzl    ��������
	 * @param hphm    ���ƺ���
	 * @param cplx    ��������
	 * @param gcxh    �������
	 * @param jcdid   ����id
	 * @param hmdCphm  ���������ƺ���<br>
	 * @return filter ������
	 */
	public static BoolQueryBuilder getFacterQuery(String kssj, String jssj,
			String hpzl, String hphm, String cplx, String gcxh, String jcdid, 
			String cd, String cb, String sd, String hmdCphm, String business,String sbzt) {
		BoolQueryBuilder query = boolQuery();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// ʱ��,,�������̵ķ���ǰ��
		if (kssj != null && !"".equals(kssj.trim()) && jssj != null&& !"".equals(jssj.trim())) {
			// ��ʼ�ͽ�ֹʱ�����
			try {
				query.must(rangeQuery("tgsj")
						.from(sdf.parse(kssj).getTime())
						.to(sdf.parse(jssj).getTime()).includeLower(true)
						.includeUpper(true));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		// ����ID�ж�
		if (jcdid != null && !"".equals(jcdid.trim())) {
			jcdid = jcdid.replaceAll("'", "");
			String strjcdid[] = jcdid.split(",");//
			if (strjcdid.length == 1) {// 
				query.must(termQuery("jcdid", jcdid));
			} else {// ������ƺ�
				query.must(termsQuery("jcdid", strjcdid));
			}
		}
		if (hphm != null && !"".equals(hphm.trim())) {// ���ƺ����ж�
			if("01".equals(business)){//ģ����ѯ
				query.must(wildcardQuery("cphm1", hphm));
			}else if("02".equals(business)){//ǰ׺��ѯ
				if(hphm.startsWith("!")){//��ǰ׺��ѯ
					query.mustNot(prefixQuery("cphm1", hphm.length() > 1? hphm.substring(1):""));
				}else {
					query.must(prefixQuery("cphm1", hphm));
				}
			}else{//��δʶ���ѯ
				String strhphm[] = hphm.split(",");// ������ƺ�
				if (strhphm.length == 1) {// ֻ��һ�����ƺ�
					query.must(termQuery("cphm1", hphm));
				} else {// ������ƺ�
					query.must(termsQuery("cphm1", strhphm));
				}
			}
		}
		// ��������
		if (cplx != null && !"".equals(cplx.trim())) {
			String strcplx[] = cplx.split(",");//
			if (strcplx.length == 1) {// ֻ��һ�����ƺ�
				query.must(termQuery("cplx1", cplx));
			} else {// ������ƺ�
				query.must(termsQuery("cplx1", strcplx));
			}
		}
		// ͼƬ
		if (gcxh != null && !"".equals(gcxh.trim())) {
			String strgcxh[] = gcxh.split(",");//
			if (strgcxh.length == 1) {// ֻ��һ��ͼƬ
				query.must(termQuery("tpid1", gcxh));
			} else {// ���ͼƬ
				query.must(termsQuery("tpid1", strgcxh));
			}
		}
		
		//����
		if (cd != null && !"".equals(cd.trim())) {
			String strcd[] = cd.split(",");
			if (strcd.length == 1) {
				query.must(termQuery("cdid", cd));
			} else {// �������
				query.must(termsQuery("cdid", strcd));
			}
		}
		
		//����
		if (cb != null && !"".equals(cb.trim())) {
			String strcb[] = cb.split(",");
			if (strcb.length == 1) {
				query.must(termQuery("cb", cb));
			} else {// �������
				query.must(termsQuery("cb", strcb));
			}
		}
		
		//�ٶ�
		if (sd != null && !"".equals(sd.trim())) {//�ٶ�һ��Ҫ�Զ��ŷָ������䷶Χ���Բ�д����С��100�Ŀ�д�� (,100)
			String strsd[] = sd.split(",",2);
			if(strsd.length>1){
				if("".equals(strsd[0].trim())||strsd[0]==null){//�ٶ���ʼΪ��
					if(!("".equals(strsd[1].trim())||strsd[1]==null)){//��ֹ�ٶȲ�Ϊ��
						query.must(rangeQuery("sd").lt(strsd[1]).includeUpper(true));
					}					
				}else if("".equals(strsd[1].trim())||strsd[1]==null){//��ʼ�ٶȲ�Ϊ�գ���ֹ�ٶ�Ϊ��
					query.must(rangeQuery("sd").gt(strsd[0]).includeLower(true));
				}else {//��Χ��ѯ
					query.must(rangeQuery("sd").from(strsd[0]).to(strsd[1])
							.includeLower(true).includeUpper(true));
				}
			}
		}
		
//		//�ų�һ����������������
//		if(hmdCphm != null && !"".equals(hmdCphm.trim())){
//			String strhphm[] = hmdCphm.split(",");
//			if (strhphm.length == 1) {// ֻ��һ�����ƺ���
//				query.mustNot(termQuery("cphm1", hmdCphm));
//			} else {// ������ƺ���
//				query.mustNot(termsQuery("cphm1", strhphm));
//			}
//		}
		//ʶ����δʶ��
		if(sbzt!=null&&!"".equals(sbzt)){ 
			query.must(termQuery("qpsfwc", sbzt));
		}
		return query;
	}
}