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
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import org.codehaus.xfire.client.Client;

/**
 * 图片解析实现类。新的方式
 * @author Administrator
 *
 */
public class CopyPicFromServer {
	/**
	 * 图片解析方式——中央存储
	 * @param pic  图片id
	 * @param flag 标志位
	 * @return 解析后的图片链接地址
	 */
	public static String CopyPicReturnPath(String pic, String flag) {
//		String xtFlag = Config.getInstance().getSysFlag();
		Config config = Config.getInstance();
		String picURL = config.getPicURL();//图片调用前缀
		
		String wftpURL = config.getWftpURL();//违法图片访问url
		String wftpUrl = config.getGcscpicUrl();//违法图片存放路径
		
		String hcUrl = config.getCacheUrl();//缓存路径
		int ccNum = Integer.parseInt(config.getStorageNum());//挂载共享数
		String ccUrl = config.getStorageUrl();//挂载共享路径
		
        //2013 06 30 18211596101A002331
		String year_month_day = "";
		if (pic != null && !"".equals(pic)) {
			int filePath1 = Integer.parseInt(pic.substring(0, 4));//年
			int filePath2 =Integer.parseInt(pic.substring(4, 6));//月
			int filePath3 = Integer.parseInt(pic.substring(6, 8));//日
			String filePath4 = pic.substring(16, 24);//监测点
			
			//-----图片id长度为27位--,,,,,,由于有几个点图片id出现异常的处理方法
			if(pic.length() == 27){
				filePath4 = pic.substring(17, 25);//监测点id	
			}
			
			////生成年月日字符串       年/月/日   三层路径
			year_month_day = File.separator + filePath1 + File.separator
								+ filePath2 + File.separator + filePath3 + File.separator;
			
			//-------------------
			//图片存放在缓存的绝对路径
			String hcPath = hcUrl + year_month_day + pic + ".jpg";
			if("02".equals(flag)){
				//如果存在缓存中，直接返回路径
				if (exists(hcPath)) {
					
				} else {
					try {
						if(ccUrl != null && !"".equals(ccUrl)){
							//解析缓存路径
							String[] arrStr = ccUrl.split(",");
							
							//创建线程
							CountDownLatch threadsSignal = new CountDownLatch(ccNum);
							storageThread st = null;
							
							//循环创建线程
							for(int i = 0;i < ccNum;i++){
								String ccPath = arrStr[i] + year_month_day + filePath4 + File.separator + pic + ".jpg";//图片存放在挂载共享路径1的路径
								st = new storageThread(threadsSignal, hcPath, ccPath);
								
								//启动线程
								st.start();
							}
							threadsSignal.await();//等待线程结束
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}else if("07".equals(flag)){
				String wfUrl = wftpUrl + year_month_day + filePath4 + File.separator + pic + ".jpg";//图片存放目录
				if(exists(hcPath) && !exists(wfUrl)){//如果缓存中有图片，并且违法中不存在
					copyImage(hcPath, wfUrl);
				}
				return wftpURL + year_month_day + filePath4 + File.separator + pic + ".jpg";
			}
		}
		return picURL + year_month_day + pic + ".jpg";//图片前缀+图片ＩＤ
	}
	
	/**
	 * 图片解析方式——前端存储
	 * @param pic  图片id
	 * @param flag 标志位
	 * @return 解析后的图片链接地址
	 */
	public static String CopyPicReturnPath2(String pic, String flag, String httpStr) {
		Config config = Config.getInstance();
//		String xtFlag = config.getSysFlag();
		String picURL = config.getPicURL();//图片调用前缀
		String wftpURL = config.getWftpURL();//违法图片访问url
		String hcUrl = config.getCacheUrl();//缓存路径
		String wftpUrl = config.getGcscpicUrl();
		String logFolder = config.getLogFolder();//日志文件存储路径
		
		//构造   /年/月/日/      路径
		int year = Integer.parseInt(pic.substring(0, 4));//年
		int month =Integer.parseInt(pic.substring(4, 6));//月
		int day = Integer.parseInt(pic.substring(6, 8));//日
		String jcdid = pic.substring(16, 24);//监测点id
		
		//-----图片id长度为27位--,,,,,,由于有几个点图片id出现异常的处理方法
		if(pic.length() == 27){
			jcdid = pic.substring(17, 25);//监测点id	
		}
		
		String year_month_day = File.separator + year + File.separator + month + File.separator + day + File.separator;//生成    /年/月/日/   路径字符串
		if (pic != null && !"".equals(pic)) {
			String hcPath = hcUrl + year_month_day + pic + ".jpg";//访问缓存的路径
			if("02".equals(flag)){//如果不存在，则重远程中获取并存放到缓存中
				if (exists(hcPath)) {//如果存在缓存中，直接返回路径
					
				}else{
					if(httpStr != null && !"".equals(httpStr)){
						httpStr = httpStr + year_month_day + jcdid + File.separator + pic + ".jpg";//远程图片目录
						try {
//							if(InterUtil.exists(httpStr)){//如果图片存在，则复制到缓存
								downloadFile(hcPath, httpStr);//复制到缓存
//							}
						} catch (Exception e) {
							StringUtil.writerTXT(logFolder, "Copy picture failure：" + httpStr);
							e.printStackTrace();
						}
					}
				}
			}else if("07".equals(flag)){//如果不是调取过车图片，则违法过车图片转移
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
	 * 判断图片是否存在
	 * @param pName
	 * @return
	 */
	public static boolean exists(String pName) {
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
	}
	
	/**
	 * 停车场图片
	 * @param tpid
	 * @return
	 */
	public static String PicCall(String tpid, String flag){
//		String xtFlag = Config.getInstance().getSysFlag();
		Config config = Config.getInstance();
		String picURL = config.getPicURL();//图片调用前缀
		String wftpURL = config.getWftpURL();//违法图片访问url
		String hcUrl = config.getCacheUrl();//缓存路径
		String wftpUrl = config.getGcscpicUrl();
		String serverIp = Config.getInstance().getPicCall(); 
		Client client = null;
    	
		//构造   /年/月/日/      路径
		int year = Integer.parseInt(tpid.substring(0, 4));//年
		int month =Integer.parseInt(tpid.substring(4, 6));//月
		int day = Integer.parseInt(tpid.substring(6, 8));//日
		String jcdid = tpid.substring(16, 24);//监测点id
		 
		//-----图片id长度为27位--,,,,,,由于有几个点图片id出现异常的处理方法
		if(tpid.length() == 27){
			jcdid = tpid.substring(17, 25);//监测点id	
		}
		
		String year_month_day = File.separator + year + File.separator + month + File.separator + day + File.separator;//生成    /年/月/日/   路径字符串
		if (tpid != null && !"".equals(tpid)) {
			String hcPath = hcUrl + year_month_day + tpid + ".jpg";//访问缓存的路径
			if("02".equals(flag)){//如果不存在，则重远程中获取并存放到缓存中
				if (exists(hcPath)) {//如果存在缓存中，直接返回路径
					
				}else{
					try {
						client = new Client(new URL(serverIp));
						Object[] s = client.invoke("PicCall2", new String[] {tpid});
						String picUrl = (String)s[0];
						if(picUrl != null && !"".equals(picUrl) && InterUtil.exists(picUrl)){//如果图片存在，则复制到缓存
							//复制到缓存
							downloadFile(hcPath, picUrl);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else if("07".equals(flag)){//如果不是调取过车图片，则违法过车图片转移
				String wfUrl = wftpUrl + year_month_day + jcdid + File.separator + tpid + ".jpg";//图片存放目录
				if(exists(hcPath) && !exists(wfUrl)){//如果缓存中有图片，并且违法中不存在
					copyImage(hcPath, wfUrl);
				}
				return wftpURL + year_month_day + jcdid + File.separator + tpid + ".jpg";
			}
		}
		return picURL + year_month_day + tpid + ".jpg";//图片前缀+图片ID
	}
	
	public static void main(String[] args) {
//		System.out.println(CopyPicReturnPath("2013070915502461101A001541", "07"));
		System.out.println(CopyPicReturnPath2("20140418110744000101A005631", "02","http://10.103.1.141"));
//		copyImage("D:\\home\\esLog\\bkService\\2014-02-19.log", "C:\\home\\esLog\\bkService\\2014-02-19.log");
	}
}
