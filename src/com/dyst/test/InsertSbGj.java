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
 * 主要实现功能：
 * 1.根据车牌号和事件段，查询ES数据库轨迹信息；
 *   1.1车牌号以文本形式提供，一个车牌占用一行。
 * 2.将结果插入到oracle数据库中；
 * 3.采用县城池的方式实现；
 * 4.每次同时查询轨迹车牌数可修改；
 * 5.车牌文本数据路径可修改；
 * 
 * 查询ES的估计信息并插入数据库中
 * 
 * @author likui
 */
public class InsertSbGj {

	public static void main(String[] args) {
		try {
			// for(int i=6;i<13;i++){//月
			// for(int j=0;j<31;j++){//日
			// for(int k=0;k<24;k++){//小时
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
			//获取车牌号码---begin----------------
			Connection connection = null;
			Statement st = null;
			DBConnectionManager dbCon = DBConnectionManager.getInstance();
			connection = dbCon.getConnection("db");// 获取数据库连接
			ResultSet rs = null;
			String cphm="";
			try {
				st = connection.createStatement();// 操作数据库
				rs = st.executeQuery("select cphm from  its.cphm_tmp2");
				while(rs.next()){
					cphm += rs.getString("cphm")+",";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
//			QueryESInSertTxt(cphm, "", "", "2014-02-20 00:00:00", "2014-05-26 00:00:00", "D://轨迹信息.csv");
//			从es数据库读取轨迹信息写入到oracle数据表中
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
		Client client = ecclient.getConnection("es");// ES数据库连接池,获取数据连接
		
		DBConnectionManager dbCon = DBConnectionManager.getInstance();
		connection = dbCon.getConnection("db");// 获取数据库连接
		 
		try {
			System.out.println(hphm);
			QueryBuilder query = getQueryByCon(hphm, "", "", beginTime, endTime); 
//				ESutil.getQueryBuilderByCon(beginTime,
//					endTime, null, hphm, null, null, null, null, null, null, null, "01");// 获取查询Query
			
			//先查询记录总数
			SearchResponse response1 = client.prepareSearch("sb").setTypes("sb")
			 .setQuery(query)
			 .setSearchType(SearchType.COUNT)
			 .setExplain(false)
		     .execute().actionGet();
			 Long count= (Long)response1.getHits().getTotalHits();//符合条件记录总数
						
			 SearchResponse response = client.prepareSearch("sb").setTypes("sb")
					.setQuery(query).setFrom(0).setSize(count.intValue())
					.setExplain(false)
//					.addFields(new String[] { "cphm1", "jcdid", "cplx1", "tpid1","tgsj", "cdid" })
					.execute().actionGet();
			System.out.println("-总记录数:" + response.getHits().getHits().length);
			SearchHits hits = response.getHits();
			String sql = "";
			
			st = connection.createStatement();// 操作数据库
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
				System.out.println("正在插入....."+hits.getAt(i).getSource().get("cphm1"));
				try {
					st.execute(sql); // 插入布控请求表记录
				} catch (Exception e) {//单条出现异常时不要影响其他的记录，tpid1要唯一
					e.printStackTrace();
					continue;
				}
			}
			System.out.println("插入数据库完成");
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
			if (client != null) {//回收连接
				ecclient.freeConnection("es", client);
			}
		}
	}
	public static BoolQueryBuilder getQueryByCon(String cphid,String jcdid,String cplx,String begintime,String endtime){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			BoolQueryBuilder query = boolQuery();
			// 车牌号id
			if (cphid != null && !"".equals(cphid)) {
				query.must(termsQuery ("cphm1", cphid.split(",")));
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
		Client client = ecclient.getConnection("es");// ES数据库连接池,获取数据连接
		
		File file = new File(filePath);// 文件路径
		FileWriter fileWriter = null;
		BufferedWriter  bw = null;
		try {
			fileWriter = new FileWriter(file, true);
			System.out.println("存放路径"+filePath);
			
			QueryBuilder query = getQueryByCon(hphm, jcdid, cplx, beginTime, endTime);// 获取查询Query
			
			//先查询记录总数
			SearchResponse response1 = client.prepareSearch("sb").setTypes("sb")
									  .setQuery(query)
									  .setSearchType(SearchType.COUNT)
									  .setExplain(false)
								      .execute().actionGet();
			 Long count= (Long)response1.getHits().getTotalHits();//符合条件记录总数
				System.out.println("总记录数："+count);	
				int bulkcount = 10000;
				for(int j=0;j<count.intValue();j+=bulkcount){//每次查询输出一万条，
					
					 SearchResponse response = client.prepareSearch("sb").setTypes("sb")
						.setQuery(query).setFrom(j).setSize(bulkcount)
//						.setSearchType(SearchType.)
//						.addSort("tgsj", SortOrder.DESC)//排序
						.setExplain(false)
						.execute().actionGet();
				 
					System.out.println("-总记录数:" + response.getHits().getHits().length);
					SearchHits hits = response.getHits();
					
					
					bw = new BufferedWriter(fileWriter);
					for (int i = 0; i < hits.getHits().length; i++) {
//						String tgsj = sdf.format(new Date((Long) hits.getAt(i)
//								.getSource().get("tgsj")));
	//					.field("cphm1", arr[0])//车牌号id1
	//					.field("cphm2", arr[1])//车牌号id2
	//					.field("cplx1", arr[2])//车牌类型1
	//					.field("cplx2", arr[3])//车牌类型2
	//					.field("tgsj", sbsj)//识别时间
	//					.field("scsj", sbsj)//上传时间
	//					.field("jcdid", arr[7])//监测点id
	//					.field("jcklx", arr[8])//识别时间
	//					.field("cdid", arr[9])//车道id
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
								
						//读取内容写文本文件
						bw.write(new String(conent.getBytes("utf-8"), "UTF-8"));
//						bw.write(conent);
						bw.newLine();
						
						
						System.out.println("正在插入....."+(i+j)+":.."+hits.getAt(i).getSource().get("cphm1"));
					
				}
					
			}
				fileWriter.flush();
				bw.close();
				fileWriter.close();
			System.out.println("插入数据库完成");
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
			if (client != null) {//回收连接
				ecclient.freeConnection("es", client);
			}
		}
	}
}
