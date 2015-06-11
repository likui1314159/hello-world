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
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;

import com.dyst.elasticsearch.util.ESClientManager;
import com.dyst.elasticsearch.util.ESutil;
import com.dyst.oracle.DBConnectionManager;
import com.dyst.util.InterUtil;

/**
 * @author likui
 */
public class CopyImageTxtByCon {

	public static void main(String[] args) {
		try {
			//���ؼ��鳵���켣
			CopyImageTxtByConInsertDir("D:\\tpid.txt", "D://images//");
		} catch (Exception e) {
		}
	}
	
	public static void CopyImageTxtByConInsertDir(String filePath, String dirPath) {
		
		BufferedReader bf = null;
		String str;
		try {
			//��ȡtxt�ļ��ĳ��ƺţ�ÿһ�����ƺŲ�ѯһ�ι켣��д��ָ�����ļ�����
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "GBK"));
			while ((str = bf.readLine()) != null) {//ÿ�����ƺŲ�ѯһ��
//				ss.substring(ss.indexOf("ͼƬID1:")+6, 32)
				if(str.contains("ͼƬID1:")){
					String tpid = str.substring(str.indexOf("ͼƬID1:")+6, str.indexOf("ͼƬID1:")+32);
					System.out.println(tpid);
					System.out.println(getPic("D://images//","20140707110313002040360211"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
				if(exists(tpPath)){//������ھ�ִ��
					downloadFile(path+File.separator+tpid+".jpg", tpPath);
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
	@Test
	public void TestPic(){
		//20140608000001293060360121
//		System.out.println(getPic("D://images//","20140707110313002040360211"));
//		String ss = "ͼƬID1:20140707110258002050140521ͼƬID1:2";
//		String tpid ="20140707110258002050140521";
		//20140707110258002050140521
//		System.out.println(ss.substring(ss.indexOf("ͼƬID1:")+6, 32));
//		System.out.println(tpid.substring(0,tpid.length()-1)+ "2");
		getPic("D://", "20140608000002381010020631");
	}

}
