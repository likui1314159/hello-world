package com.dyst.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

/*
 * 接口中常用方法
 */
public class InterUtil {

	/**
	 * @param str
	 *            需要过滤的字符串
	 * @return 过滤后的字符
	 */
	public static String keyWordFilter(String str) {
		// 用#分割关键字
		if ("".equals(str) || str == null) {
			return "";
		}
		// System.out.println(str);
		String model_str = "%#and#exec#insert#select#delete#update#count#*#'#chr#mid#master#truncate#char#declare#;#or#-#+#,#<#># ";
		String model_split_str[] = model_str.split("#");
		for (int i = 0; i < model_split_str.length; i++) {
			if (str.indexOf(model_split_str[i]) >= 0) {// >=0说明有关键字，否则说明没有关键字
				str = str.replace(model_split_str[i], "");
			}
		}
		return str;
	}

	/**
	 * 车牌类型转换成汉字车牌颜色
	 * 
	 * @param cplx
	 * @return
	 */
	public static String cplxTrans(String cplx) {
		String getCplx = "&nbsp;";
		if (cplx == null) {
			getCplx = "";
		} else {
			Integer cp1 = Integer.parseInt(cplx);// 把车牌类型转换成对应中文
			switch (cp1) {
			case 0:
				getCplx = "蓝牌";
				break;
			case 1:
				getCplx = "黑牌";
				break;
			case 2:
				getCplx = "黄牌";
				break;
			case 3:
				getCplx = "新黄牌";
				break;
			case 4:
				getCplx = "黄色后牌";
				break;
			case 5:
				getCplx = "警车";
				break;
			case 6:
				getCplx = "军车";
				break;
			case 7:
				getCplx = "新黄色后牌";
				break;
			case 8:
				getCplx = "武警";
				break;
			case 9:
				getCplx = "新白牌";
				break;
			default:
				getCplx = "";
			}
		}
		return getCplx;
	}

	/**
	 * 根据隶属部门id查询应该属于哪个布控范围
	 * 
	 * @param lsbmid
	 * @return
	 */
	public static String lbbmidToBkfw(String lsbmid) {
		String bkbm = "&nbsp;";
		if (lsbmid == null || lsbmid.trim().equals("")) {
			bkbm = "";
		} else if (lsbmid.length() > 6) {
			lsbmid = lsbmid.substring(0, 6);
			if (lsbmid.equals("440300")) {// 获取布控部门信息，根据登陆人员信息
				bkbm = "1";
			} else if (lsbmid.equals("440303")) {
				bkbm = "3";
			} else if (lsbmid.equals("440304")) {
				bkbm = "4";
			} else if (lsbmid.equals("440305")) {
				bkbm = "5";
			} else if (lsbmid.equals("440306")) {
				bkbm = "7";
			} else if (lsbmid.equals("440307")) {
				bkbm = "2";
			} else if (lsbmid.equals("440308")) {
				bkbm = "6";
			} else if (lsbmid.equals("440309")) {
				bkbm = "8";
			} else if (lsbmid.equals("440310")) {
				bkbm = "9";
			} else if (lsbmid.equals("440398")) {
				bkbm = "a";
			} else {
				bkbm = "1";
			}
		}
		return bkbm;
	}

	public static String cplxTOcpys(String cplx) {
		/*
		 * 根据参数cplx得到省厅的车牌颜色数据 省厅的：0-白色，1-黄色，2-蓝色，3-黑色，4-其它颜色 本地：==全部== 蓝牌 0 黑牌
		 * 1 黄牌 2 新黄牌 3 黄色后牌 4 警车 5 军车 6 新黄色后牌 7 武警 8 新白牌- 9
		 */
		String cpys = "";
		if ("0".equals(cplx)) {
			cpys = "蓝色";
		} else if ("2".equals(cplx)) {
			cpys = "黄色";
		} else if ("9".equals(cplx)) {
			cpys = "白色";
		} else if ("1".equals(cplx)) {
			cpys = "黑色";
		} else {
			cpys = "其他颜色";
		}
		return cpys;
	}

	public static String cplxTOStcpys(String cplx) {
		/*
		 * 根据参数cplx得到省厅的车牌颜色数据 省厅的：0-白色，1-黄色，2-蓝色，3-黑色，4-其它颜色 本地：==全部== 蓝牌 0 黑牌
		 * 1 黄牌 2 新黄牌 3 黄色后牌 4 警车 5 军车 6 新黄色后牌 7 武警 8 新白牌- 9
		 */
		String cpys = "";
		if ("0".equals(cplx)) {
			cpys = "2";//
		} else if ("1".equals(cplx)) {
			cpys = "3";//
		} else if ("2".equals(cplx)) {
			cpys = "1";//
		} else if ("3".equals(cplx)) {
			cpys = "1";//
		} else if ("4".equals(cplx)) {
			cpys = "1";//
		} else if ("9".equals(cplx)) {
			cpys = "0";
		} else {
			cpys = "4";
		}
		return cpys;
	}

	/**
	 * 号牌种类转换成车牌类型
	 * 
	 * @param hpzl
	 * @return
	 * @throws Exception
	 */
	public static String HpzlToCplx(String hpzl) throws Exception {
		/*
		 * 根据参数cplx得到省厅的车牌颜色数据 省厅的：0-白色，1-黄色，2-蓝色，3-黑色，4-其它颜色 本地：==全部== 蓝牌 0 黑牌
		 * 1 黄牌 2 新黄牌 3 黄色后牌 4 警车 5 军车 6 新黄色后牌 7 武警 8 新白牌- 9
		 */
		String temp = "2";
		if (hpzl == null || "".equals(hpzl)) {
			temp = "2";
			return temp;
		}
		int i = Integer.parseInt(hpzl);

		switch (i) {
		case 1:
			temp = "2";
			break;

		case 2:
			temp = "0";
			break;
		case 3:
			temp = "1";
			break;

		case 4:
			temp = "1";
			break;

		case 5:
			temp = "1";
			break;

		case 6:
			temp = "1";
			break;

		case 13:
			temp = "2";
			break;
		case 15:
			temp = "2";
			break;
		case 16:
			temp = "2";
			break;
		case 20:
			temp = "1";
			break;
		case 21:
			temp = "1";
			break;
		case 22:
			temp = "1";
			break;
		case 23:
			temp = "5";
			break;
		}
		return temp;
	}

	/**
	 * 根据车牌类型获取相对应的号牌种类
	 * 
	 * @param cplx
	 * @return
	 * @throws Exception
	 */
	public static String cplxToHpzl(String cplx) throws Exception {
		String temp = "";
		if (cplx == null || "".equals(cplx) || !StringUtils.isNumeric(cplx)) {
			temp = "";
			return temp;
		}
		int i = Integer.parseInt(cplx);

		switch (i) {
		case 0:
			temp = "02";
			break;

		case 1:
			temp = "06";
			break;

		case 2:
			temp = "01";
			break;

		case 3:
			temp = "01";
			break;

		case 4:
			temp = "01";
			break;

		case 5:
			temp = "23";
			break;

		case 6:
			temp = "23";
			break;

		case 7:
			temp = "01";
			break;

		case 8:
			temp = "02";
			break;

		case 9:
			temp = "02";
			break;
		}
		return temp;
	}

	/**
	 * 获取本机当前系统时间
	 * 
	 * @return
	 */
	public static String getTime() {
		/*
		 * 获取当前时间的字符串值
		 */
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		java.util.Date date = new Date();
		String time = dateFormat.format(date);
		return time;
	}

	/**
	 * 格式化时间
	 * 
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date) {
		/*
		 * 时间格式化。。。
		 */
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String time = dateFormat.format(date);
		return time;
	}

	/**
	 * 获取当前时间前n天的时间
	 * 
	 * @param n
	 * @return
	 */
	public static String getTime(int n) {
		/*
		 * 获取当前时间的前n天的时间
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -n);
		String mDateTime = sdf.format(c.getTime());
		// String strStart=mDateTime.substring(0,19);
		return mDateTime;
	}
	 
	/**
	 * 获取beginTime 时间点的前n天的时间
	 * 
	 * @param n
	 * @return
	 */
	public static String getTimeGjcx(int n, Date beginTime) {
		/*
		 * 获取beginTime时间点前几天的时间段
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Calendar c = Calendar.getInstance();
		c.setTime(beginTime);
		c.add(Calendar.DAY_OF_MONTH, -n);
		String mDateTime = sdf.format(c.getTime());
		String strStart = mDateTime.substring(0, 19);
		return strStart;
	}

	/**
	 * 根据传输参数xh的长度不满足十位时，补足十位 方法：前端补零
	 * 
	 * @param xh
	 * @return
	 */
	public static String getXh(String xh) {
		/*
		 * 根据传入的参数长度，不满十位的在前面补0.返回十位长的字符串。
		 */
		String seq_bkqqs = "";
		if (xh != null) {
			char a[] = xh.toCharArray();
			int lengh = a.length;

			if (lengh < 10) {
				for (int j = 0; j < 10 - lengh; j++) {
					seq_bkqqs = seq_bkqqs + "0";
				}
			}
			seq_bkqqs = seq_bkqqs + xh;

		}
		return seq_bkqqs;
	}

	/**
	 * 根据传输的xh补足4位时补足4位，前端补零
	 * 
	 * @param xh
	 * @return
	 */
	public static String getLsh(String xh)// 流水号
	{
		/*
		 * 单号生成时调用的方法
		 */
		String seq_bkqqs = "";
		if (xh != null) {
			char a[] = xh.toCharArray();
			int lengh = a.length;

			if (lengh < 4) {
				for (int j = 0; j < 4 - lengh; j++) {
					seq_bkqqs = seq_bkqqs + "0";
				}
			}
			seq_bkqqs = seq_bkqqs + xh;

		}
		return seq_bkqqs;
	}

	public static String getText(String text) {
		/*
		 * 如果字符串为空，则把“”字符串赋给它,使字符串不为null 目前可以不使用该方法，使用StringUtils方法
		 */
		if (text != null) {
			return text;
		}
		return "";
	}

	/**
	 * 通过HttpServletRequest返回IP地址
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return ip String
	 * @throws Exception
	 */
	public static String getIpAddr(HttpServletRequest request) throws Exception {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 手机号码校验 是返回true ,不是返回 false;
	 * 
	 * @param mobiles
	 *            手机号码
	 * @return 校验结果
	 */
	public static boolean isMobileNO(String mobiles) {
		if (mobiles == null || "".equals(mobiles.trim())) {
			return false;
		}
		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		// logger.info(m.matches()+"---");
		return m.matches();
	}

	public static String getDistanceTime(Date one, Date two) {
		// long day = 0;
		// long hour = 0;
		if (one == null || two == null) {
			return "";
		}
		long min = 0;
		long sec = 0;
		try {
			long time1 = one.getTime();
			long time2 = two.getTime();
			long diff;
			if (time1 < time2) {
				diff = time2 - time1;
			} else {
				diff = time1 - time2;
			}
			// day = diff / (24 * 60 * 60 * 1000);
			// hour = (diff / (60 * 60 * 1000) - day * 24);
			min = ((diff / (60 * 1000))); // - day * 24 * 60 - hour * 60
			sec = (diff / 1000 - min * 60); // -day*24*60*60-hour*60*60
		} catch (Exception e) {
			e.printStackTrace();
		}
		return min + "分" + sec + "秒"; // day + "天" + hour + "小时" +
	}

	public static String getDistanceHour(Date one, Date two) {
		long day = 0;
		long hour = 0;
		if (one == null || two == null) {
			return "";
		}
		long min = 0;
		long sec = 0;
		try {
			long time1 = one.getTime();
			long time2 = two.getTime();
			long diff;
			if (time1 < time2) {
				diff = time2 - time1;
			} else {
				diff = time1 - time2;
			}
			day = diff / (24 * 60 * 60 * 1000);
			hour = (diff / (60 * 60 * 1000) - day * 24);
			min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
			sec = (diff / 1000 - min * 60 - day * 24 * 60 * 60 - hour * 60 * 60); //
		} catch (Exception e) {
			e.printStackTrace();
		}
		return day + "天" + hour + "小时" + min + "分" + sec + "秒";
	}

	/**
	 * 两个时间相差
	 * 
	 * @return 返回值为：相差分, 秒
	 */
	public static String getDistanceTimes(Date dateBegin, Date dateEnd) {
		if (dateBegin == null || "".equals(dateBegin) || dateEnd == null
				|| "".equals(dateEnd)) {
			return "";
		}
		long day = 0;
		long hour = 0;
		long min = 0;
		// long sec = 0;
		try {
			long time1 = dateBegin.getTime();
			long time2 = dateEnd.getTime();
			long diff;
			if (time1 < time2) {
				diff = time2 - time1;
			} else {
				diff = time1 - time2;
			}
			day = diff / (24 * 60 * 60 * 1000);
			hour = (diff / (60 * 60 * 1000) - day * 24);
			min = (diff / (60 * 1000 - day * 24 * 60 - hour * 60));
			// sec = (diff/1000-day*24*60*60-hour*60*60-min*60);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return min + "分钟" + min + "秒";
	}

	public void printDclog() {
		BufferedReader br = null;
		FileReader fileReader = null;
		try {
			File file = new File("../webapps/dc/WEB-INF/classes/dcLog.log");
			fileReader = new FileReader(file);
			br = new BufferedReader(fileReader);
			String newLine = null;
			while ((newLine = br.readLine()) != null) {
				System.out.println(newLine);
			}
			br.close();
			fileReader.close();
		} catch (Exception e) {
			System.out.println("找不到系统日志文件！");
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileReader != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 说明：获取两个日期时间的时间小时差值
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
	public static long getDistanceOfHour(Date one, Date two) {
		// 判空
		if (one == null || two == null) {
			return -1;
		}

		long hour = 0;
		try {
			long time1 = one.getTime();
			long time2 = two.getTime();
			long diff;
			if (time1 < time2) {
				diff = time2 - time1;
			} else {
				diff = time1 - time2;
			}

			hour = diff / (60 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hour; // 
	}

	/**
	 * 根据字符串数组生成单引号连接的字符串
	 * 
	 * @param strs
	 * @return
	 */
	public static String getStr(String[] strs) {
		StringBuffer strbuf = new StringBuffer();
		int len = strs.length;
		for (int i = 0; i < strs.length; i++) {
			if (i == len - 1) {
				strbuf.append("'" + strs[i] + "'");
			} else {
				strbuf.append("'" + strs[i] + "',");
			}
		}
		return strbuf.toString();
	}
	
	
	/**
	 * 判断车牌前缀是否包含"蒙"
	 * 
	 * @param strs
	 * @return
	 */
	public static boolean getMeng(String[] strs) {
		boolean bl=false;
	
		for (int i = 0; i < strs.length; i++) {
			if (strs[i].equals("蒙")) {
				bl=true;
			} 
		}
		return bl;
	}

	/**
	 * 根据开始截止时间生成SB表SQL列表
	 * 
	 * @param beginTime
	 *            开始时间
	 * @param endTime
	 *            截止时间
	 * @return 识别表组成的列表
	 */
	@SuppressWarnings({ "unchecked", "deprecation" })
	public List getSbList(Date beginTime, Date endTime) {
		List list = new ArrayList();
//		DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int bMon = beginTime.getMonth() + 1;
		int eMon = endTime.getMonth() + 1;
		int bDay = beginTime.getDate();
		int eDay = endTime.getDate();

		Calendar cal = Calendar.getInstance();
		cal.setTime(beginTime);
		int maxDate = cal.getActualMaximum(Calendar.DATE);
//		int minDate = cal.getActualMinimum(Calendar.DATE);
		if (bMon == eMon) {// 同一月份
			for (int i = bDay; i <= eDay; i++) {
				list.add("SB" + i + " s where s.fqh = '" + bMon % 6 + "' ");
			}
		} else {// 不在同一个粤，从开始时间到本月最大日，下一月的开始日到截止日
			for (int i = bDay; i <= maxDate; i++) {
				list.add("SB" + i + " s  where s.fqh = '" + bMon % 6 + "' ");
			}
			for (int i = 1; i <= eDay; i++) {
				list.add("SB" + i + " s  where s.fqh = '" + eMon % 6 + "' ");
			}
		}
		return list;
	}
	
	/**
	 * 
	 * @param filePath
	 *            文件存放路径
	 * @param conent
	 *            写入内容
	 */
	public static void writerTXT(String filePath, String conent) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		conent = conent + "--" + sdf2.format(new Date());
		try {
			File file = new File(filePath);// 文件路径
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fileWriter);
			bw.newLine();
			bw.write(new String(conent.getBytes("UTF-8"), System.getProperty("file.encoding")));
			fileWriter.flush();
			bw.close();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取两个时间段之间的日期集合<br>
	 * @param startDate 开始时间 格式 "yyyy-MM-dd HH:mm:ss"<br>
	 * @param endDate   截止时间
	 * @return
	 */
	public static Set<String> process(String startDate, String endDate) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			startDate = sdf.format(format.parse(startDate));
			endDate = sdf.format(format.parse(endDate));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		Set<String> al = new HashSet<String>();
		if (startDate.equals(endDate)) {
			// IF起始日期等于截止日期,仅返回起始日期一天
			al.add(startDate);
		} else if (startDate.compareTo(endDate) < 0) {
			// IF起始日期早于截止日期,返回起止日期的每一天
			while (startDate.compareTo(endDate) < 0) {
				al.add(startDate);
				try {
					Long l = sdf.parse(startDate).getTime();
					startDate = sdf.format(l + 3600 * 24 * 1000);// +1天
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			al.add(startDate);
		} else {
			// IF起始日期晚于截止日期,仅返回起始日期一天
			al.add(startDate);
		}
		return al;
	}
	/**
	 * 查看某一个图片连接是否存在
	 * @param pName
	 * @return
	 */
	public static  boolean exists(String pName){
		try {
			HttpURLConnection con = (HttpURLConnection) new 
									URL(pName).openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode()==HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			return false;
		}
	}
}
