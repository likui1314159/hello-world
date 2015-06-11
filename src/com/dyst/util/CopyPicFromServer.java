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
 * ͼƬ����ʵ���ࡣ�µķ�ʽ
 * @author Administrator
 *
 */
public class CopyPicFromServer {
	/**
	 * ͼƬ������ʽ��������洢
	 * @param pic  ͼƬid
	 * @param flag ��־λ
	 * @return �������ͼƬ���ӵ�ַ
	 */
	public static String CopyPicReturnPath(String pic, String flag) {
//		String xtFlag = Config.getInstance().getSysFlag();
		Config config = Config.getInstance();
		String picURL = config.getPicURL();//ͼƬ����ǰ׺
		
		String wftpURL = config.getWftpURL();//Υ��ͼƬ����url
		String wftpUrl = config.getGcscpicUrl();//Υ��ͼƬ���·��
		
		String hcUrl = config.getCacheUrl();//����·��
		int ccNum = Integer.parseInt(config.getStorageNum());//���ع�����
		String ccUrl = config.getStorageUrl();//���ع���·��
		
        //2013 06 30 18211596101A002331
		String year_month_day = "";
		if (pic != null && !"".equals(pic)) {
			int filePath1 = Integer.parseInt(pic.substring(0, 4));//��
			int filePath2 =Integer.parseInt(pic.substring(4, 6));//��
			int filePath3 = Integer.parseInt(pic.substring(6, 8));//��
			String filePath4 = pic.substring(16, 24);//����
			
			//-----ͼƬid����Ϊ27λ--,,,,,,�����м�����ͼƬid�����쳣�Ĵ�����
			if(pic.length() == 27){
				filePath4 = pic.substring(17, 25);//����id	
			}
			
			////�����������ַ���       ��/��/��   ����·��
			year_month_day = File.separator + filePath1 + File.separator
								+ filePath2 + File.separator + filePath3 + File.separator;
			
			//-------------------
			//ͼƬ����ڻ���ľ���·��
			String hcPath = hcUrl + year_month_day + pic + ".jpg";
			if("02".equals(flag)){
				//������ڻ����У�ֱ�ӷ���·��
				if (exists(hcPath)) {
					
				} else {
					try {
						if(ccUrl != null && !"".equals(ccUrl)){
							//��������·��
							String[] arrStr = ccUrl.split(",");
							
							//�����߳�
							CountDownLatch threadsSignal = new CountDownLatch(ccNum);
							storageThread st = null;
							
							//ѭ�������߳�
							for(int i = 0;i < ccNum;i++){
								String ccPath = arrStr[i] + year_month_day + filePath4 + File.separator + pic + ".jpg";//ͼƬ����ڹ��ع���·��1��·��
								st = new storageThread(threadsSignal, hcPath, ccPath);
								
								//�����߳�
								st.start();
							}
							threadsSignal.await();//�ȴ��߳̽���
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}else if("07".equals(flag)){
				String wfUrl = wftpUrl + year_month_day + filePath4 + File.separator + pic + ".jpg";//ͼƬ���Ŀ¼
				if(exists(hcPath) && !exists(wfUrl)){//�����������ͼƬ������Υ���в�����
					copyImage(hcPath, wfUrl);
				}
				return wftpURL + year_month_day + filePath4 + File.separator + pic + ".jpg";
			}
		}
		return picURL + year_month_day + pic + ".jpg";//ͼƬǰ׺+ͼƬ�ɣ�
	}
	
	/**
	 * ͼƬ������ʽ����ǰ�˴洢
	 * @param pic  ͼƬid
	 * @param flag ��־λ
	 * @return �������ͼƬ���ӵ�ַ
	 */
	public static String CopyPicReturnPath2(String pic, String flag, String httpStr) {
		Config config = Config.getInstance();
//		String xtFlag = config.getSysFlag();
		String picURL = config.getPicURL();//ͼƬ����ǰ׺
		String wftpURL = config.getWftpURL();//Υ��ͼƬ����url
		String hcUrl = config.getCacheUrl();//����·��
		String wftpUrl = config.getGcscpicUrl();
		String logFolder = config.getLogFolder();//��־�ļ��洢·��
		
		//����   /��/��/��/      ·��
		int year = Integer.parseInt(pic.substring(0, 4));//��
		int month =Integer.parseInt(pic.substring(4, 6));//��
		int day = Integer.parseInt(pic.substring(6, 8));//��
		String jcdid = pic.substring(16, 24);//����id
		
		//-----ͼƬid����Ϊ27λ--,,,,,,�����м�����ͼƬid�����쳣�Ĵ�����
		if(pic.length() == 27){
			jcdid = pic.substring(17, 25);//����id	
		}
		
		String year_month_day = File.separator + year + File.separator + month + File.separator + day + File.separator;//����    /��/��/��/   ·���ַ���
		if (pic != null && !"".equals(pic)) {
			String hcPath = hcUrl + year_month_day + pic + ".jpg";//���ʻ����·��
			if("02".equals(flag)){//��������ڣ�����Զ���л�ȡ����ŵ�������
				if (exists(hcPath)) {//������ڻ����У�ֱ�ӷ���·��
					
				}else{
					if(httpStr != null && !"".equals(httpStr)){
						httpStr = httpStr + year_month_day + jcdid + File.separator + pic + ".jpg";//Զ��ͼƬĿ¼
						try {
//							if(InterUtil.exists(httpStr)){//���ͼƬ���ڣ����Ƶ�����
								downloadFile(hcPath, httpStr);//���Ƶ�����
//							}
						} catch (Exception e) {
							StringUtil.writerTXT(logFolder, "Copy picture failure��" + httpStr);
							e.printStackTrace();
						}
					}
				}
			}else if("07".equals(flag)){//������ǵ�ȡ����ͼƬ����Υ������ͼƬת��
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
	 * �ж�ͼƬ�Ƿ����
	 * @param pName
	 * @return
	 */
	public static boolean exists(String pName) {
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
	}
	
	/**
	 * ͣ����ͼƬ
	 * @param tpid
	 * @return
	 */
	public static String PicCall(String tpid, String flag){
//		String xtFlag = Config.getInstance().getSysFlag();
		Config config = Config.getInstance();
		String picURL = config.getPicURL();//ͼƬ����ǰ׺
		String wftpURL = config.getWftpURL();//Υ��ͼƬ����url
		String hcUrl = config.getCacheUrl();//����·��
		String wftpUrl = config.getGcscpicUrl();
		String serverIp = Config.getInstance().getPicCall(); 
		Client client = null;
    	
		//����   /��/��/��/      ·��
		int year = Integer.parseInt(tpid.substring(0, 4));//��
		int month =Integer.parseInt(tpid.substring(4, 6));//��
		int day = Integer.parseInt(tpid.substring(6, 8));//��
		String jcdid = tpid.substring(16, 24);//����id
		 
		//-----ͼƬid����Ϊ27λ--,,,,,,�����м�����ͼƬid�����쳣�Ĵ�����
		if(tpid.length() == 27){
			jcdid = tpid.substring(17, 25);//����id	
		}
		
		String year_month_day = File.separator + year + File.separator + month + File.separator + day + File.separator;//����    /��/��/��/   ·���ַ���
		if (tpid != null && !"".equals(tpid)) {
			String hcPath = hcUrl + year_month_day + tpid + ".jpg";//���ʻ����·��
			if("02".equals(flag)){//��������ڣ�����Զ���л�ȡ����ŵ�������
				if (exists(hcPath)) {//������ڻ����У�ֱ�ӷ���·��
					
				}else{
					try {
						client = new Client(new URL(serverIp));
						Object[] s = client.invoke("PicCall2", new String[] {tpid});
						String picUrl = (String)s[0];
						if(picUrl != null && !"".equals(picUrl) && InterUtil.exists(picUrl)){//���ͼƬ���ڣ����Ƶ�����
							//���Ƶ�����
							downloadFile(hcPath, picUrl);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else if("07".equals(flag)){//������ǵ�ȡ����ͼƬ����Υ������ͼƬת��
				String wfUrl = wftpUrl + year_month_day + jcdid + File.separator + tpid + ".jpg";//ͼƬ���Ŀ¼
				if(exists(hcPath) && !exists(wfUrl)){//�����������ͼƬ������Υ���в�����
					copyImage(hcPath, wfUrl);
				}
				return wftpURL + year_month_day + jcdid + File.separator + tpid + ".jpg";
			}
		}
		return picURL + year_month_day + tpid + ".jpg";//ͼƬǰ׺+ͼƬID
	}
	
	public static void main(String[] args) {
//		System.out.println(CopyPicReturnPath("2013070915502461101A001541", "07"));
		System.out.println(CopyPicReturnPath2("20140418110744000101A005631", "02","http://10.103.1.141"));
//		copyImage("D:\\home\\esLog\\bkService\\2014-02-19.log", "C:\\home\\esLog\\bkService\\2014-02-19.log");
	}
}
