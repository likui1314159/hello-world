package com.dyst.util;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.facet.terms.TermsFacet;

import com.dyst.entites.PfTemp;
import com.dyst.entites.SbC;
import com.dyst.entites.SbPz;
import com.dyst.entites.Sbnew;
import com.dyst.oracle.JjhomdOracle;

@SuppressWarnings("unchecked")
public class XmlCreater {
	/**
	 * 根据参数List,转换成通行数据XML文档字符串
	 * @param list
	 * @return XML生成文件
	 */
	public String createXml(String homdFlag, List list, SearchHits hits){
		/*
		 * 根据list值创建XML文件，最后转换成字符串，返回
		 */
//		Date date1 = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		//创建xml文件
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_rowdata1 = doc.addElement("root");
		//添加head元素
		Element el_head = el_rowdata1.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
	    Element el_message = el_head.addElement("message");
	    el_success.setText("1");
	    
	    //数据总数
	    int lenES = 0;
	    int lenOralce = 0;
	    if(hits != null){//ES 库查询总数
//	    	lenES = hits.getTotalHits();//获取总数
	    	lenES = hits.getHits().length;
	    }
	    if(list != null && list.size() > 0){//Oralce库查询总数
	    	lenOralce = list.size();
	    }
	    el_count.setText("" + (lenOralce + lenES));
	    el_message.setText("查询成功");
	    
	    //为body元素赋值
	    Element  el_body = el_rowdata1.addElement("body");
	    Sbnew sb = new Sbnew();
	    //Oralce库查询结果集
		if(list != null && list.size() > 0) {
			//如果返回数据为空，或者失败，则设置count为0，message为数据库连接失败
			//下面为body节点的数据	
			for(int i = 0;i < list.size();i++) {	
				//tx=(Txsjcx)list.get(i);//旧的实现方式
				sb = (Sbnew)list.get(i);
				
				//判断是否隐藏红名单，如果隐藏红名单，则更换数据
				Integer tpzs = sb.getTpzs();
				//homdFlag为null或为空或0时，只要该车牌为红名单，则隐藏
				if((homdFlag == null || "".equals(homdFlag) || !"0".equals(homdFlag)) 
						&& JjhomdOracle.hideJjhomd(sb.getCphm1(), sb.getCplx1())){//隐藏
					sb = new Sbnew();
					sb.setCphm1("******");
					sb.setCplx1("******");
					sb.setJcdid("******");
					sb.setCdid("******");
					sb.setTgsj(null);
					sb.setTpzs(tpzs);
					sb.setTpid1("00000000000000000000000000000000000");
					sb.setTpid2("");
					sb.setTpid3("");
					sb.setTpid4("");
					sb.setTpid5("");
					sb.setCb("******");
					sb.setSd(null);
				}
				
				Element el_data = el_body.addElement("data");
				Element el_hphm = el_data.addElement("hphm");
				Element el_cplx = el_data.addElement("cplx");
				Element el_jcdid = el_data.addElement("jcdid");
				Element el_cdid = el_data.addElement("cdid");
				Element el_sbsj = el_data.addElement("sbsj");
//				Element el_jcdmc = el_data.addElement("jcdmc");
//				Element el_cllx = el_data.addElement("cllx");
				Element el_tpzs = el_data.addElement("tpzs");
				Element el_tp1 = el_data.addElement("tp1");
			    Element el_tp2 = el_data.addElement("tp2");
				Element el_tp3 = el_data.addElement("tp3");
				Element el_tp4 = el_data.addElement("tp4");
				Element el_tp5 = el_data.addElement("tp5");
//				Element el_clxs = el_data.addElement("clxs");
				Element el_cb = el_data.addElement("cb");
				Element el_sd = el_data.addElement("sd");
				    	
//				//赋值
//				el_hphm.setText(tx.getCphid());
//				el_sbsj.setText(sdf.format(tx.getSbsj()));
//				el_jcdid.setText(tx.getJcdid());
//				el_jcdmc.setText("");//监测点名称
//				el_cplx.setText(tx.getCplx());
//				el_tp1.setText(tx.getTpid1());
//				el_tp2.setText("");
//				el_cdid.setText(tx.getCdid());
				//赋值,一张表实现方式
				el_hphm.setText(sb.getCphm1());
				el_cplx.setText(sb.getCplx1());
				
				el_jcdid.setText(sb.getJcdid());
//				el_jcdmc.setText("");//监测点名称
				el_cdid.setText(sb.getCdid());
				
				Timestamp tgsj = sb.getTgsj();
				el_sbsj.setText(tgsj == null? "":sdf.format(tgsj));
				
				el_tpzs.setText("" + sb.getTpzs());//图片张数
				String tpid1 = sb.getTpid1();
				String tpid2 = sb.getTpid2();
				String tpid3 = sb.getTpid3();
				String tpid4 = sb.getTpid4();
				String tpid5 = sb.getTpid5();
				el_tp1.setText(tpid1 == null? "":tpid1);
				el_tp2.setText(tpid2 == null? "":tpid2);
				el_tp3.setText(tpid3 == null? "":tpid3);
				el_tp4.setText(tpid4 == null? "":tpid4);
				el_tp5.setText(tpid5 == null? "":tpid5);
				
				String cb = sb.getCb();
				el_cb.setText(cb == null? "":cb);
				
				Double sd = sb.getSd();
				el_sd.setText(sd == null || sd < 0 ? "":sd.toString());
			}
		}
		
		//ES库查询结果集
		if(lenES > 0){
			Map<String, String> map = null;
 			for(final SearchHit hit:hits){
				try {
					Element el_data=el_body.addElement("data");
					Element el_hphm=el_data.addElement("hphm");
					Element el_cplx=el_data.addElement("cplx");
					Element el_jcdid=el_data.addElement("jcdid");
					Element el_cdid=el_data.addElement("cdid");
					Element el_sbsj=el_data.addElement("sbsj");
//					Element el_jcdmc=el_data.addElement("jcdmc");
		//		    Element el_cllx=el_data.addElement("cllx");
					Element el_tpzs=el_data.addElement("tpzs");
				    Element el_tp1=el_data.addElement("tp1");
				    Element el_tp2=el_data.addElement("tp2");
				    Element el_tp3=el_data.addElement("tp3");
				    Element el_tp4=el_data.addElement("tp4");
				    Element el_tp5=el_data.addElement("tp5");
		//		    Element el_clxs=el_data.addElement("clxs");
				    Element el_cb = el_data.addElement("cb");
					Element el_sd = el_data.addElement("sd");
					
//					System.out.println(hit.getId()+"=="+hit.getScore()+"");
					map = new HashMap<String, String>();
		            final Iterator<SearchHitField> iterator = hit.iterator();	 	
		            while(iterator.hasNext()){//一条记录
		            	final SearchHitField hitfield = iterator.next();
		            	map.put(hitfield.getName(), hitfield.getValue().toString());
//		            	System.out.print(hitfield.getName()+"=="+hitfield.getValue()+"-----");
		            }
		            
		            //判断是否隐藏红名单，如果隐藏红名单，则更换数据
		            String tpzs = (String)map.get("tpzs");
		            //homdFlag为null或为空或0时，只要该车牌为红名单，则隐藏
					if((homdFlag == null || "".equals(homdFlag) || !"0".equals(homdFlag)) 
							&& JjhomdOracle.hideJjhomd((String)map.get("cphm1"), (String)map.get("cplx1"))){//隐藏
						map = new HashMap<String, String>();
						map.put("cphm1", "******");
						map.put("cplx1", "******");
						map.put("jcdid", "******");
						map.put("cdid", "******");
						map.put("tgsj", "");
						map.put("tpid1", "00000000000000000000000000000000000");
						map.put("tpid2", "");
						map.put("tpid3", "");
						map.put("tpid4", "");
						map.put("tpid5", "");
						map.put("cb", "******");
						map.put("sd", "");
						map.put("tpzs", tpzs);
					}
					
			    	el_hphm.setText((String)map.get("cphm1"));
			    	el_cplx.setText((String)map.get("cplx1"));
				    el_jcdid.setText((String)map.get("jcdid"));
				    el_cdid.setText((String)map.get("cdid"));
//				    el_jcdmc.setText("");//监测点名称
				    el_sbsj.setText("".equals(map.get("tgsj")) ? "": 
				    	sdf.format(new Date(Long.parseLong(map.get("tgsj")))));
				   
				    el_tpzs.setText(tpzs == null? "":tpzs);//图片张数;
				    Object tpid1 = map.get("tpid1");
				    Object tpid2 = map.get("tpid2");
				    Object tpid3 = map.get("tpid3");
				    Object tpid4 = map.get("tpid4");
				    Object tpid5 = map.get("tpid5");
				    el_tp1.setText(tpid1 == null? "":(String)tpid1);
				    el_tp2.setText(tpid2 == null? "":(String)tpid2);
				    el_tp3.setText(tpid3 == null? "":(String)tpid3);
				    el_tp4.setText(tpid4 == null? "":(String)tpid4);
				    el_tp5.setText(tpid5 == null? "":(String)tpid5);
				    
				    String cb = (String)map.get("cb");
					el_cb.setText(cb == null? "":cb);
					
					Object sd = map.get("sd");
					el_sd.setText(sd == null? "":sd.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}     				
		}
	
		String str_xml = doc.asXML();//转换成字符串
	//	System.out.println(str_xml);
				
//		Date date2 = new Date();
//		double d = (date2.getTime() - date1.getTime());
//		System.out.println("生成XML文件耗时："+d/1000+"秒");
				
		return str_xml;
	}
	
	/**
	 * 根据oracle和ES查询结果记录总数通过XML形式返回给用户<br>
	 * @param oracleCount： Oracle查询记录总数<br>
	 * @param esCount： ES查询记录总数<br>
	 * @return  xml 报文
	 */
	public String createCountXml(int oracleCount, int esCount){
		/*
		 * 根据list值创建XML文件，最后转换成字符串，返回
		 */
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_rowdata1 = doc.addElement("root");
		//添加head元素
		Element el_head = el_rowdata1.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
	    Element el_message = el_head.addElement("message");
	    
	    el_success.setText("1") ;
	    el_count.setText("" + (oracleCount + esCount));
	    el_message.setText("查询成功");
	    
		String str_xml = doc.asXML();//转换成字符串
		return str_xml;
	}
	
	/**
	 * 查询失败，返回指定错误的xml文件
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public String createErrorXml(String message){
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_rowdata1 = doc.addElement("root");
		// 添加head元素
		Element el_head = el_rowdata1.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
		Element el_message = el_head.addElement("message");

		el_success.setText("0");//失败
		el_count.setText("0");
		el_message.setText(message);//失败信息描述
		String str_xml = doc.asXML();// 转换成字符串
		return str_xml;
	}
	
	/**
	 * 图片查询，返回的xml文件
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public String createPicPath(List<String> listPic){
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_rowdata1 = doc.addElement("root");
		// 添加head元素
		Element el_head = el_rowdata1.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
		Element el_message = el_head.addElement("message");
		Element el_body = el_rowdata1.addElement("body");

		el_success.setText("1");//失败
		el_message.setText("图片地址查询成功");//
		if(listPic == null || listPic.size() == 0){//集合为空
			el_count.setText("0");
			return doc.asXML();// 转换成字符串
		}
		
		//如果获取到结果，则返回查询结果
		el_count.setText("" + listPic.size());//查询总数
		for (int j = 0;j < listPic.size();j++) {
			Element el_path = el_body.addElement("path");
	    	//赋值
			el_path.setText(listPic.get(j));
		}     
		String str_xml = doc.asXML();// 转换成字符串
		// System.out.println(str_xml);
		return str_xml;
	}
	
	/**
	 * 更新识别，返回的xml文件
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public String createUpdateXml(String message){
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_rowdata1 = doc.addElement("root");
		// 添加head元素
		Element el_head = el_rowdata1.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
		Element el_message = el_head.addElement("message");

		el_success.setText("1");//
		el_count.setText("");
		el_message.setText(message);//信息描述
		String str_xml = doc.asXML();// 转换成字符串
		return str_xml;
	}
	
	/**
	 * 统计查询结果返回
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public String createTjxml(TermsFacet tf){
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_root = doc.addElement("root");
		// 添加head元素
		Element el_head = el_root.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
		Element el_message = el_head.addElement("message");
		Element el_body = el_root.addElement("body");

		el_success.setText("1");//
		el_message.setText("统计查询成功");//
//		for(TermsFacet.Entry entry:tf){
////			System.out.print((i++)+"字段值:"+entry.getTerm()+"   ");
////			System.out.println("出现频率:"+entry.getCount());
//		}
		//如果获取到结果，则返回查询结果
		el_count.setText("" + tf.getTotalCount());//查询总数
		
		for(TermsFacet.Entry entry:tf){
			Element el_data = el_body.addElement("data");
			Element groupName = el_data.addElement("groupName");
			Element el_value = el_data.addElement("value");
	    	//赋值
			groupName.setText(""+entry.getTerm());//分组字段值
			el_value.setText(""+entry.getCount());//分组值
		}     
		String str_xml = doc.asXML();// 转换成字符串
		// System.out.println(str_xml);
		return str_xml;
	}
	/**
	 * 车辆频繁出现点分析
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public  String createFrequently(List<SbC> listC,int count,int maxReturnValue){
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_root = doc.addElement("root");
		// 添加head元素
		Element el_head = el_root.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
		Element el_message = el_head.addElement("message");
		Element el_body = el_root.addElement("body");

		el_success.setText("1");//
		el_message.setText("统计查询成功");//
		//如果获取到结果，则返回查询结果
		el_count.setText("" + count);//查询总数
		//单个点在总数中的比率
//		DecimalFormat df = new DecimalFormat("#.00");
//		System.out.println(df.format(d));
		int maxValue = 0;
		for(int i=listC.size()-1;maxValue<maxReturnValue&&i>=0;i--){
			SbC pt = listC.get(i);
			
			Element el_data = el_body.addElement("data");
			Element jcdid = el_data.addElement("jcdid");
			Element frequency = el_data.addElement("frequency");
			Element timeElement = el_data.addElement("time");
			Element ratio = el_data.addElement("ratio");
			Element tpidsElement = el_data.addElement("tpids");
			
		    HashSet<String> set = (HashSet<String>) pt.getSetTpid();//图片id集合
		    
			//赋值
			jcdid.setText(""+pt.getJcdid());//
			//pt.getCount()获取的数与实际可能不符，取图片id的唯一值数
			frequency.setText(""+set.size());//
			//单个点在总出现次数中的比率
//			ratio.setText(df.format(((double)set.size()/(double)count)));
			ratio.setText(String.format("%.2f", ((double)set.size()/(double)count)*100));
//			String.format("%.4f", (21d/22222d)
			timeElement.setText(pt.getDescription());
            for(String tpid:set){
            	Element tpidElement = tpidsElement.addElement("tpid");
            	tpidElement.setText(tpid);
            }
            maxValue++;
		}
//		System.out.println(doc.asXML());
		return doc.asXML();// 转换成字符串
	}
	/**
	 * 车辆频繁出现点分析
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public  String createPzfx(ArrayList<SbPz> listC,int count){
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_root = doc.addElement("root");
		// 添加head元素
		Element el_head = el_root.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
		Element el_message = el_head.addElement("message");
		Element el_body = el_root.addElement("body");

		el_success.setText("1");//
		el_message.setText("统计查询成功");//
		//如果获取到结果，则返回查询结果
		el_count.setText("" + count);//查询总数
		
		/*
		 * <?xml version="1.0" encoding="GB2312"?>
			<root>	
			<head>
			    <success>1</success>
				<count>2002</count>
				<message></message>
			</head>
				<body>
					<data>
			<cphm1>粤B12345 </cphm1>
						<cphm2>粤B12346 </cphm2>
						<frequecy>0.5</ frequecy>
			</data>
			<data>
			<cphm1>粤B12347cphm1</cphm1>
						<cphm2>粤B12348cphm2</cphm2>
						<frequecy>0.6</ frequecy>
			</data>
			…
				</body>
			</root>
		 */
		
		for(int i=0;i<listC.size();i++){
			SbPz pt = listC.get(i);
			
			Element el_data = el_body.addElement("data");
			Element cphm1 = el_data.addElement("cphm1");
			Element cphm2 = el_data.addElement("cphm2");
			Element frequecy = el_data.addElement("frequency");
			
			//赋值
			cphm1.setText(pt.getCphm1());//
			cphm2.setText(pt.getCphm2());
			frequecy.setText(""+pt.getPropability());//
		}
		return doc.asXML();// 转换成字符串
	}
}
