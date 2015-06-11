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
	 * 根据图片id转换成图片地址<br>旧方式
	 * @param pic
	 * @param flag 1:公安网地址   0：交警图片地址
	 * @return
	 */
	public static String CopyPicReturnPath(String pic, String flag) {
		Config config = Config.getInstance();
		String picURL = config.getPicURL();//图片调用前缀
		String wftpURL = config.getWftpURL();//违法图片访问url
		String hcUrl = config.getCacheUrl();//缓存路径
		String wftpUrl = config.getGcscpicUrl();
		
		//构造   /年/月/日/      路径
		int year = Integer.parseInt(pic.substring(0, 4));//年
		int month =Integer.parseInt(pic.substring(4, 6));//月
		int day = Integer.parseInt(pic.substring(6, 8));//日
		String jcdid = pic.substring(16, 24);//监测点id
		if(pic.length()==27){//图片id长度为27位--
			jcdid = pic.substring(17, 25);//监测点id	
		}
		String year_month_day = File.separator + year + File.separator + month + File.separator + day + File.separator;//生成    /年/月/日/   路径字符串
		if (pic != null && !"".equals(pic)) {
			String hcPath = hcUrl + year_month_day + pic + ".jpg";//访问缓存的路径
			if("02".equals(flag)){//如果不存在，则重远程中获取并存放到缓存中
				if (existsHc(hcPath)) {//如果存在缓存中，直接返回路径
					
				}else{
					/**
					 * 根据图片ID转换成图片绝对地址
					 */
					String picDate = pic.substring(0, 8);// 对应的日期
					List temp_ip = getPicServerIpByJcdId(jcdid);// 调用方法，根据监测点id不同获取不同图片存放中心的地址
					String hh_mm = pic.substring(8, 10) + "-" + pic.substring(10, 12);
						
					// 所要获取的图片所在服务起的地址
					String picSerIp = "";
					if (temp_ip != null && temp_ip.size() > 0) {// 如果存在数据字典服务器
						for(int i = 0;i < temp_ip.size();i++) {
							//生成路径
							picSerIp = temp_ip.get(i) + "store" + day%10 + "/" + picDate + "/" + hh_mm + "/" + pic + ".jpg";
								
							//校验看图片是否存在
							Boolean b = exists(picSerIp);
							if(b){
								if(picSerIp != null && !"".equals(picSerIp)){
									try {
										//复制到缓存
										downloadFile(hcPath, picSerIp);
										
										//返回直接地址
//										return picSerIp;
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			} else if("07".equals(flag)){//如果不是调取过车图片，则违法过车图片转移
				String wfUrl = wftpUrl + year_month_day + jcdid + File.separator + pic + ".jpg";//图片存放目录
				if(exists(hcPath) && !exists(wfUrl)){//如果缓存中有图片，并且违法中不存在
					copyImage(hcPath, wfUrl);
				}
				return wftpURL + year_month_day + jcdid + File.separator + pic + ".jpg";
			}
		}
		return picURL + year_month_day + pic + ".jpg";//图片前缀+图片ID
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
	 * 判断图片是否存在缓存中
	 * @param pName
	 * @return
	 */
	public static boolean existsHc(String pName) {
		return new File(pName).exists();
	}
	
	/**
	 * 将图片复制到缓存路径下
	 * @param src
	 *            原路径（缓存路径）
	 * @param dest
	 *            目的路径（文件存放目录）
	 */
	public static void copyImage(String src, String dest) {
		BufferedInputStream input = null;
		BufferedOutputStream output = null;
		File srcDir = new File(src);
		File destDir = new File(dest);
		try {
			if(!destDir.getParentFile().exists()){//如果文件夹路径不存在，创建文件夹
				destDir.getParentFile().mkdirs();
			}
			
			//读取图片流
			input = new BufferedInputStream(new FileInputStream(srcDir));
			byte[] data = new byte[input.available()];
			input.read(data);
			
			//写流
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
	* 将http地址形势的文件下载并保存到本地  
	*   
	* @param path  
	*            文件保存位置  
	* @param url  
	*            文件url地址  
	* @throws IOException  
	*/  
	public static void downloadFile(String path, String urlStr) throws Exception {  
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
		
		//等待6秒后返回，等待拷贝完成
		try {
			Thread.sleep(1000*2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据检测点ID获得对应图片所在服务器IP
	 * 不同的jcdid的图片存放在不同的服务器上
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
			//lbdm 为0045，交警网IP ；0046为公安网Ip
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
	 * 根据图片id转换成图片地址(返回所有可能的图片地址，不校验图片是否存在)
	 * @param pic
	 * @param flag 1:公安网地址   0：交警图片地址
	 * @return
	 */
	public static String CopyPicReturnPaths(String pic,String flag) {
		/**
		 * 根据图片ID转换成图片绝对地址
		 */
		// 20121101  164523 21 10300503 1 1
		// 日期                 
		// 时间 随机数 jcdid cdid tpid1
		if (pic != null && !"".equals(pic)) {
//			List<String> list = new ArrayList<String>();
			
			String day = (String) pic.subSequence(6, 8);
			int temp_day = Integer.parseInt(day);
			String picDate = pic.substring(0, 8);// 对应的日期
			String jcdId = pic.substring(16, 24);// 监测点id
			String hh_mm = pic.substring(8, 10) + "-" + pic.substring(10, 12);
			List temp_ip = getPicServerIpByJcdId(jcdId);// 调用方法，根据监测点id不同获取不同图片存放中心的地址
			
			// 所要获取的图片所在服务起的地址
			String picSerIp = "";
			StringBuffer sb = new StringBuffer();
			if (temp_ip != null && temp_ip.size() > 0) {// 如果存在数据字典服务器
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
//	 * 测试主函数
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
