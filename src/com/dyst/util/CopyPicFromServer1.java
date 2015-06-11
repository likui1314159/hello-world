package com.dyst.util;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

@SuppressWarnings("unchecked")
public class CopyPicFromServer1 {
	/**
	 * ����ͼƬidת����ͼƬ��ַ<br>�ɷ�ʽ
	 * @param pic
	 * @param flag 1:��������ַ   0������ͼƬ��ַ
	 * @return
	 */
	public static String CopyPicReturnPath(String pic, String flag) {
		Config config = Config.getInstance();
		String picURL = config.getPicURL();//ͼƬ����ǰ׺
		String wftpURL = config.getWftpURL();//Υ��ͼƬ����url
		String hcUrl = config.getCacheUrl();//����·��
		String wftpUrl = config.getGcscpicUrl();
		
		//����   /��/��/��/      ·��
		int year = Integer.parseInt(pic.substring(0, 4));//��
		int month =Integer.parseInt(pic.substring(4, 6));//��
		int day = Integer.parseInt(pic.substring(6, 8));//��
		String jcdid = pic.substring(16, 24);//����id
		if(pic.length()==27){//ͼƬid����Ϊ27λ--
			jcdid = pic.substring(17, 25);//����id	
		}
		String year_month_day = File.separator + year + File.separator + month + File.separator + day + File.separator;//����    /��/��/��/   ·���ַ���
		if (pic != null && !"".equals(pic)) {
			String hcPath = hcUrl + year_month_day + pic + ".jpg";//���ʻ����·��
			if("02".equals(flag)){//��������ڣ�����Զ���л�ȡ����ŵ�������
				if (existsHc(hcPath)) {//������ڻ����У�ֱ�ӷ���·��
					
				}else{
					/**
					 * ����ͼƬIDת����ͼƬ���Ե�ַ
					 */
					String picDate = pic.substring(0, 8);// ��Ӧ������
					List temp_ip = getPicServerIpByJcdId(jcdid);// ���÷��������ݼ���id��ͬ��ȡ��ͬͼƬ������ĵĵ�ַ
					String hh_mm = pic.substring(8, 10) + "-" + pic.substring(10, 12);
						
					// ��Ҫ��ȡ��ͼƬ���ڷ�����ĵ�ַ
					String picSerIp = "";
					if (temp_ip != null && temp_ip.size() > 0) {// ������������ֵ������
						for(int i = 0;i < temp_ip.size();i++) {
							//����·��
							picSerIp = temp_ip.get(i) + "store" + day%10 + "/" + picDate + "/" + hh_mm + "/" + pic + ".jpg";
								
							//У�鿴ͼƬ�Ƿ����
							Boolean b = exists(picSerIp);
							if(b){
								if(picSerIp != null && !"".equals(picSerIp)){
									try {
										//���Ƶ�����
										downloadFile(hcPath, picSerIp);
										
										//����ֱ�ӵ�ַ
//										return picSerIp;
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			} else if("07".equals(flag)){//������ǵ�ȡ����ͼƬ����Υ������ͼƬת��
				String wfUrl = wftpUrl + year_month_day + jcdid + File.separator + pic + ".jpg";//ͼƬ���Ŀ¼
				if(exists(hcPath) && !exists(wfUrl)){//�����������ͼƬ������Υ���в�����
					copyImage(hcPath, wfUrl);
				}
				return wftpURL + year_month_day + jcdid + File.separator + pic + ".jpg";
			}
		}
		return picURL + year_month_day + pic + ".jpg";//ͼƬǰ׺+ͼƬID
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
	 * �ж�ͼƬ�Ƿ���ڻ�����
	 * @param pName
	 * @return
	 */
	public static boolean existsHc(String pName) {
		return new File(pName).exists();
	}
	
	/**
	 * ��ͼƬ���Ƶ�����·����
	 * @param src
	 *            ԭ·��������·����
	 * @param dest
	 *            Ŀ��·�����ļ����Ŀ¼��
	 */
	public static void copyImage(String src, String dest) {
		BufferedInputStream input = null;
		BufferedOutputStream output = null;
		File srcDir = new File(src);
		File destDir = new File(dest);
		try {
			if(!destDir.getParentFile().exists()){//����ļ���·�������ڣ������ļ���
				destDir.getParentFile().mkdirs();
			}
			
			//��ȡͼƬ��
			input = new BufferedInputStream(new FileInputStream(srcDir));
			byte[] data = new byte[input.available()];
			input.read(data);
			
			//д��
			output = new BufferedOutputStream(new FileOutputStream(destDir));
			output.write(data);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (input != null){
					input.close();
				}
				if (output != null){
					output.close();
				}
			} catch (IOException ioe) {

			}
		}
	}

	/**  
	* ��http��ַ���Ƶ��ļ����ز����浽����  
	*   
	* @param path  
	*            �ļ�����λ��  
	* @param url  
	*            �ļ�url��ַ  
	* @throws IOException  
	*/  
	public static void downloadFile(String path, String urlStr) throws Exception {  
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
		
		//�ȴ�6��󷵻أ��ȴ��������
		try {
			Thread.sleep(1000*2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ���ݼ���ID��ö�ӦͼƬ���ڷ�����IP
	 * ��ͬ��jcdid��ͼƬ����ڲ�ͬ�ķ�������
	 * @param jcdId
	 * @return 
	 */
	public static List getPicServerIpByJcdId(String jcdId){
		DateConnection dataCon = new DateConnection();
		Connection con = dataCon.getconnect();
		Statement st = null;
		ResultSet rs = null;
		
		List<String> tuip = new ArrayList<String>();
		if(jcdId == null || "".equals(jcdId)){
			return tuip;
		}
		
		try {
			String picIp = null;
			//lbdm Ϊ0045��������IP ��0046Ϊ������Ip
			String sqlStr = "select describe from dict where typecode = '1111' " +
					" and trim(typeseries) = (select trim(lsfzx) from jcd where id = '" + jcdId + "')";
//			String sqlStr = "select lbms from sjzd where lbdm = '0045' " +
//			" and trim(lbxh) = (select trim(lsfzx) from jcd where id='" + jcdId + "')";
			st = con.createStatement();
			rs  = st.executeQuery(sqlStr);
			while(rs.next()){
				picIp = rs.getString(1).toString();
				tuip.add(picIp);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if (rs != null){
				try {
					rs.close();
					rs = null;
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			
			if (st != null){
				try {
					st.close();
					st = null;
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			
			if (con != null){
				try {
					con.close();
					con = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return tuip;
	}
	
	
	/**
	 * ����ͼƬidת����ͼƬ��ַ(�������п��ܵ�ͼƬ��ַ����У��ͼƬ�Ƿ����)
	 * @param pic
	 * @param flag 1:��������ַ   0������ͼƬ��ַ
	 * @return
	 */
	public static String CopyPicReturnPaths(String pic,String flag) {
		/**
		 * ����ͼƬIDת����ͼƬ���Ե�ַ
		 */
		// 20121101  164523 21 10300503 1 1
		// ����                 
		// ʱ�� ����� jcdid cdid tpid1
		if (pic != null && !"".equals(pic)) {
//			List<String> list = new ArrayList<String>();
			
			String day = (String) pic.subSequence(6, 8);
			int temp_day = Integer.parseInt(day);
			String picDate = pic.substring(0, 8);// ��Ӧ������
			String jcdId = pic.substring(16, 24);// ����id
			String hh_mm = pic.substring(8, 10) + "-" + pic.substring(10, 12);
			List temp_ip = getPicServerIpByJcdId(jcdId);// ���÷��������ݼ���id��ͬ��ȡ��ͬͼƬ������ĵĵ�ַ
			
			// ��Ҫ��ȡ��ͼƬ���ڷ�����ĵ�ַ
			String picSerIp = "";
			StringBuffer sb = new StringBuffer();
			if (temp_ip != null && temp_ip.size() > 0) {// ������������ֵ������
				int ipsize = temp_ip.size();
				for (int i = 0; i < ipsize; i++) {
					picSerIp = temp_ip.get(i) + "store" + temp_day
					% 10 + "/" + picDate + "/" + hh_mm + "/" + pic
					+ ".jpg";
					
					if("0".equals(flag)){
						picSerIp = picSerIp.replace("10.42.31.101", "100.100.36.201");
					}
					sb.append(picSerIp);
					if(i != ipsize - 1){
						sb.append(",");
					}
				}
			}
			return sb.toString();
		} else {
			return "";
		}
	}
//	/**
//	 * ����������
//	 * @param args
//	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
	   CopyPicFromServer1 c = new CopyPicFromServer1();
	   String s = c.CopyPicReturnPath("20130913000009231030110231","1");
	   System.out.println(s);
	}
//	http://100.100.36.201:8080/pic_server1/store1/20130801/00-00/20130801000006002050140711.jpg
//	http://100.100.36.201:8080/pic_server_226/store1/20130801/00-00/20130801000006002050140711.jpg
}
