package com.dyst.test;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

import com.dyst.elasticsearch.util.ESClientManager;
import com.dyst.elasticsearch.util.ESutil;
import com.dyst.oracle.DBConnectionManager;
import com.dyst.util.InterUtil;

/**
 * ��Ҫʵ�ֹ��ܣ�
 * 1.���ݳ��ƺź��¼��Σ���ѯES���ݿ�켣��Ϣ��
 *   1.1���ƺ����ı���ʽ�ṩ��һ������ռ��һ�С�
 * 2.��������뵽oracle���ݿ��У�
 * 3.�����سǳصķ�ʽʵ�֣�
 * 4.ÿ��ͬʱ��ѯ�켣���������޸ģ�
 * 5.�����ı�����·�����޸ģ�
 * 
 * ��ѯES�Ĺ�����Ϣ���������ݿ���
 * 
 * @author likui
 */
public class InsertSbGj {

	public static void main(String[] args) {
		try {
			// for(int i=6;i<13;i++){//��
			// for(int j=0;j<31;j++){//��
			// for(int k=0;k<24;k++){//Сʱ
			// QueryESInSertTxt(null, "2014-02-01 00:00:00",
			// "2014-02-01 01:00:00", "D://export.txt");
			// }
			// }
			// }
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdfexp = new SimpleDateFormat("yyyyMMdd");
//			Date currDate = sdf.parse("2014-02-17 00:00:00");
//			for (int i=138;i <145; i++) {
//				// System.out.println(InterUtil.getTimeGjcx(i+1, currDate));
//				// InterUtil.getTimeGjcx(i, currDate);
//				QueryESInSertTxt(null, InterUtil.getTimeGjcx(i + 1, currDate),
//						InterUtil.getTimeGjcx(i, currDate), "/exportData/"
//								+ "ysb_" +sdfexp.format(sdf.parse(InterUtil.getTimeGjcx(i+1, currDate))) + ".txt");
//			}
			//��ȡ���ƺ���---begin----------------
			Connection connection = null;
			Statement st = null;
			DBConnectionManager dbCon = DBConnectionManager.getInstance();
			connection = dbCon.getConnection("db");// ��ȡ���ݿ�����
			ResultSet rs = null;
			String cphm="";
			try {
				st = connection.createStatement();// �������ݿ�
				rs = st.executeQuery("select cphm from  its.cphm_tmp2");
				while(rs.next()){
					cphm += rs.getString("cphm")+",";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
//			QueryESInSertTxt(cphm, "", "", "2014-02-20 00:00:00", "2014-05-26 00:00:00", "D://�켣��Ϣ.csv");
//			��es���ݿ��ȡ�켣��Ϣд�뵽oracle���ݱ���
			QueryESInSertOrtacle(cphm, "2014-05-15 00:00:00", "2014-05-26 00:00:00");
		} catch (Exception e) {
			
		} finally {

		}
	}
	
	public static void QueryESInSertOrtacle(String hphm, String beginTime,
			String endTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TransportClient clientTrans = null;
		ESClientManager ecclient = ESClientManager.getInstance();
		Connection connection = null;
		Statement st = null;
		Client client = ecclient.getConnection("es");// ES���ݿ����ӳ�,��ȡ��������
		
		DBConnectionManager dbCon = DBConnectionManager.getInstance();
		connection = dbCon.getConnection("db");// ��ȡ���ݿ�����
		 
		try {
			System.out.println(hphm);
			QueryBuilder query = getQueryByCon(hphm, "", "", beginTime, endTime); 
//				ESutil.getQueryBuilderByCon(beginTime,
//					endTime, null, hphm, null, null, null, null, null, null, null, "01");// ��ȡ��ѯQuery
			
			//�Ȳ�ѯ��¼����
			SearchResponse response1 = client.prepareSearch("sb").setTypes("sb")
			 .setQuery(query)
			 .setSearchType(SearchType.COUNT)
			 .setExplain(false)
		     .execute().actionGet();
			 Long count= (Long)response1.getHits().getTotalHits();//����������¼����
						
			 SearchResponse response = client.prepareSearch("sb").setTypes("sb")
					.setQuery(query).setFrom(0).setSize(count.intValue())
					.setExplain(false)
//					.addFields(new String[] { "cphm1", "jcdid", "cplx1", "tpid1","tgsj", "cdid" })
					.execute().actionGet();
			System.out.println("-�ܼ�¼��:" + response.getHits().getHits().length);
			SearchHits hits = response.getHits();
			String sql = "";
			
			st = connection.createStatement();// �������ݿ�
			for (int i = 0; i < hits.getHits().length; i++) {
				String tgsj = sdf.format(new Date((Long) hits.getAt(i)
						.getSource().get("tgsj")));
				sql = " insert into SBTEST (cphid,tpid1,cplx,sbsj,jcdid)" + " values ('"
						+ hits.getAt(i).getSource().get("cphm1") + "','"
						+ hits.getAt(i).getSource().get("tpid1") + "','"
						+ hits.getAt(i).getSource().get("cplx1") + "'"
						+ ",to_date('" + tgsj + "','yyyy-MM-dd HH24:mi:ss'),'"
						+ hits.getAt(i).getSource().get("jcdid") + "'"
						+ ")";
				System.out.println("���ڲ���....."+hits.getAt(i).getSource().get("cphm1"));
				try {
					st.execute(sql); // ���벼��������¼
				} catch (Exception e) {//���������쳣ʱ��ҪӰ�������ļ�¼��tpid1ҪΨһ
					e.printStackTrace();
					continue;
				}
			}
			System.out.println("�������ݿ����");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (clientTrans != null) {
				clientTrans.close();
			}
			if(st!=null){
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(connection!=null){
				dbCon.freeConnection("db", connection);
//				try {
//					connection.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
			}
			if (client != null) {//��������
				ecclient.freeConnection("es", client);
			}
		}
	}
	public static BoolQueryBuilder getQueryByCon(String cphid,String jcdid,String cplx,String begintime,String endtime){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			BoolQueryBuilder query = boolQuery();
			// ���ƺ�id
			if (cphid != null && !"".equals(cphid)) {
				query.must(termsQuery ("cphm1", cphid.split(",")));
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
							.includeUpper(true))
							;
					} catch (ParseException e) {
						e.printStackTrace();
					}
			}
			return query;
		}

	/**
	 * 
	 * @param hphm
	 * @param cplx
	 * @param jcdid
	 * @param beginTime
	 * @param endTime
	 * @param filePath
	 */
	public static void QueryESInSertTxt(String hphm,String cplx ,String jcdid, String beginTime,String endTime,String filePath) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TransportClient clientTrans = null;
		ESClientManager ecclient = ESClientManager.getInstance();
//		Connection connection = null;
		Client client = ecclient.getConnection("es");// ES���ݿ����ӳ�,��ȡ��������
		
		File file = new File(filePath);// �ļ�·��
		FileWriter fileWriter = null;
		BufferedWriter  bw = null;
		try {
			fileWriter = new FileWriter(file, true);
			System.out.println("���·��"+filePath);
			
			QueryBuilder query = getQueryByCon(hphm, jcdid, cplx, beginTime, endTime);// ��ȡ��ѯQuery
			
			//�Ȳ�ѯ��¼����
			SearchResponse response1 = client.prepareSearch("sb").setTypes("sb")
									  .setQuery(query)
									  .setSearchType(SearchType.COUNT)
									  .setExplain(false)
								      .execute().actionGet();
			 Long count= (Long)response1.getHits().getTotalHits();//����������¼����
				System.out.println("�ܼ�¼����"+count);	
				int bulkcount = 10000;
				for(int j=0;j<count.intValue();j+=bulkcount){//ÿ�β�ѯ���һ������
					
					 SearchResponse response = client.prepareSearch("sb").setTypes("sb")
						.setQuery(query).setFrom(j).setSize(bulkcount)
//						.setSearchType(SearchType.)
//						.addSort("tgsj", SortOrder.DESC)//����
						.setExplain(false)
						.execute().actionGet();
				 
					System.out.println("-�ܼ�¼��:" + response.getHits().getHits().length);
					SearchHits hits = response.getHits();
					
					
					bw = new BufferedWriter(fileWriter);
					for (int i = 0; i < hits.getHits().length; i++) {
//						String tgsj = sdf.format(new Date((Long) hits.getAt(i)
//								.getSource().get("tgsj")));
	//					.field("cphm1", arr[0])//���ƺ�id1
	//					.field("cphm2", arr[1])//���ƺ�id2
	//					.field("cplx1", arr[2])//��������1
	//					.field("cplx2", arr[3])//��������2
	//					.field("tgsj", sbsj)//ʶ��ʱ��
	//					.field("scsj", sbsj)//�ϴ�ʱ��
	//					.field("jcdid", arr[7])//����id
	//					.field("jcklx", arr[8])//ʶ��ʱ��
	//					.field("cdid", arr[9])//����id
	//					.field("csys",arr[10])//
	//					.field("cllx",arr[11])//
	//					.field("cb",arr[12])//
	//					.field("xxkbm",arr[13])//
	//					.field("wflx",arr[14])//
	//					.field("sd",arr[15])//
	//					.field("qdid",arr[16])//
	//					.field("xsfx",arr[17])//
	//					.field("tpzs",arr[18])//
	//					.field("tpid1",arr[19])//
	//					.field("tpid2",arr[20])//
	//					.field("tpid3",arr[21])//
	//					.field("tpid4",arr[22])//
	//					.field("tpid5",arr[23])//
	//					.field("qpsfwc",arr[24])//
	//					.field("hpsfwc",arr[25])//
	//					.field("qhsfyz",arr[26])///
	//					.field("zxd",arr[27])//
	//					.field("fbcd",arr[28])//
	//					.field("bcbz",arr[29])//
	//					.field("fqh",arr[30])//
	//					.field("spurl",arr[31])//
	//					.field("bl",arr[32])//
	//					.field("byzd1",arr[33])//
	//					.field("byzd2",arr[34])//
	//					.field("byzd3",arr[35])//
						String conent =  hits.getAt(i).getSource().get("cphm1") + ","
//						+hits.getAt(i).getSource().get("cphm2") + ","
//						+hits.getAt(i).getSource().get("cplx1") + ","
//						+hits.getAt(i).getSource().get("cplx2") + ","
//						 + ","
						+sdf.format(new Date((Long) hits.getAt(i).getSource().get("tgsj"))) + ","
//						+sdf.format(new Date((Long) hits.getAt(i).getSource().get("scsj"))) + ","
						+hits.getAt(i).getSource().get("jcdid") 
						+ ","
//						+hits.getAt(i).getSource().get("jcklx") + ","
//						+hits.getAt(i).getSource().get("cdid") + ","
//						+hits.getAt(i).getSource().get("csys") + ","
//						+hits.getAt(i).getSource().get("cllx") + ","
//						+hits.getAt(i).getSource().get("cb") + ","
//						+hits.getAt(i).getSource().get("xxkbm") + ","
//						+hits.getAt(i).getSource().get("wflx") + ","
//						+hits.getAt(i).getSource().get("sd") + ","
//						+hits.getAt(i).getSource().get("qdid") + ","
//						+(hits.getAt(i).getSource().get("xsfx").equals(null)?"":hits.getAt(i).getSource().get("xsfx"))+ ","
//						+hits.getAt(i).getSource().get("tpzs") + ","
						+hits.getAt(i).getSource().get("tpid1") +"   "
//						+ ","
//						+hits.getAt(i).getSource().get("tpid2") + ","
//						+hits.getAt(i).getSource().get("tpid3") + ","
//						+hits.getAt(i).getSource().get("tpid4") + ","
//						+hits.getAt(i).getSource().get("tpid5") + ","
//						+hits.getAt(i).getSource().get("qpsfwc") + ","
//						+hits.getAt(i).getSource().get("hpsfwc") + ","
//						+hits.getAt(i).getSource().get("qhsfyz") + ","
//						+hits.getAt(i).getSource().get("zxd") + ","
//						+hits.getAt(i).getSource().get("fbcd") + ","
//						+hits.getAt(i).getSource().get("bcbz") + ","
//						+hits.getAt(i).getSource().get("fqh") + ","
//						+hits.getAt(i).getSource().get("spurl") + ","
//						+hits.getAt(i).getSource().get("bl") + ","
//						+hits.getAt(i).getSource().get("byzd1") + ","
//						+hits.getAt(i).getSource().get("byzd2") + ","
//						+hits.getAt(i).getSource().get("byzd3") 
						;
								
						//��ȡ����д�ı��ļ�
						bw.write(new String(conent.getBytes("utf-8"), "UTF-8"));
//						bw.write(conent);
						bw.newLine();
						
						
						System.out.println("���ڲ���....."+(i+j)+":.."+hits.getAt(i).getSource().get("cphm1"));
					
				}
					
			}
				fileWriter.flush();
				bw.close();
				fileWriter.close();
			System.out.println("�������ݿ����");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				fileWriter.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (clientTrans != null) {
				clientTrans.close();
			}
			if (client != null) {//��������
				ecclient.freeConnection("es", client);
			}
		}
	}
}
