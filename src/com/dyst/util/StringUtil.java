package com.dyst.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class StringUtil {
	// ת���ַ���Ϊʮ�����Ʊ���

	private static String hexString="0123456789ABCDEF"; 
	public static String toHexString(String str) {
		// ����Ĭ�ϱ����ȡ�ֽ�����
		byte[] bytes = str.getBytes();
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		// ���ֽ�������ÿ���ֽڲ���2λ16��������
		for (int i = 0; i < bytes.length; i++) {
			sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
			sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
		}
		return sb.toString();

	}
	
	public static String decode(String bytes) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
				bytes.length() / 2);
		// ��ÿ2λ16����������װ��һ���ֽ�
		for (int i = 0; i < bytes.length(); i += 2)
			baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
					.indexOf(bytes.charAt(i + 1))));
		return new String(baos.toByteArray());
	}

	 
	// ������д������
	public static void writerTXT(String conent) {
//		InputStream in = StringUtil.class.getResourceAsStream("/rizhi.log");
		String txtPath = "D:/���ݵ����¼.log";
		try {
			File file = new File(txtPath);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			// д��txt�ļ�
			FileWriter fileWriter = new FileWriter(txtPath, true);
			BufferedWriter bw = new BufferedWriter(fileWriter);
			bw.newLine();
			bw.write(conent);
			fileWriter.flush();
			bw.close();
			fileWriter.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * д��־�ļ������ƶ�Ŀ¼��Щ��־��Ϣ����־��Ϣ������.log��ʽ��ţ�һ��һ���ļ�
	 * @param filePath  ��־�ļ�����ļ���
	 * @param conent    д������
	 */
	public static void writerTXT(String filePath, String conent) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			File fileFolder = new File(filePath);
			if (!fileFolder.getParentFile().exists()) {
				fileFolder.getParentFile().mkdirs();
			}
			if (!fileFolder.exists()) {
				fileFolder.mkdirs();
			}
			File file = new File(fileFolder + File.separator
					+ sdf.format(new Date()) + ".log");// ��־�ļ�
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fileWriter);
			bw.newLine();
			bw.write("д��ʱ�䣺"+sdf1.format(new Date())+"����־���ݣ�"+conent);
			fileWriter.flush();
			bw.close();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	  //������д������
	public static void JmeterwriterTXT(String conent){  
		     String  txtPath="D:/JmeterwriterTXT.log";
		         try {  
		              File file = new File(txtPath);  
		              if(!file.getParentFile().exists()){  
		                 file.getParentFile().mkdirs();
		              }  
		              if(!file.exists()){  
		                  file.createNewFile();
		              }  
		              //д��txt�ļ�     
		              FileWriter fileWriter = new FileWriter(txtPath,true);  
		              BufferedWriter bw = new BufferedWriter(fileWriter);  
		              bw.newLine();  
		              bw.write(conent);  
		              fileWriter.flush();  
		              bw.close();  
		              fileWriter.close();  
		                
		          } catch (Exception e) {  
		              e.printStackTrace();  
		          }  
		      }  
}
