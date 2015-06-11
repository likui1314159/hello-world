package com.dyst.oracle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dyst.util.Config;
import com.dyst.util.InterUtil;

/**
 * SQL���������
 * @author Administrator
 *
 */
public class SearchInfo {
	/**
	 * ���ݲ�������SQL���  1��ʶ���ʽ
	 * @param kssj   ��ʼʱ��<br>
	 * @param jssj   ��ֹʱ��<br>
	 * @param hpzl   ��������<br>
	 * @param cphid  ���ƺ�ID<br>
	 * @param cplx   ��������<br>
	 * @param gcxh   �������<br>
	 * @param jcdid  ����id<br>
	 * @param hmdCphm  ���������ƺ���<br>
	 * @param bussiness ҵ������<br>
	 * @param flag      ��־״̬��0:��ѯ��¼��1:��ѯ����
	 * @return ���ɵ�SQL���
	 */
	public String getSqlByCon(String kssj, String jssj,
			String hpzl, String cphid, String cplx, String gcxh, String jcdid,
			String cd, String cb, String sd, String hmdCphm, String bussiness, String flag) {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd");
		
		Config config = Config.getInstance();
		String qz1 = config.getQz1();//ͼƬ����ǰ׺
		String qz2 = config.getQz2();//ͼƬ����ǰ׺
        String selectTable = "sb";

        //��ȡ��
        Set<String> al = InterUtil.process(kssj, jssj);
        String day = "";
			 //��װ
		    for(String d : al){
		    	try {
					day += sdf2.format(sdf1.parse(d))+",";
				} catch (ParseException e) {
					e.printStackTrace();
				}
		    }
			if(day.length() > 0){
				day = day.substring(0, day.length() - 1);
			}
		
        
        //������ѯ���
		StringBuffer strSql = new StringBuffer("select cphm1,cplx1,tgsj,tpid1,tpid2,tpid3,tpid4,tpid5,jcdid,cdid,cb,sd,tpzs,rownum rn from " + selectTable + " where 1=1 and fqh in (" + day + ")");
		
		if("1".equals(flag)){//��ѯ��¼����
			strSql = new StringBuffer("select count(1) as count from " + selectTable + " where 1=1 and fqh in (" + day + ")");
		}
		
		//�������
		if (cphid != null && !"".equals(cphid.trim())) {
			//���ƺ����ж�,ֻ����ʶ������ѯ��ģ����ѯ��������ѯ�ŷ�����ӳ��ƺ�����
			String strhphm[] = cphid.split(",");
			if("01".equals(bussiness.trim())){//��ʶ�𳵹켣��ѯ�ӿ�
				if(strhphm.length == 1){
					strSql.append(" and cphm1 = '" + cphid + "'");
				}else{
					strSql.append(" and cphm1 in (" + InterUtil.getStr(strhphm) + ")");
				}
			}else if("04".equals(bussiness.trim())){
				//����ģ����ѯ��ֻ��һ�����ƺ�,����*���������ַ������������ַ�;��Oracle���ݿ�����Ҫת����%��_
				strSql.append(" and cphm1 like '" + cphid.replace("*", "%").replace("?", "_") + "'");
			}else if("05".equals(bussiness.trim())){//ǰ׺��ѯ
				if(cphid.startsWith("!")){//��ǰ׺��ѯ
					strSql.append(" and cphm1 not like '" + (cphid.length() > 1? cphid.substring(1):"") + "%'");
				}else{
					if(cphid.subSequence(0, 1).equals(qz1)){
						strSql.append(" and cphm1 like '" + cphid + "%' and cphm1 not like '" + qz2 + "%'");
					}else{
						strSql.append(" and cphm1 like '" + cphid + "%'");
					}
				}
			}
		}
		
		//��������
		if (jcdid != null && !"".equals(jcdid.trim())) {// ����ID�ж�
			String strjcdid[] = jcdid.split(",");// ������ƺ�
			if (strjcdid.length == 1) {
				strSql.append(" and jcdid = '" + jcdid + "'");
			} else {
				strSql.append(" and (jcdid in ('00000000')");
				
				String[] aa = new String[500];
				int j = 0;
				for(int k = 0;k < strjcdid.length;k++){
					aa[j] = strjcdid[k];
					if((j+1) == 500){
						strSql.append(" or jcdid in (" + InterUtil.getStr(aa) + ")");
						aa = new String[500];
						j = 0;
					}else{
						j++;
					}
					
					if(j > 0 && k == strjcdid.length - 1){
						aa = Arrays.copyOf(aa, j);
						strSql.append(" or jcdid in (" + InterUtil.getStr(aa) + ")");
					}
				}
				strSql.append(" )");
			}
		}
		//������������
		if (cplx != null && !"".equals(cplx.trim())) {
			String strcplx[] = cplx.split(",");
			if (strcplx.length == 1) {
				strSql.append(" and cplx1 = '"+cplx+"'");
			} else {// ������ƺ�
				strSql.append(" and cplx1 in ("+InterUtil.getStr(strcplx)+")");
			}
		}
		
		//ͼƬid����
		if (gcxh != null && !"".equals(gcxh.trim())) {
			String strgcxh[] = gcxh.split(",");
			if (strgcxh.length == 1) {
				strSql.append(" and tpid1 = '" + gcxh + "'");
			} else {// ���ͼƬid
				strSql.append(" and tpid1 in ("+InterUtil.getStr(strgcxh)+")");
			}
		}
		
		//����
		if (cd != null && !"".equals(cd.trim())) {
			String strcd[] = cd.split(",");
			if (strcd.length == 1) {
				strSql.append(" and cdid = '" + cd + "'");
			} else {// �������
				strSql.append(" and cdid in (" + InterUtil.getStr(strcd) + ")");
			}
		}
		
		//����
		if (cb != null && !"".equals(cb.trim())) {
			strSql.append(" and cb like '%" + cb.trim() + "%'");
		}
		
		//�ٶ�
		if (sd != null && !"".equals(sd.trim()) && !",".equals(sd.trim())) {
			String strsd[] = sd.split(",");
			if(strsd.length == 1){
				strSql.append(" and sd >= " + strsd[0]);
			}else if(strsd.length > 1){
				if("".equals(strsd[0])){
					strSql.append(" and sd <= " + strsd[1]);
				}else{
					strSql.append(" and sd >= " + strsd[0] + " and sd <= " + strsd[1]);
				}
			}
		}
		
		//ʶ��ʱ������
		if (kssj != null && !"".equals(kssj.trim()) && jssj != null
				&& !"".equals(jssj.trim())) {
			// ��ʼ�ͽ�ֹʱ�����
			strSql.append(" and tgsj between to_date('" + kssj + "','yyyy-MM-dd HH24:mi:ss') and to_date('" + jssj + "','yyyy-MM-dd HH24:mi:ss')");
		}
		
		//�ų�һ����������������
//		if(hmdCphm != null && !"".equals(hmdCphm.trim())){
//			String strhphm[] = hmdCphm.split(",");
//			if (strhphm.length == 1) {
//				strSql.append(" and cphm1 != '" + hmdCphm + "'");
//			} else {// ������ƺ�
//				String[] aa = new String[500];
//				int j = 0;
//				for(int k = 0;k < strhphm.length;k++){
//					aa[j] = strhphm[k];
//					if((j+1) == 500){
//						strSql.append(" and cphm1 not in (" + InterUtil.getStr(aa) + ")");
//						aa = new String[500];
//						j = 0;
//					}else{
//						j++;
//					}
//					
//					if(j > 0 && k == strhphm.length - 1){
//						aa = Arrays.copyOf(aa, j);
//						strSql.append(" and cphm1 not in (" + InterUtil.getStr(aa) + ")");
//					}
//				}
//			}
//		}
		
		//δʶ������ѯ,����Ƿ���ɱ�־λ0
        if("03".equals(bussiness.trim())){//δʶ���ѯ
     	  strSql.append(" and qpsfwc = '0'");
        }else{
          strSql.append(" and qpsfwc != '0'");//��ʶ���ѯ
        }
//        System.out.println(strSql.toString());
		return strSql.toString();		
	}
	
	/**
	 * ���ݲ�������SQL��� 31��ʶ���ʽ
	 * @param kssj   ��ʼʱ��<br>
	 * @param jssj   ��ֹʱ��<br>
	 * @param hpzl   ��������<br>
	 * @param cphid  ���ƺ�ID<br>
	 * @param cplx   ��������<br>
	 * @param gcxh   �������<br>
	 * @param jcdid  ����id<br>
	 * @param hmdCphm  ���������ƺ���<br>
	 * @param flag      ��־״̬��0:��ѯ��¼��1:��ѯ����
	 * @return ���ɵ�SQL���
	 */
	@SuppressWarnings("unchecked")
	public String getSB31SqlByCon(String begintime, String endtime, String hpzl, 
			String cphid, String cplx, String gcxh, String jcdid, 
			String hmdCphm, String bussiness, String flag) {
		String str_sql = null;
		InterUtil inter = new InterUtil();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date tm1 = null;
		Date tm2 = null;
		
		Config config = Config.getInstance();
		String qz1 = config.getQz1();//ͼƬ����ǰ׺
		String qz2 = config.getQz2();//ͼƬ����ǰ׺
		
		if(begintime != null && !"".equals(begintime.trim())){
			try {
				tm1 = sdf.parse(begintime);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if(endtime!=null&&!"".equals(endtime.trim())){
			try {
				tm2 = sdf.parse(endtime);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		//δʶ���ѯ����ʱδ���
		if("03".equals(bussiness.trim())){
			return "";
		}
			  
		if (tm1 != null && tm2 != null) {
			List list = inter.getSbList(tm1, tm2);
			if(list.size()>0){
				str_sql = "select s.cphid as cphm1, s.cplx as cplx1, s.sbsj as tgsj, s.tpid1 as tpid1, " +
						"s.tpid2 as tpid2, s.txtp as tpid3, s.txtp2 as tpid4, s.txtp3 as tpid5, s.jcdid as jcdid, s.cdid as cdid, " +
						"1 as tpzs, rownum rn from " + list.get(0) ;								
				if (cphid!= null &&!"".equals(cphid.trim())) {	
					String strhphm[] = cphid.split(",");
					if("01".equals(bussiness.trim())){//��ʶ�𳵹켣��ѯ�ӿ�
						if(strhphm.length==1){
							str_sql = str_sql + " and s.cphid = '" + cphid + "'";
						}else{
							str_sql = str_sql + " and s.cphid in ("+InterUtil.getStr(strhphm)+")";
						}
					}else if("04".equals(bussiness.trim())){
						//����ģ����ѯ��ֻ��һ�����ƺ�,����*���������ַ������������ַ�;��Oracle���ݿ�����Ҫת����%��_
						str_sql = str_sql + " and s.cphid like '"+cphid.replace("*", "%").replace("?", "_")+"'";
					}else if("05".equals(bussiness.trim())){//ǰ׺��ѯ
						if(cphid.startsWith("!")){//��ǰ׺��ѯ
							str_sql = str_sql + " and s.cphid not like '" + (cphid.length() > 1? cphid.substring(1):"") + "%'";
						}else{
							if(cphid.subSequence(0, 1).equals(qz1)){
								str_sql = str_sql + " and s.cphid like '" + cphid + "%' and s.cphid not like '"+ qz2 + "%'";
							}else{
								str_sql = str_sql + " and s.cphid like '" + cphid + "%'";
							}
						}
					}
				}
				
				//��������
				if (cplx!= null&&(!cplx.equals("-"))&&(!"".equals(cplx.trim()))) {					   
					String strcplx[] = cplx.split(",");
					if (strcplx.length == 1) {
						str_sql = str_sql+ " and s.cplx = '" + cplx + "'";
					} else {// �����������
						str_sql = str_sql+ " and s.cplx in (" +InterUtil.getStr(strcplx)+")";
					}
				}
				
				//ͼƬid����
				if (gcxh != null && !"".equals(gcxh.trim())) {
					String strgcxh[] = gcxh.split(",");
					if (strgcxh.length == 1) {
						str_sql = str_sql + " and s.tpid1 = '" + gcxh + "'";
					} else {// ���ͼƬid
						str_sql = str_sql + " and s.tpid1 in ("+InterUtil.getStr(strgcxh)+")";
					}
				}
				
				//����
				if (jcdid!= null && !"".equals(jcdid.trim())) {		
					String strjcdid[] = jcdid.split(",");//�������
					if (strjcdid.length == 1) {
						str_sql = str_sql + " and s.jcdid = '" + jcdid + "'";
					} else {
						str_sql = str_sql + " and (s.jcdid in ('00000000')";
						
						String[] aa = new String[500];
						int j = 0;
						for(int k = 0;k < strjcdid.length;k++){
							aa[j] = strjcdid[k];
							if((j+1) == 500){
								str_sql = str_sql + " or s.jcdid in (" + InterUtil.getStr(aa) + ")";
								aa = new String[500];
								j = 0;
							}else{
								j++;
							}
							
							if(j > 0 && k == strjcdid.length - 1){
								aa = Arrays.copyOf(aa, j);
								str_sql = str_sql + " or s.jcdid in (" + InterUtil.getStr(aa) + ")";
							}
						}
						str_sql = str_sql + " )";
					}
				}
				
//				//�ų�һ����������������
//				if(hmdCphm != null && !"".equals(hmdCphm.trim())){
//					String strhphm[] = hmdCphm.split(",");
//					if (strhphm.length == 1) {
//						str_sql = str_sql+ " and s.cphid != '" + hmdCphm + "'";
//					} else {// ������ƺ�
//						String[] aa = new String[500];
//						int j = 0;
//						for(int k = 0;k < strhphm.length;k++){
//							aa[j] = strhphm[k];
//							if((j+1) == 500){
//								str_sql = str_sql + " and s.cphid not in (" + InterUtil.getStr(aa) + ")";
//								aa = new String[500];
//								j = 0;
//							}else{
//								j++;
//							}
//							
//							if(j > 0 && k == strhphm.length - 1){
//								aa = Arrays.copyOf(aa, j);
//								str_sql = str_sql + " and s.cphid not in (" + InterUtil.getStr(aa) + ")";
//							}
//						}
//					}
//				}
				
				//ʱ��
				str_sql+=" and s.sbsj between to_date('"+ sdf.format(tm1)+"','yyyy-MM-dd HH24:mi:ss') and to_date('"+sdf.format(tm2)+"','yyyy-MM-dd HH24:mi:ss') ";
			}
			
			for (int i = 1; i < list.size(); i++) {					
				str_sql += " union (select s.cphid as cphm1, s.cplx as cplx1, s.sbsj as tgsj, s.tpid1 as tpid1, " +
						"s.tpid2 as tpid2, s.txtp as tpid3, s.txtp2 as tpid4, s.txtp3 as tpid5, s.jcdid as jcdid, s.cdid as cdid, " +
						"1 as tpzs, rownum rn from " + list.get(i) ;								
				if (cphid!= null &&!"".equals(cphid.trim())) {	
					String strhphm[] = cphid.split(",");
					if("01".equals(bussiness.trim())){//��ʶ�𳵹켣��ѯ�ӿ�
						if(strhphm.length==1){
							str_sql = str_sql + " and s.cphid = '" + cphid + "'";
						}else{
							str_sql = str_sql + " and s.cphid in ("+InterUtil.getStr(strhphm)+")";
						}
					}else if("04".equals(bussiness.trim())){
						//����ģ����ѯ��ֻ��һ�����ƺ�,����*���������ַ������������ַ�;��Oracle���ݿ�����Ҫת����%��_
						str_sql = str_sql+" and s.cphid like '"+cphid.replace("*", "%").replace("?", "_")+"'";
					}else if("05".equals(bussiness.trim())){//ǰ׺��ѯ
						if(cphid.startsWith("!")){//��ǰ׺��ѯ
							str_sql = str_sql + " and s.cphid not like '" + (cphid.length() > 1? cphid.substring(1):"") + "%'";
						}else{
							if(cphid.subSequence(0, 1).equals(qz1)){
								str_sql = str_sql + " and s.cphid like '" + cphid + "%' and s.cphid not like '"+ qz2 + "%'";
							}else{
								str_sql = str_sql + " and s.cphid like '" + cphid + "%'";
							}
						}
					}
				}
				
				//��������
				if (cplx!= null&&(!cplx.equals("-"))&&(!"".equals(cplx.trim()))) {					   
					String strcplx[] = cplx.split(",");
					if (strcplx.length == 1) {
						str_sql = str_sql + " and s.cplx = '" + cplx + "'";
					} else {// �����������
						str_sql = str_sql + " and s.cplx in (" +InterUtil.getStr(strcplx)+")";
					}
				}
				
				//ͼƬid����
				if (gcxh != null && !"".equals(gcxh.trim())) {
					String strgcxh[] = gcxh.split(",");
					if (strgcxh.length == 1) {
						str_sql = str_sql + " and s.tpid1 = '" + gcxh + "'";
					} else {// ���ͼƬid
						str_sql = str_sql + " and s.tpid1 in ("+InterUtil.getStr(strgcxh)+")";
					}
				}
				
				//����
				if (jcdid!= null && !"".equals(jcdid.trim())) {		
					String strjcdid[] = jcdid.split(",");// �������
					if (strjcdid.length == 1) {
						str_sql = str_sql + " and s.jcdid = '" + jcdid + "'";
					} else {
						str_sql = str_sql + " and (s.jcdid in ('00000000')";
						
						String[] aa = new String[500];
						int j = 0;
						for(int k = 0;k < strjcdid.length;k++){
							aa[j] = strjcdid[k];
							if((j+1) == 500){
								str_sql = str_sql + " or s.jcdid in (" + InterUtil.getStr(aa) + ")";
								aa = new String[500];
								j = 0;
							}else{
								j++;
							}
							
							if(j > 0 && k == strjcdid.length - 1){
								aa = Arrays.copyOf(aa, j);
								str_sql = str_sql + " or s.jcdid in (" + InterUtil.getStr(aa) + ")";
							}
						}
						str_sql = str_sql + " )";
					}
				}
				
//				//�ų�һ����������������
//				if(hmdCphm != null && !"".equals(hmdCphm.trim())){
//					String strhphm[] = hmdCphm.split(",");
//					if (strhphm.length == 1) {
//						str_sql = str_sql+ " and s.cphid != '" + hmdCphm + "'";
//					} else {// ������ƺ�
//						String[] aa = new String[500];
//						int j = 0;
//						for(int k = 0;k < strhphm.length;k++){
//							aa[j] = strhphm[k];
//							if((j+1) == 500){
//								str_sql = str_sql + " and s.cphid not in (" + InterUtil.getStr(aa) + ")";
//								aa = new String[500];
//								j = 0;
//							}else{
//								j++;
//							}
//							
//							if(j > 0 && k == strhphm.length - 1){
//								aa = Arrays.copyOf(aa, j);
//								str_sql = str_sql + " and s.cphid not in (" + InterUtil.getStr(aa) + ")";
//							}
//						}
//					}
//				}
				
				//ʱ��
				str_sql+=" and s.sbsj between to_date('"+ sdf.format(tm1)+"','yyyy-MM-dd HH24:mi:ss') and to_date('"+sdf.format(tm2)+"','yyyy-MM-dd HH24:mi:ss') )";
			}
//			str_sql+=" order by sbsj desc";
			if("1".equals(flag)){//��ѯ��¼����
				str_sql = "select count(1) as count from ("+str_sql+" )";
			}
		}else{
			throw new NullPointerException("no result!");
		}
		
		return str_sql;
	}
}