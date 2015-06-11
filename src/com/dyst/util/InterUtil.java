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
 * �ӿ��г��÷���
 */
public class InterUtil {

	/**
	 * @param str
	 *            ��Ҫ���˵��ַ���
	 * @return ���˺���ַ�
	 */
	public static String keyWordFilter(String str) {
		// ��#�ָ�ؼ���
		if ("".equals(str) || str == null) {
			return "";
		}
		// System.out.println(str);
		String model_str = "%#and#exec#insert#select#delete#update#count#*#'#chr#mid#master#truncate#char#declare#;#or#-#+#,#<#># ";
		String model_split_str[] = model_str.split("#");
		for (int i = 0; i < model_split_str.length; i++) {
			if (str.indexOf(model_split_str[i]) >= 0) {// >=0˵���йؼ��֣�����˵��û�йؼ���
				str = str.replace(model_split_str[i], "");
			}
		}
		return str;
	}

	/**
	 * ��������ת���ɺ��ֳ�����ɫ
	 * 
	 * @param cplx
	 * @return
	 */
	public static String cplxTrans(String cplx) {
		String getCplx = "&nbsp;";
		if (cplx == null) {
			getCplx = "";
		} else {
			Integer cp1 = Integer.parseInt(cplx);// �ѳ�������ת���ɶ�Ӧ����
			switch (cp1) {
			case 0:
				getCplx = "����";
				break;
			case 1:
				getCplx = "����";
				break;
			case 2:
				getCplx = "����";
				break;
			case 3:
				getCplx = "�»���";
				break;
			case 4:
				getCplx = "��ɫ����";
				break;
			case 5:
				getCplx = "����";
				break;
			case 6:
				getCplx = "����";
				break;
			case 7:
				getCplx = "�»�ɫ����";
				break;
			case 8:
				getCplx = "�侯";
				break;
			case 9:
				getCplx = "�°���";
				break;
			default:
				getCplx = "";
			}
		}
		return getCplx;
	}

	/**
	 * ������������id��ѯӦ�������ĸ����ط�Χ
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
			if (lsbmid.equals("440300")) {// ��ȡ���ز�����Ϣ�����ݵ�½��Ա��Ϣ
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
		 * ���ݲ���cplx�õ�ʡ���ĳ�����ɫ���� ʡ���ģ�0-��ɫ��1-��ɫ��2-��ɫ��3-��ɫ��4-������ɫ ���أ�==ȫ��== ���� 0 ����
		 * 1 ���� 2 �»��� 3 ��ɫ���� 4 ���� 5 ���� 6 �»�ɫ���� 7 �侯 8 �°���- 9
		 */
		String cpys = "";
		if ("0".equals(cplx)) {
			cpys = "��ɫ";
		} else if ("2".equals(cplx)) {
			cpys = "��ɫ";
		} else if ("9".equals(cplx)) {
			cpys = "��ɫ";
		} else if ("1".equals(cplx)) {
			cpys = "��ɫ";
		} else {
			cpys = "������ɫ";
		}
		return cpys;
	}

	public static String cplxTOStcpys(String cplx) {
		/*
		 * ���ݲ���cplx�õ�ʡ���ĳ�����ɫ���� ʡ���ģ�0-��ɫ��1-��ɫ��2-��ɫ��3-��ɫ��4-������ɫ ���أ�==ȫ��== ���� 0 ����
		 * 1 ���� 2 �»��� 3 ��ɫ���� 4 ���� 5 ���� 6 �»�ɫ���� 7 �侯 8 �°���- 9
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
	 * ��������ת���ɳ�������
	 * 
	 * @param hpzl
	 * @return
	 * @throws Exception
	 */
	public static String HpzlToCplx(String hpzl) throws Exception {
		/*
		 * ���ݲ���cplx�õ�ʡ���ĳ�����ɫ���� ʡ���ģ�0-��ɫ��1-��ɫ��2-��ɫ��3-��ɫ��4-������ɫ ���أ�==ȫ��== ���� 0 ����
		 * 1 ���� 2 �»��� 3 ��ɫ���� 4 ���� 5 ���� 6 �»�ɫ���� 7 �侯 8 �°���- 9
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
	 * ���ݳ������ͻ�ȡ���Ӧ�ĺ�������
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
	 * ��ȡ������ǰϵͳʱ��
	 * 
	 * @return
	 */
	public static String getTime() {
		/*
		 * ��ȡ��ǰʱ����ַ���ֵ
		 */
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		java.util.Date date = new Date();
		String time = dateFormat.format(date);
		return time;
	}

	/**
	 * ��ʽ��ʱ��
	 * 
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date) {
		/*
		 * ʱ���ʽ��������
		 */
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String time = dateFormat.format(date);
		return time;
	}

	/**
	 * ��ȡ��ǰʱ��ǰn���ʱ��
	 * 
	 * @param n
	 * @return
	 */
	public static String getTime(int n) {
		/*
		 * ��ȡ��ǰʱ���ǰn���ʱ��
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -n);
		String mDateTime = sdf.format(c.getTime());
		// String strStart=mDateTime.substring(0,19);
		return mDateTime;
	}
	 
	/**
	 * ��ȡbeginTime ʱ����ǰn���ʱ��
	 * 
	 * @param n
	 * @return
	 */
	public static String getTimeGjcx(int n, Date beginTime) {
		/*
		 * ��ȡbeginTimeʱ���ǰ�����ʱ���
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
	 * ���ݴ������xh�ĳ��Ȳ�����ʮλʱ������ʮλ ������ǰ�˲���
	 * 
	 * @param xh
	 * @return
	 */
	public static String getXh(String xh) {
		/*
		 * ���ݴ���Ĳ������ȣ�����ʮλ����ǰ�油0.����ʮλ�����ַ�����
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
	 * ���ݴ����xh����4λʱ����4λ��ǰ�˲���
	 * 
	 * @param xh
	 * @return
	 */
	public static String getLsh(String xh)// ��ˮ��
	{
		/*
		 * ��������ʱ���õķ���
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
		 * ����ַ���Ϊ�գ���ѡ����ַ���������,ʹ�ַ�����Ϊnull Ŀǰ���Բ�ʹ�ø÷�����ʹ��StringUtils����
		 */
		if (text != null) {
			return text;
		}
		return "";
	}

	/**
	 * ͨ��HttpServletRequest����IP��ַ
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
	 * �ֻ�����У�� �Ƿ���true ,���Ƿ��� false;
	 * 
	 * @param mobiles
	 *            �ֻ�����
	 * @return У����
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
		return min + "��" + sec + "��"; // day + "��" + hour + "Сʱ" +
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
		return day + "��" + hour + "Сʱ" + min + "��" + sec + "��";
	}

	/**
	 * ����ʱ�����
	 * 
	 * @return ����ֵΪ������, ��
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

		return min + "����" + min + "��";
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
			System.out.println("�Ҳ���ϵͳ��־�ļ���");
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
	 * ˵������ȡ��������ʱ���ʱ��Сʱ��ֵ
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
	public static long getDistanceOfHour(Date one, Date two) {
		// �п�
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
	 * �����ַ����������ɵ��������ӵ��ַ���
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
	 * �жϳ���ǰ׺�Ƿ����"��"
	 * 
	 * @param strs
	 * @return
	 */
	public static boolean getMeng(String[] strs) {
		boolean bl=false;
	
		for (int i = 0; i < strs.length; i++) {
			if (strs[i].equals("��")) {
				bl=true;
			} 
		}
		return bl;
	}

	/**
	 * ���ݿ�ʼ��ֹʱ������SB��SQL�б�
	 * 
	 * @param beginTime
	 *            ��ʼʱ��
	 * @param endTime
	 *            ��ֹʱ��
	 * @return ʶ�����ɵ��б�
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
		if (bMon == eMon) {// ͬһ�·�
			for (int i = bDay; i <= eDay; i++) {
				list.add("SB" + i + " s where s.fqh = '" + bMon % 6 + "' ");
			}
		} else {// ����ͬһ�������ӿ�ʼʱ�䵽��������գ���һ�µĿ�ʼ�յ���ֹ��
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
	 *            �ļ����·��
	 * @param conent
	 *            д������
	 */
	public static void writerTXT(String filePath, String conent) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		conent = conent + "--" + sdf2.format(new Date());
		try {
			File file = new File(filePath);// �ļ�·��
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
	 * ��ȡ����ʱ���֮������ڼ���<br>
	 * @param startDate ��ʼʱ�� ��ʽ "yyyy-MM-dd HH:mm:ss"<br>
	 * @param endDate   ��ֹʱ��
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
			// IF��ʼ���ڵ��ڽ�ֹ����,��������ʼ����һ��
			al.add(startDate);
		} else if (startDate.compareTo(endDate) < 0) {
			// IF��ʼ�������ڽ�ֹ����,������ֹ���ڵ�ÿһ��
			while (startDate.compareTo(endDate) < 0) {
				al.add(startDate);
				try {
					Long l = sdf.parse(startDate).getTime();
					startDate = sdf.format(l + 3600 * 24 * 1000);// +1��
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			al.add(startDate);
		} else {
			// IF��ʼ�������ڽ�ֹ����,��������ʼ����һ��
			al.add(startDate);
		}
		return al;
	}
	/**
	 * �鿴ĳһ��ͼƬ�����Ƿ����
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
