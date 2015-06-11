package com.dyst.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class StringUtil {
	// 转化字符串为十六进制编码

	private static String hexString="0123456789ABCDEF"; 
	public static String toHexString(String str) {
		// 根据默认编码获取字节数组
		byte[] bytes = str.getBytes();
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		// 将字节数组中每个字节拆解成2位16进制整数
		for (int i = 0; i < bytes.length; i++) {
			sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
			sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
		}
		return sb.toString();

	}
	
	public static String decode(String bytes) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
				bytes.length() / 2);
		// 将每2位16进制整数组装成一个字节
		for (int i = 0; i < bytes.length(); i += 2)
			baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
					.indexOf(bytes.charAt(i + 1))));
		return new String(baos.toByteArray());
	}

	 
	// 参数：写入内容
	public static void writerTXT(String conent) {
//		InputStream in = StringUtil.class.getResourceAsStream("/rizhi.log");
		String txtPath = "D:/数据导入记录.log";
		try {
			File file = new File(txtPath);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			// 写入txt文件
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
	 * 写日志文件，在制定目录下些日志信息，日志信息以日期.log形式存放，一天一个文件
	 * @param filePath  日志文件存放文件夹
	 * @param conent    写入内容
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
					+ sdf.format(new Date()) + ".log");// 日志文件
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fileWriter);
			bw.newLine();
			bw.write("写入时间："+sdf1.format(new Date())+"，日志内容："+conent);
			fileWriter.flush();
			bw.close();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	  //参数：写入内容
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
		              //写入txt文件     
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
