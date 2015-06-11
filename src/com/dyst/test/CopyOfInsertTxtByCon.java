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
			//���Υ�³����켣
//			QueryESInSertTxt("D:\\cphm.txt", cphm, "", "", "2014-05-27 17:00:00", "2014-05-28 17:00:00", "D://���Υ�³����켣20140528//");
			
			//���ؼ��鳵���켣
			QueryESInSertTxt("D:\\cphm2.txt", "", "", "30606705", "2014-06-29 20:10:00", "2014-06-29 20:20:00", "D://�����켣30606705//");

			//			��es���ݿ��ȡ�켣��Ϣд�뵽oracle���ݱ���
//			QueryESInSertOrtacle(cphm, "2014-02-20 00:00:00", "2014-05-26 00:00:00");
			System.out.println("������¼��ͼƬ��ʱ��"+(System.currentTimeMillis()-startTime)/1000d+"��");
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
			
			QueryBuilder query = ESutil.getQueryBuilderByCon(beginTime,
					endTime, null, hphm, null, null, null, null, null, null, null, "01");// ��ȡ��ѯQuery
			
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
	
	/**
	 * ��ѯES���ݣ����뵽csv�ļ���
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
		Client client = ecclient.getConnection("es");// ES���ݿ����ӳ�,��ȡ��������
		
		
	////webservice ���õ�ַclient
		org.codehaus.xfire.client.Client webClient = null;
    	String ip="http://100.100.37.37:8989/dyst/services/InAccess?wsdl";//ͼƬ�ӿڷ���
			try {
				webClient = new org.codehaus.xfire.client.Client(new URL(ip));
			}catch (Exception e) {
				e.printStackTrace();
			}
			
		//��Ҫ�����������ʱʹ��---begin----------------
		Connection connection = null;
		Statement st = null;
		DBConnectionManager dbCon = DBConnectionManager.getInstance();
		connection = dbCon.getConnection("db");// ��ȡ���ݿ�����
		ResultSet rs = null;
		Map<String, String> jcdMap = new HashMap<String,String>();
		try {
			st = connection.createStatement();// �������ݿ�
			rs = st.executeQuery("select id,jcdmc from jcd");
			while(rs.next()){
				jcdMap.put(rs.getString("id"), rs.getString("jcdmc"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//		---end----------------
		
		File file = null;//new File(filePath);// �ļ�·��
		FileWriter fileWriter = null;
		BufferedWriter  bw = null;
		BufferedReader bf = null;
		String str;
		try {
//			//��ȡtxt�ļ��ĳ��ƺţ�ÿһ�����ƺŲ�ѯһ�ι켣��д��ָ�����ļ�����
//			bf = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), "GBK"));
//			while ((str = bf.readLine()) != null) {//ÿ�����ƺŲ�ѯһ��
				//����csv��ʽ�ļ�·�������û�У��򴴽�
				file = new File(fileFolder+File.separator+jcdid+".txt");
				if(!file.exists()){
					file.getParentFile().mkdirs();
				}
				fileWriter = new FileWriter(file, true);
				bw = new BufferedWriter(fileWriter);//����д�ļ�write
//				System.out.println("���·��"+filePath);
				
				//----------------------------------------ES��ʼ-----------------------
			    //���ɲ�ѯquery����
				QueryBuilder query = getQueryByCon("", jcdid, cplx, beginTime, endTime);// ��ȡ��ѯQuery
				
				//�Ȳ�ѯ��¼����
				SearchResponse response1 = client.prepareSearch("sb").setTypes("sb")
										  .setQuery(query)
										  .setSearchType(SearchType.COUNT)
										  .setExplain(false)
									      .execute().actionGet();
				Long count = (Long)response1.getHits().getTotalHits();//����������¼����
				System.out.println("�ܼ�¼����"+count);	
				int bulkcount = 1000;
				int k = 0;
				for(int j=0;j < count.intValue();j+=bulkcount){//ÿ�β�ѯ���һ������
					SearchResponse response = client.prepareSearch("sb").setTypes("sb")
							.setQuery(query).setFrom(0).setSize(bulkcount)
							.addSort("tgsj", SortOrder.DESC)//����
							.setExplain(false)
							.execute().actionGet();
					 
					System.out.println("-�ܼ�¼��:" + response.getHits().getHits().length);
					SearchHits hits = response.getHits();
						
					for (int i = 0; i < hits.getHits().length; i++) {
						String tpid = (String) hits.getAt(i).getSource().get("tpid1");
						
						//��дͼƬ
						getPic(fileFolder+File.separator+tpid+".jpg",tpid,webClient);
						//ȫ��ͼƬ
						String tpid2 = tpid.substring(0,tpid.length()-1)+ "2";
						getPic(fileFolder+File.separator+"ȫ��"+File.separator+tpid2+".jpg",tpid2,webClient);
						
						String conent = hits.getAt(i).getSource().get("cphm1") + ","
							+ sdf.format(new Date((Long) hits.getAt(i).getSource().get("tgsj"))) + ","
							+ hits.getAt(i).getSource().get("jcdid") + ","
							+ jcdMap.get(hits.getAt(i).getSource().get("jcdid")) 
							+ "," + "#"//����cvs�ļ���ͼƬid�������ˣ�ǰ��#��ʽ�����ַ���
							+hits.getAt(i).getSource().get("tpid1");
									
						//��ȡ����д�ı��ļ�
						bw.write(new String(conent.getBytes("GBK"), "GBK"));
//						bw.write(conent);
						bw.newLine();
						fileWriter.flush();
						System.out.println("ES���ڲ���....."+(i+j)+":.."+hits.getAt(i).getSource().get("cphm1"));
					}
//				}
			}
				fileWriter.flush();
				if(bw!=null){
					bw.close();
				}
					
				fileWriter.close();
				System.out.println("�������ݿ����");
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
			if (client != null) {//��������
				ecclient.freeConnection("es", client);
				
			}
		}
	}
	/**
	 * ����ͼƬid���ļ�·������ͼƬд��ָ����·����
	 * @param path ͼƬ�ļ�·��
	 * @param tpid ͼƬid
	 * @return д��ɹ�����true,ʧ�ܷ���false
	 */
	public static  boolean getPic(String path, String tpid, org.codehaus.xfire.client.Client client ){
		
    	String str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
    		+ "<head></head><body>" +
    				"<tpid>"+tpid+"</tpid>" +
    				"</body></root>";
			try {
				Object[] s = client.invoke("executes", new String[] {"01", "02", "hello,world","1" ,str_xml} );
				String tpPath = (String)s[0];
				//��ȡͼƬURL·��
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
	 * �鿴ĳһ��ͼƬ�����Ƿ����
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
	 * ����ͼƬid���ļ�·������ͼƬд��ָ����·����
	 * @param path ͼƬ�ļ�·��
	 * @param tpid ͼƬid
	 * @return д��ɹ�����true,ʧ�ܷ���false
	 */
	public static  boolean getPic(String path, String tpid ){
		
    	String str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
    		+ "<head></head><body>" +
    				"<tpid>"+tpid+"</tpid>" +
    				"</body></root>";
    	org.codehaus.xfire.client.Client client = null;
    	String ip=""; 
			ip="http://100.100.37.37:8989/dyst/services/InAccess?wsdl";//ͼƬ�ӿڷ���
			try {
				client = new org.codehaus.xfire.client.Client(new URL(ip));
				Object[] s = client.invoke("executes", new String[] {"01", "02", "hello,world","1" ,str_xml} );
				String tpPath = (String)s[0];
				//��ȡͼƬURL·��
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
	* ��http��ַ���ļ����ز����浽����  
	*   
	* @param path  
	*            �ļ�����λ��  
	* @param url  
	*            �ļ�url��ַ  
	* @throws IOException  
	*/  
	public  static void downloadFile(String path, String urlStr) throws Exception {  
		URL url = null;
		HttpURLConnection con = null;
		BufferedImage input = null;
		BufferedOutputStream output = null;
		
		File pathDir = new File(path);
		if(!pathDir.getParentFile().exists()){//����ļ���·�������ڣ������ļ���
			pathDir.getParentFile().mkdirs();
		}
		
		//��ȡͼƬ��
		url = new URL(urlStr);
		con = (HttpURLConnection)url.openConnection();
		input = ImageIO.read(con.getInputStream());
		
		//дͼƬ
		output = new BufferedOutputStream(new FileOutputStream(pathDir));
		ImageIO.write(input, "jpg", output);
		 
		output.flush();
		output.close();
		con.disconnect();
		
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

}
