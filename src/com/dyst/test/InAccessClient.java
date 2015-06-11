package com.dyst.test;

import org.junit.Test;
import java.net.URL;
import org.codehaus.xfire.client.Client;

import com.dyst.util.InterUtil;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InAccessClient { 
	@Test
	// 交警测试
	public void TestGJ() throws InterruptedException {
		Date date1 = null;
		date1 = new Date();
		String str_xml ="";
//		Service srvcModel = new ObjectServiceFactory()
//				.create(IInAccessService.class);
		// xml报文
//    	str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
//    		+ "<head><pagesize>13</pagesize><from>0</from></head><body><data>"
//			+ "<hphm></hphm><kssj>2013-4-10 13:13:59</kssj><jssj>2013-4-10 13:14:12</jssj>"
//			+ "<tpid></tpid><jcdid></jcdid><hpzl></hpzl><cplx></cplx></data></body></root>";
//    	
//		//模糊查询测试
//    	蒙CY0558,蒙CTH588
//    	 str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
//    		+ "<head><pagesize>30</pagesize><from>0</from></head><body><data>"
//			+ "<hphm>粤BH278D</hphm><kssj>2012-01-01 00:00:00</kssj><jssj>2013-04-18 13:14:12</jssj>"
//			+ "<tpid></tpid><jcdid></jcdid><hpzl></hpzl><cplx></cplx></data></body></root>";
    	 
    	 //跨库测试查询
		 str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
    		+ "<head><pagesize>20</pagesize><from>0</from><sort>tgsj</sort><sortType>DESC</sortType></head><body><data>"
			+ "<hphm></hphm><kssj>2014-07-01 00:00:00</kssj><jssj>2014-07-01 02:00:00</jssj>"
			+ "<tpid></tpid><jcdid></jcdid><hpzl></hpzl><cplx></cplx>" +
					"<cd></cd><cb></cb><sd></sd><hmdCphm></hmdCphm>" +
					"</data></body></root>";
    	
//    	 str_xml ="<?xml version=\"1.0\" encoding=\"GB2312\"?>" +
//    			"<root><head><pagesize>20</pagesize><from>0</from></head>" +
//    			"<body><data><hphm>粤A</hphm>" +
//    			"<kssj>2013-07-01 23:55:00</kssj><jssj>2013-07-06 00:05:00</jssj>" +
//    			"<tpid></tpid><jcdid></jcdid><hpzl></hpzl><cplx></cplx></data></body></root>";
    	 Client client = null;
 		try {
 			String ip=""; 
//			ip="http://100.100.37.37:8989/dyst/services/InAccess?wsdl";//本地服务
//			ip="http://100.100.37.37:8989/dyst/services/InAccess?wsdl";//本地服务
			ip="http://100.100.3.135:8080/dyst/services/InAccess?wsdl";//本地服务
//			ip="http://10.103.1.63:8080/dyst/services/InAccess?wsdl";//乌海服务 
//			ip="http://100.100.37.38:8080/dyst/services/InAccess?wsdl";//深圳服务
			
 			client = new Client(new URL(ip));
// 			date1 = new Date();
 			//建立Webservice耗时
 			Date date2 = new Date();
// 			System.out.println("建立Webservice耗时："+sdf.format(date2));
 			double d = (date2.getTime()-date1.getTime());
 			System.out.println("建立Webservice耗时："+d/1000);
 			date1 = new Date();
 			Object[] s = client.invoke("executes", new String[] {"01", "01", "hello,world", "1", str_xml});
 			System.out.println(s[0]);
 			date2 = new Date();
 			d = (date2.getTime() - date1.getTime());
 			System.out.println("查询Webservice耗时：" + d/1000);
 			
 			//
// 			date1 = new Date();
// 			ip="http://100.100.3.135:8080/dyst/services/InAccess?wsdl";//本地服务
// 			client = new Client(new URL(ip));
// 			Object[] s0 = client.invoke("executes", new String[] {"01", "01", "hello,world", "0", str_xml});
// 			System.out.println(s0[0]);
// 			date2 = new Date();
// 			d = (date2.getTime() - date1.getTime());
// 			System.out.println("37.35 dyst查询Webservice耗时：" + d/1000);
// 			
// 			date1 = new Date();
// 			ip="http://100.100.3.135:8080/dystField/services/InAccess?wsdl";//本地服务
// 			client = new Client(new URL(ip));
// 			Object[] s1 = client.invoke("executes", new String[] {"01", "01", "hello,world", "0", str_xml});
// 			System.out.println(s1[0]);
// 			date2 = new Date();
// 			d = (date2.getTime() - date1.getTime());
// 			System.out.println("dystField查询Webservice耗时：" + d/1000);
 			
 			
 			
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}finally{
 			if(client!=null){
 				client.close();
// 				System.out.println("最后执行");
 			}
 		}
//		InAccessServiceImpl impl = new InAccessServiceImpl();
//		System.out.println(impl.executes("01", "01", "hello,world","1" ,str_xml));
//		Thread.sleep(100000);
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
    				"<tpid>20140502225704562010440211</tpid>" +
//    				"<tpid>2014050412003501103A056301</tpid>" +
//    				"<tpid>2014050400031004103A047211</tpid>" +
//    				"<tpid>2014050412031701101A044801</tpid>" +
    				"</body></root>";
    	Client client;
    	String ip=""; 
//			ip ="100.100.21.114";
			ip="http://100.100.37.37:8989/dyst/services/InAccess?wsdl";//本地服务
//			ip="http://10.103.1.63:8080/dyst/services/InAccess?wsdl";//乌海服务
//			ip="http://100.100.37.34:80/dyst/services/InAccess?wsdl";//深圳服务
			
			try {
				client = new Client(new URL(ip));
				date1 = new Date();
				Object[] s = client.invoke("executes", new String[] {"01", "02", "hello,world","1" ,str_xml} );
				System.out.println(s[0]);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		Date date2 = new Date();
		System.out.println("查询结束时间："+sdf.format(date2));
		double d = (date2.getTime()-date1.getTime());
		System.out.println(d/1000);
	}
//	
	@Test
	public void TestSix() {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date1 = new Date();
    	System.out.println("初始化时间："+sdf.format(date1));
    	Client client;
    	String ip=""; 
			ip="http://10.103.1.63:8080/Six/service/ReaderService?wsdl";//深圳服务
			
			try {
				client = new Client(new URL(ip));
				 date1 = new Date();
				Object[] s = client.invoke("executes", new String[] {"蒙CY1092"} );
				System.out.println(s[0]);
//				System.out.println(s[1]);
				System.out.println(s.length);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		Date date2 = new Date();
		System.out.println("查询结束时间："+sdf.format(date2));
		double d = (date2.getTime()-date1.getTime());
		System.out.println(d/1000);
	}
	@Test
	public void TestUpdateSb() {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date1 = new Date();
    	System.out.println("初始化时间："+sdf.format(date1));
		// xml报文
    	String str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
    		+ "<head></head><body><data>" +
    				"<tpid>20130803000032522050141251</tpid>" +
    				"<hphm>2222222</hphm>" +
    				"<cplx>9</cplx>" +
    				"</data></body></root>";
    	Client client;
    	String ip=""; 
//			ip ="100.100.21.114";
//			ip="http://100.100.3.75:8080/dyst/services/InAccess?wsdl";//本地服务
//			ip="http://10.103.1.63:8080/dyst/services/InAccess?wsdl";//乌海服务
//			ip="http://100.100.37.34:80/dyst/services/InAccess?wsdl";//深圳服务
			
			try {
				client = new Client(new URL(ip));
				 date1 = new Date();
				Object[] s = client.invoke("executes", new String[] {"01", "06", "hello,world","0" ,str_xml} );
				System.out.println(s[0]);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		Date date2 = new Date();
		System.out.println("查询结束时间："+sdf.format(date2));
		double d = (date2.getTime()-date1.getTime());
		System.out.println(d/1000);
	}
	@Test
	public void TestSzPicCall() {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date1 = new Date();
    	System.out.println("初始化时间："+sdf.format(date1));
		// xml报文
    	String str="2014050400160001104A008411";
    	Client client = null;
    	String ip="http://100.100.36.201:8080/picCall/Service.asmx?WSDL"; 
			try {
				client = new Client(new URL(ip));
				 date1 = new Date();
				Object[] s = client.invoke("PicCall2", new String[] {str} );
				System.out.println(s[0]);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		Date date2 = new Date();
		System.out.println("查询结束时间："+sdf.format(date2));
		double d = (date2.getTime()-date1.getTime());
		System.out.println(d/1000);
	}
	@Test
	public void dateStringToLong() throws Exception{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(sdf.parse("2013-08-07 23:55:00").getTime());
		System.out.println(sdf.parse("2013-08-10 00:05:00").getTime());
		System.out.println("20121204000000452050140951".substring(0, 8));
		
	}
	
	@Test
	public void dateStringToLong2() throws Exception{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf = new SimpleDateFormat("dd");
		Date midDate = df.parse(InterUtil.getTime(Integer.parseInt("0")));
		System.out.print(sdf.format(midDate));
	}
	@Test
	// 统计测试
	public void TestTj() throws InterruptedException {
		Date date1 = null;
		date1 = new Date();
		String str_xml ="";
    	 //跨库测试查询
		 str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root>"
    		+ "<head><groupName>jcdid</groupName><type>01</type><sbzt>1</sbzt></head><body>" +
    				"<data><hphm></hphm><kssj>2014-4-8 09:13:59</kssj>" +
    				"<jssj>2014-4-10 09:14:12</jssj>"
			+ "<tpid></tpid><jcdid></jcdid><hpzl></hpzl><cplx>0</cplx>" +
					"<cd></cd><cb></cb><sd></sd><hmdCphm></hmdCphm>" +
					"</data></body></root>";
    	
    	Client client = null;
 		try {
 			String ip=""; 
			ip="http://100.100.3.135:8080/dystField/services/InAccess?wsdl";//本地服务
			
 			client = new Client(new URL(ip));
// 			date1 = new Date();
 			//建立Webservice耗时
 			Date date2 = new Date();
// 			System.out.println("建立Webservice耗时："+sdf.format(date2));
 			double d = (date2.getTime()-date1.getTime());
 			System.out.println("建立Webservice耗时："+d/1000);
 			date1 = new Date();
 			Object[] s = client.invoke("executes", new String[] {"01", "08", "hello,world", "0", str_xml});
 			System.out.println(s[0]);
 			date2 = new Date();
 			d = (date2.getTime() - date1.getTime());
 			System.out.println("查询Webservice耗时：" + d/1000);
 			
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}finally{
 			if(client!=null){
 				client.close();
// 				System.out.println("最后执行");
 			}
 		}
//		InAccessServiceImpl impl = new InAccessServiceImpl();
//		System.out.println(impl.executes("01", "01", "hello,world","1" ,str_xml));
//		Thread.sleep(100000);
	}
	@Test
	// 碰撞分析测试
	public void TestPzfx() throws InterruptedException {
		Date date1 = null;
		date1 = new Date();
		String str_xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root><head>" +
		"<frequency>0.5</frequency><maxReturnRecord>10000</maxReturnRecord></head>" +
		"<body>" +
		"<data><jcdid>20501810</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-25 13:00:00</jssj></data>" +
		"<data><jcdid>10300607</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-25 13:00:00</jssj></data>" +
		"<data><jcdid>10100609</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-25 13:00:00</jssj></data>" +
		"<data><jcdid>10100611</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-25 13:00:00</jssj></data>" +
		"<data><jcdid>10200613</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-25 13:00:00</jssj></data>" +
		"<data><jcdid>10300207</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-25 13:00:00</jssj></data>" +
		"<data><jcdid>10100603</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-25 13:00:00</jssj></data>" +
		"</body></root>";
    	
    	Client client = null;
 		try {
 			String ip=""; 
			ip="http://100.100.3.135:8080/dyst/services/InAccess?wsdl";//本地服务
			
 			client = new Client(new URL(ip));
 			date1 = new Date();
// 			建立Webservice耗时
 			Date date2 = new Date();
// 			System.out.println("建立Webservice耗时："+sdf.format(date2));
 			double d = (date2.getTime()-date1.getTime());
 			System.out.println("建立Webservice耗时："+d/1000);
 			date1 = new Date();
 			Object[] s = client.invoke("executes", new String[] {"01", "10", "hello,world", "0", str_xml});
 			System.out.println(s[0]);
 			date2 = new Date();
 			d = (date2.getTime() - date1.getTime());
 			System.out.println("查询Webservice耗时：" + d/1000);
 			
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}finally{
 			if(client!=null){
 				client.close();
// 				System.out.println("最后执行");
 			}
 		}
//		InAccessServiceImpl impl = new InAccessServiceImpl();
//		System.out.println(impl.executes("01", "01", "hello,world","1" ,str_xml));
//		Thread.sleep(100000);
	}
	@Test
	// 频繁出现点分析测试
	public void TestFrequence() throws InterruptedException {
		Date date1 = null;
		date1 = new Date();
		String requestXml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root><head><maxReturnCount>20</maxReturnCount>" +
		"</head><body><data><hphm>粤B029PL</hphm><kssj>2014-07-10 09:00:00</kssj>" +
		"<jssj>2014-07-11 14:55:00</jssj></data></body></root>";
    	
    	Client client = null;
 		try {
 			String ip=""; 
			ip="http://100.100.3.135:8080/dyst/services/InAccess?wsdl";//本地服务
			
 			client = new Client(new URL(ip));
 			date1 = new Date();
// 			建立Webservice耗时
 			Date date2 = new Date();
// 			System.out.println("建立Webservice耗时："+sdf.format(date2));
 			double d = (date2.getTime()-date1.getTime());
 			System.out.println("建立Webservice耗时："+d/1000);
 			date1 = new Date();
 			Object[] s = client.invoke("executes", new String[] {"01", "09", "hello,world", "0", requestXml});
 			System.out.println(s[0]);
 			date2 = new Date();
 			d = (date2.getTime() - date1.getTime());
 			System.out.println("查询Webservice耗时：" + d/1000);
 			
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}finally{
 			if(client!=null){
 				client.close();
// 				System.out.println("最后执行");
 			}
 		}
//		InAccessServiceImpl impl = new InAccessServiceImpl();
//		System.out.println(impl.executes("01", "01", "hello,world","1" ,str_xml));
//		Thread.sleep(100000);
	}
}
