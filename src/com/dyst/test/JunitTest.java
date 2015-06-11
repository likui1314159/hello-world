package com.dyst.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.dyst.util.InterUtil;
import com.dyst.webservice.InAccessServiceImpl;

public class JunitTest {
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	@Test
	public void Testgj() {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date1 = new Date();
    	System.out.println("初始化时间："+sdf.format(date1));
		// xml报文
    	String str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
    		+ "<head><pagesize>13</pagesize><from>0</from></head><body><data>"
			+ "<hphm></hphm><kssj>2014-2-10 13:13:59</kssj><jssj>2014-2-10 13:14:12</jssj>"
			+ "<tpid></tpid><jcdid></jcdid><hpzl></hpzl><cplx></cplx><hmdCphm></hmdCphm></data></body></root>";
    	
//		//模糊查询测试
//    	 str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
//    		+ "<head><pagesize>30</pagesize><from>0</from></head><body><data>"
//			+ "<hphm>粤BH278D</hphm><kssj>2012-01-01 00:00:00</kssj><jssj>2013-04-18 13:14:12</jssj>"
//			+ "<tpid></tpid><jcdid></jcdid><hpzl></hpzl><cplx></cplx></data></body></root>";
//    	 
//    	 //跨库测试查询
//    	 str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
//     		+ "<head><pagesize>12</pagesize><from>0</from></head><body><data>"
// 			+ "<hphm>粤BG553Q</hphm><kssj>2013-01-01 00:00:00</kssj>" +
// 					"<jssj>2013-04-18 13:14:12</jssj>"
// 			+ "<tpid></tpid><jcdid></jcdid><hpzl></hpzl><cplx></cplx></data></body></root>";
    	//
		InAccessServiceImpl impl = new InAccessServiceImpl();
		System.out.println(impl.executes("01", "01", "hello,world","1" ,str_xml));
		Date date2 = new Date();
		System.out.println("查询结束时间："+sdf.format(date2));
		double d = (date2.getTime()-date1.getTime());
		System.out.println(d/1000);
	}
	
	public static void main(String[] args)
	{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date1 = new Date();
    	System.out.println("初始化时间："+sdf.format(date1));
		// xml报文
    	String str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
    		+ "<head><pagesize>13</pagesize><from>0</from></head><body><data>"
			+ "<hphm></hphm><kssj>2013-4-10 13:13:59</kssj><jssj>2013-4-10 13:14:12</jssj>"
			+ "<tpid></tpid><jcdid></jcdid><hpzl></hpzl><cplx></cplx></data></body></root>";
    	
		//模糊查询测试
    	 str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
    		+ "<head><pagesize>30</pagesize><from>0</from></head><body><data>"
			+ "<hphm>粤BH278D</hphm><kssj>2012-01-01 00:00:00</kssj><jssj>2013-04-18 13:14:12</jssj>"
			+ "<tpid></tpid><jcdid>10100210</jcdid><hpzl></hpzl><cplx></cplx></data></body></root>";
    	 
    	 //跨库测试查询
    	 str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
     		+ "<head><pagesize>500</pagesize><from>0</from></head><body><data>"
 			+ "<hphm></hphm><kssj>2012-01-01 00:00:00</kssj>" +
 					"<jssj>2012-04-18 13:14:12</jssj>"
 			+ "<tpid></tpid><jcdid>10100210</jcdid><hpzl></hpzl><cplx></cplx></data></body></root>";
    	//
		InAccessServiceImpl impl = new InAccessServiceImpl();
		System.out.println(impl.executes("01", "01", "hello,world","1" ,str_xml));
		Date date2 = new Date();
		System.out.println("查询结束时间："+sdf.format(date2));
		double d = (date2.getTime()-date1.getTime());
		System.out.println(d/1000);
		
//		System.out.println(CopyOfESClient.getInstance().client);
//		System.out.println(CopyOfESClient.getInstance().hashCode());
	}
	@Test
	public void TestPic() {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date1 = new Date();
    	System.out.println("初始化时间："+sdf.format(date1));
		// xml报文
    	String str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
    		+ "<head></head><body>" +
//    				"<tpid>20130419000003002050200321</tpid>" +
//    				"<tpid>20130419000003002050200321</tpid>" +
//    				"<tpid>20130419000003002050200321</tpid>" +
    				"<tpid>20130419000004752040080131</tpid>" +
    				"<tpid>20130419000004752040080131</tpid>" +
    				"<tpid>20130419000004752040080131</tpid>" +
    				"<tpid>20130419000003002050200321</tpid>" +
    				"</body></root>";
		InAccessServiceImpl impl = new InAccessServiceImpl();
		System.out.println(impl.executes("01", "02", "hello,world","1" ,str_xml));
		Date date2 = new Date();
		System.out.println("查询结束时间："+sdf.format(date2));
		double d = (date2.getTime()-date1.getTime());
		System.out.println(d/1000);
	}
	@Test
	public void TestUtils() {
//		String [] str = "12,".split(",",2);
//		for (String string : str) {
//			System.out.println(string+"--------");
//		}
//		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
//		SimpleDateFormat sdf2 = new SimpleDateFormat("dd");
//		 //获取天
////        List<String> allDay = InterUtil.getDayBetweenTwo("2014-4-15", 
////        		"2014-4-17");
//        Set<String> daySet = new HashSet<String>();//保存天，避免重复
//        String day = "";
//		try {
//			//摘取天
//		    for(String d : allDay){
//		    	daySet.add(sdf2.format(sdf1.parse(d)));
//		    }
//		    //组装
//		    for(String d : daySet){
//		    	day += Integer.parseInt(d) + ",";
//		    }
//		    
//			if(day.length() > 0){
//				day = day.substring(0, day.length() - 1);
//			}
//			System.out.println(day);
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		
	}
	@Test
	public void TestPicSocket() throws ParseException{
	 System.out.println(sdf.format(new Date(1396225203000l)));
	 //100.100.7.51 9002
	 //数据字典 0045 代码
		
		
	}
	
	
}
