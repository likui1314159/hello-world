package com.dyst.test;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.*;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

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
public class CopyOfInsertTxtByCon {

	public static void main(String[] args) {
		try {
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
			long startTime = System.currentTimeMillis();
			String cphm="";
			//多次违章车辆轨迹
//			QueryESInSertTxt("D:\\cphm.txt", cphm, "", "", "2014-05-27 17:00:00", "2014-05-28 17:00:00", "D://多次违章车辆轨迹20140528//");
			
			//海关缉查车辆轨迹
			QueryESInSertTxt("D:\\cphm2.txt", "", "", "30606705", "2014-06-29 20:10:00", "2014-06-29 20:20:00", "D://车辆轨迹30606705//");

			//			从es数据库读取轨迹信息写入到oracle数据表中
//			QueryESInSertOrtacle(cphm, "2014-02-20 00:00:00", "2014-05-26 00:00:00");
			System.out.println("导出记录及图片耗时："+(System.currentTimeMillis()-startTime)/1000d+"秒");
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
			
			QueryBuilder query = ESutil.getQueryBuilderByCon(beginTime,
					endTime, null, hphm, null, null, null, null, null, null, null, "01");// 获取查询Query
			
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
	
	/**
	 * 查询ES数据，导入到csv文件中
	 * @param hphm
	 * @param cplx
	 * @param jcdid
	 * @param beginTime
	 * @param endTime
	 * @param filePath
	 */
	public static void QueryESInSertTxt(String txtFile, String hphm,String cplx ,String jcdid,
			String beginTime,String endTime,String fileFolder) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TransportClient clientTrans = null;
		ESClientManager ecclient = ESClientManager.getInstance();
//		Connection connection = null;
		Client client = ecclient.getConnection("es");// ES数据库连接池,获取数据连接
		
		
	////webservice 调用地址client
		org.codehaus.xfire.client.Client webClient = null;
    	String ip="http://100.100.37.37:8989/dyst/services/InAccess?wsdl";//图片接口服务
			try {
				webClient = new org.codehaus.xfire.client.Client(new URL(ip));
			}catch (Exception e) {
				e.printStackTrace();
			}
			
		//需要翻译监测点名称时使用---begin----------------
		Connection connection = null;
		Statement st = null;
		DBConnectionManager dbCon = DBConnectionManager.getInstance();
		connection = dbCon.getConnection("db");// 获取数据库连接
		ResultSet rs = null;
		Map<String, String> jcdMap = new HashMap<String,String>();
		try {
			st = connection.createStatement();// 操作数据库
			rs = st.executeQuery("select id,jcdmc from jcd");
			while(rs.next()){
				jcdMap.put(rs.getString("id"), rs.getString("jcdmc"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//		---end----------------
		
		File file = null;//new File(filePath);// 文件路径
		FileWriter fileWriter = null;
		BufferedWriter  bw = null;
		BufferedReader bf = null;
		String str;
		try {
//			//读取txt文件的车牌号，每一个车牌号查询一次轨迹，写入指定的文件夹中
//			bf = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), "GBK"));
//			while ((str = bf.readLine()) != null) {//每个车牌号查询一次
				//生成csv格式文件路径，如果没有，则创建
				file = new File(fileFolder+File.separator+jcdid+".txt");
				if(!file.exists()){
					file.getParentFile().mkdirs();
				}
				fileWriter = new FileWriter(file, true);
				bw = new BufferedWriter(fileWriter);//创建写文件write
//				System.out.println("存放路径"+filePath);
				
				//----------------------------------------ES开始-----------------------
			    //生成查询query条件
				QueryBuilder query = getQueryByCon("", jcdid, cplx, beginTime, endTime);// 获取查询Query
				
				//先查询记录总数
				SearchResponse response1 = client.prepareSearch("sb").setTypes("sb")
										  .setQuery(query)
										  .setSearchType(SearchType.COUNT)
										  .setExplain(false)
									      .execute().actionGet();
				Long count = (Long)response1.getHits().getTotalHits();//符合条件记录总数
				System.out.println("总记录数："+count);	
				int bulkcount = 1000;
				int k = 0;
				for(int j=0;j < count.intValue();j+=bulkcount){//每次查询输出一万条，
					SearchResponse response = client.prepareSearch("sb").setTypes("sb")
							.setQuery(query).setFrom(0).setSize(bulkcount)
							.addSort("tgsj", SortOrder.DESC)//排序
							.setExplain(false)
							.execute().actionGet();
					 
					System.out.println("-总记录数:" + response.getHits().getHits().length);
					SearchHits hits = response.getHits();
						
					for (int i = 0; i < hits.getHits().length; i++) {
						String tpid = (String) hits.getAt(i).getSource().get("tpid1");
						
						//特写图片
						getPic(fileFolder+File.separator+tpid+".jpg",tpid,webClient);
						//全景图片
						String tpid2 = tpid.substring(0,tpid.length()-1)+ "2";
						getPic(fileFolder+File.separator+"全景"+File.separator+tpid2+".jpg",tpid2,webClient);
						
						String conent = hits.getAt(i).getSource().get("cphm1") + ","
							+ sdf.format(new Date((Long) hits.getAt(i).getSource().get("tgsj"))) + ","
							+ hits.getAt(i).getSource().get("jcdid") + ","
							+ jcdMap.get(hits.getAt(i).getSource().get("jcdid")) 
							+ "," + "#"//存在cvs文件后图片id成数字了，前加#格式化成字符串
							+hits.getAt(i).getSource().get("tpid1");
									
						//读取内容写文本文件
						bw.write(new String(conent.getBytes("GBK"), "GBK"));
//						bw.write(conent);
						bw.newLine();
						fileWriter.flush();
						System.out.println("ES正在插入....."+(i+j)+":.."+hits.getAt(i).getSource().get("cphm1"));
					}
//				}
			}
				fileWriter.flush();
				if(bw!=null){
					bw.close();
				}
					
				fileWriter.close();
				System.out.println("插入数据库完成");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				fileWriter.close();
			} catch (IOException e1) {
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
	/**
	 * 根据图片id和文件路径，把图片写入指定的路径中
	 * @param path 图片文件路径
	 * @param tpid 图片id
	 * @return 写入成功返回true,失败返回false
	 */
	public static  boolean getPic(String path, String tpid, org.codehaus.xfire.client.Client client ){
		
    	String str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
    		+ "<head></head><body>" +
    				"<tpid>"+tpid+"</tpid>" +
    				"</body></root>";
			try {
				Object[] s = client.invoke("executes", new String[] {"01", "02", "hello,world","1" ,str_xml} );
				String tpPath = (String)s[0];
				//截取图片URL路径
				tpPath = tpPath.substring(tpPath.indexOf("http://"), tpPath.indexOf("jpg")+3);
//				System.out.println(s[0]);
				if(exists(tpPath)){
					downloadFile(path, tpPath);
					return true;
				}
				return false;
			} catch (MalformedURLException e) {
				return false;
			} catch (Exception e) {
				return false;
			}
	} 
	/**
	 * 查看某一个图片连接是否存在
	 * @param pName
	 * @return
	 */
	public static boolean exists(String pName){
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(pName).openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * 根据图片id和文件路径，把图片写入指定的路径中
	 * @param path 图片文件路径
	 * @param tpid 图片id
	 * @return 写入成功返回true,失败返回false
	 */
	public static  boolean getPic(String path, String tpid ){
		
    	String str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
    		+ "<head></head><body>" +
    				"<tpid>"+tpid+"</tpid>" +
    				"</body></root>";
    	org.codehaus.xfire.client.Client client = null;
    	String ip=""; 
			ip="http://100.100.37.37:8989/dyst/services/InAccess?wsdl";//图片接口服务
			try {
				client = new org.codehaus.xfire.client.Client(new URL(ip));
				Object[] s = client.invoke("executes", new String[] {"01", "02", "hello,world","1" ,str_xml} );
				String tpPath = (String)s[0];
				//截取图片URL路径
				tpPath = tpPath.substring(tpPath.indexOf("http://"), tpPath.indexOf("jpg")+3);
				System.out.println(s[0]);
				downloadFile(path, tpPath);
				return true;
			} catch (MalformedURLException e) {
				return false;
			} catch (Exception e) {
				return false;
			}
	} 
	/**  
	* 将http地址的文件下载并保存到本地  
	*   
	* @param path  
	*            文件保存位置  
	* @param url  
	*            文件url地址  
	* @throws IOException  
	*/  
	public  static void downloadFile(String path, String urlStr) throws Exception {  
		URL url = null;
		HttpURLConnection con = null;
		BufferedImage input = null;
		BufferedOutputStream output = null;
		
		File pathDir = new File(path);
		if(!pathDir.getParentFile().exists()){//如果文件夹路径不存在，创建文件夹
			pathDir.getParentFile().mkdirs();
		}
		
		//读取图片流
		url = new URL(urlStr);
		con = (HttpURLConnection)url.openConnection();
		input = ImageIO.read(con.getInputStream());
		
		//写图片
		output = new BufferedOutputStream(new FileOutputStream(pathDir));
		ImageIO.write(input, "jpg", output);
		 
		output.flush();
		output.close();
		con.disconnect();
		
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

}
