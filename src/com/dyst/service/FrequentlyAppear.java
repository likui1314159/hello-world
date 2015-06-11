package com.dyst.service;

import static org.elasticsearch.index.query.QueryBuilders.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;

import com.dyst.elasticsearch.util.ESClientManager;
import com.dyst.entites.*;
import com.dyst.util.Config;
import com.dyst.util.XmlCreater;

public class FrequentlyAppear {
	
	//车辆频繁出现点分析
    public String frequentlyApp(String requestXml) {   
    	
    	XmlCreater xml = new XmlCreater();
    	Config config = Config.getInstance();//配置信息类
		Document document = null;
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	//条件变量
		String cphm = null;//号牌号码
		Date startTime = null;//开始时间
		Date endTime = null;//截止时间
		int maxReturnValue = 20;//默认返回的最大记录数，默认为20条
		//解析XML文件，获取参数
		try {
			document = (Document) DocumentHelper.parseText(requestXml);
			Element root = document.getRootElement();//获取根节点
			Element head = (Element) root.selectNodes("head").get(0);
			Element body = (Element) root.selectNodes("body").get(0);
			Element data = (Element) body.selectNodes("data").get(0);
			
			startTime = sdf.parse(data.element("kssj").getText().trim());//起始时间
			endTime = sdf.parse(data.element("jssj").getText().trim());//截止时间
			cphm = data.element("hphm").getText().trim();//号牌号码
			maxReturnValue = Integer.parseInt(head.element("maxReturnCount").getText().trim());
		} catch (Exception e) {
			//"09:xml文件格式无法解析，请检查！"
			return xml.createErrorXml(config.getErrorCode09());
		}
	   //获取es数据库连接
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
	
		try {
			//构建query查询条件
		    BoolQueryBuilder query = boolQuery();
		    //车牌号码
		    if(cphm!=null&&!"".equals(cphm.trim())){
		    	query.must(termsQuery("cphm1", cphm));
		    }
//			query.must(termQuery("jcdid", "20504604"));
		    //通过时间
		    if(startTime!=null&&startTime!=null){
		    	query.must(rangeQuery("tgsj")
						.from(startTime.getTime())
						.to(endTime.getTime()).includeLower(true)
						.includeUpper(true))
						;
		    }

	   /*
	    * 查询ES数据库的识别数据，只返回"cphm1","jcdid","cplx1","tgsj","tpid1"字段，
	    * 默认只返回10条数据，所以.setSize(9999)改成9999设置为最大的返回记录数
	    */
		SearchResponse response = client.prepareSearch("sb").setTypes("sb")
				.setQuery(query).setFrom(0).setSize(9999)
				.addFields(new String[]{"cphm1","jcdid","cplx1","tgsj","tpid1"})
				.setExplain(false)
				.execute().actionGet();
		SearchHits hits = response.getHits();
		System.out.println(hits.getTotalHits() + "-总记录数");
		//结果集组合
		Map<String, String> map;//= new LinkedHashMap<String, String>();
		List<Sbfx> listresult = new ArrayList<Sbfx>();//符合条件的所有结果集
		Sbfx sb = null;
			for(final SearchHit hit:response.getHits()){
		           final Iterator<SearchHitField> iterator = hit.iterator();	
		           map = new LinkedHashMap<String, String>();
		           while(iterator.hasNext()){
		        	 final SearchHitField hitfield = iterator.next();
		        	 map.put(hitfield.getName(),hitfield.getValue().toString());
		           }
		           sb = new Sbfx();
		           sb.setCphm1(map.get("cphm1"));
		           sb.setCplx1(map.get("Cplx1"));
		           sb.setJcdid(map.get("jcdid"));
		           sb.setTgsj(new Date(Long.parseLong(map.get("tgsj"))));
		           sb.setTpid1(map.get("tpid1"));
		           listresult.add(sb);
		    }
			//时间戳1，开始值从0开始算
			ArrayList<int[]> listTime1 = szTime(0, 15);
			//时间戳2，开始值从00:05分开始算
			ArrayList<int[]> listTime2 = szTime(5, 15);
			
			List<PfTemp> listA = new ArrayList<PfTemp>();
			PfTemp temp = null;
			//输出集合A
			for(Sbfx s:listresult){
				temp = new PfTemp();
				temp.setJcdid(s.getJcdid());//出现地点
				temp.setIndex1(indexTime(s.getTgsj(), listTime1));
				temp.setIndex2(indexTime(s.getTgsj(), listTime2));
				temp.setTpid(s.getTpid1());//图片id
//				System.out.println(s.getCphm1());
				listA.add(temp);
			}
			//按照监测点id排序
			Collections.sort(listA);//
			/*
			 生成集合B1和B2,B1对应index1，B2对应Index2
			对A数组的时间戳1和地点相同的数据进行分组，并统计个数，记为
			B1{地点，时间戳1，出现次数}
			对A数组的时间戳2和地点相同的数据进行分组，并统计个数，记为
			B2{地点，时间戳2，出现次数}
			 */
			List<PfTemp> listB1 = new ArrayList<PfTemp>();
			List<PfTemp> listB2 = new ArrayList<PfTemp>();
			
			//构建数组B1
			for(int i=0;i<listA.size();i++){
				PfTemp tmp = listA.get(i);
				if(!existSB(tmp.getJcdid(), tmp.getIndex1(), listB1, "1")){
					listB1.add(generateSB(tmp.getJcdid(), tmp.getIndex1(), listA, "1"));
				}
				if(!existSB(tmp.getJcdid(), tmp.getIndex2(), listB2, "2")){
					listB2.add(generateSB(tmp.getJcdid(), tmp.getIndex2(), listA, "2"));
				}
			}
//			对B1和B2数据进行归并，归并方法：地点和时间戳编号相同的，出现
//			次数相加，将归并结果写入到C，否则B1，B2的数据直接写入C
			List<SbC> listC = new ArrayList<SbC>();
			for(int i=0;i<listB1.size();i++){
				PfTemp tmp = listB1.get(i);
				SbC c = new SbC();
				c.setJcdid(tmp.getJcdid());//地点
				c.setCount(tmp.getCount());//先把集合B1的count添加到c中
				HashSet<String> set = new HashSet<String>();
				set.addAll(tmp.getSetTpid());
				
				boolean flag = false;//用于控制显示的时间段信息。
				for(int j=0;j<listB2.size();j++){
					PfTemp tmpB = listB2.get(j);
					if(tmp.getJcdid().equals(tmpB.getJcdid())&&tmp.getIndex1()==tmpB.getIndex2()){
//						c.setCount((int)Math.ceil((double)(c.getCount()+tmpB.getCount())/2d));
						flag = true;//标志存在集合1和2的交集
//						c.setCount(c.getCount()+tmpB.getCount());
						set.addAll(tmpB.getSetTpid());		
						listB2.remove(j);//与B1归并后删除掉该元素
						break;
					}
				}
				//时间段描述
				if(flag){
					c.setDescription(getDecsByIndex(tmp.getIndex1(), 15,0));//
//					System.out.println("@@@@@@"+c.getDescription());
				}else{
					c.setDescription(getDecsByIndex2(tmp.getIndex1(), 15,0));//
//					System.out.println("==========="+c.getDescription());
				}
				c.setCount(set.size());//
				c.setSetTpid(set);
				listC.add(c);
			}
//			for(int i=0;listB2!=null&&i<listB2.size();i++){
//				PfTemp tmp = listB2.get(i);
//				SbC c = new SbC();
//				c.setJcdid(tmp.getJcdid());//地点
//				//时间段描述
//				c.setDescription(getDecsByIndex2(tmp.getIndex2(), 15, 5));//
//				c.setCount(tmp.getCount());//先把集合B1的count添加到c中
//				c.setSetTpid(tmp.getSetTpid());
//				listC.add(c);
//			}
			Collections.sort(listC);//
			
			return xml.createFrequently(listC, Long.valueOf(hits.getTotalHits()).intValue(),maxReturnValue);
			//以下为输出测试
//			int count = 0;
//			int count1 = 0;
//			HashSet< String > set1 = new HashSet() ;
//			for(SbC pt : listC){
//				count +=pt.getCount();
//				System.out.println(pt.getJcdid()+"----"+pt.getCount()+"----"+pt.getDescription());
//                HashSet< String > set = (HashSet<String>) pt.getSetTpid();
//                set1.addAll(pt.getSetTpid());
//                count1 += set.size();
//                for(String tpid:set){
//                	System.out.println("                 tpid="+tpid);
//                }
//			}
//			System.out.println("--------------统计的记录总数count="+count);
//			System.out.println("--------------统计的记录总数Tpidcount="+count1);
//			System.out.println("--------------统计的记录总数TpidSet="+set1.size());
//			for(Sbfx pt : listresult){
//				if(pt.getTgsj().getHours()==23||pt.getTgsj().getHours()==23){
//					System.out.println(sdf.format(pt.getTgsj()));
//	 			}
//			}
//			输出C中次数前20位数据，数据描述为：地点，时间端：
//			时间段计算方法为，以时间戳1的时间段对应，将右边界扩大5分钟.
//			例如：时间戳为1，时间段[0:00 0:15]->[0:00 0:20]
	} catch (Exception e) {
		e.printStackTrace();
		return xml.createErrorXml("频繁出现点分析出错");
	}finally{
		if(ecclient!=null){
			ecclient.freeConnection("es", client);
		}
		
	}
} 
    
    
    
/**
 * 根据序号生成时间段描述信息
 * 例如：时间戳为1，时间段[0:00 0:15]->[0:00 0:20]
 * @param index
 * @return
 */
public String getDecsByIndex(int index,int step,int start){
	String desc = 
//	"["+(index*step-start)/60+":"+(index*step-start)%60+" "
//	  +((index*step-start)+step)/60+":"+((index*step-start)+step)%60+"]->" +
	  		"[" +
	  +(index*step-start)/60+":"+(index*step-start)%60+"  "
	  +((index*step-start)+20)/60+":"+((index*step-start)+20)%60+"]";
	return desc;
}


/**
 * 根据序号生成时间段描述信息
 * @param index
 * @return
 */
public String getDecsByIndex2(int index,int step,int start){
	String desc = "["+(index*step+start)/60+":"+(index*step+start)%60+"  "
	  +((index*step)+step+start)/60+":"+((index*step)+step+start)%60+"]" ;
//	  		"->[" +
//	  +(index*step)/60+":"+(index*step)%60+" "
//	  +((index*step)+20)/60+":"+((index*step)+20)%60+"]";
	return desc;
}
/**
 * 生成数组B,根据jcdid和序号，查找集合中存在跟指定jacketed和序号相同的，则count加1
 * @param jcdid
 * @param index
 * @param list
 * @param flag
 * @return
 */
public PfTemp generateSB(String jcdid,int index,List<PfTemp> list,String flag){
	PfTemp p = new PfTemp();
	int count=0;
	if("1".equals(flag)){
		p.setJcdid(jcdid);
		p.setIndex1(index);
		Set<String> tpidSet = new HashSet<String>();// (new Set()).add(p.getTpid());
//		tpidSet.add(p.getTpid());
		for(PfTemp pt : list){
			if(jcdid.equals(pt.getJcdid())&&index==pt.getIndex1()){
				count++;
				tpidSet.add(pt.getTpid());
			}
		}
		p.setCount(count);
		//图片id的set集合。去重
		p.setSetTpid(tpidSet);
	}else if("2".equals(flag)){
		p.setJcdid(jcdid);
		p.setIndex2(index);
		Set<String> tpidSet = new HashSet<String>();// (new Set()).add(p.getTpid());
//		tpidSet.add(p.getTpid());
		for(PfTemp pt : list){
			if(jcdid.equals(pt.getJcdid())&&index==pt.getIndex2()){
				count++;
				tpidSet.add(pt.getTpid());
			}
		}
		p.setCount(count);
		p.setSetTpid(tpidSet);
	}
	return p;
} 

/**
 * 判断给定的监测点id和序号是否在集合中存在	
 * @param jcdid
 * @param index
 * @param list
 * @param flag 判断是index1还是index2
 * @return
 */
public boolean existSB(String jcdid ,int index ,List<PfTemp>  list,String flag){
	if("1".equals(flag)){
		for(PfTemp p :list){
			if(p.getJcdid().equals(jcdid)&&p.getIndex1()==index){
				return true;
			}
		}
	}else if("2".equals(flag)){
		for(PfTemp p :list){
			if(p.getJcdid().equals(jcdid)&&p.getIndex2()==index){
				return true;
			}
		}
	}
	
	return false;
}

/**
 * 把日期的 小时*60+分钟 ，计算该值在集合中的索引值
 * @param d 日期
 * @param list 日期数组集合，int[2] 每个实体包括开始值和结束值范围
 * @return
 */
public int indexTime(Date d, ArrayList<int[]> list){
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(d.getTime());
//    System.out.println(sdf.format(d));
//    System.out.println(d.getHours()+"---"+d.getMinutes());
    int time = d.getHours()*60+d.getMinutes();
//    System.out.println(time+"时间值");
    for(int i=0; i<list.size();i++){
    	int begin = list.get(i)[0];
    	int end = list.get(i)[1];
    	if(time>=begin&&time<end){
//    		System.out.println(begin+"----------"+end);
    		return i;
    	}
    }
//    System.out.println(+list.get(list.size()-1)[0]+"===="+list.get(list.size()-1)[1]+"------"+d);
    //如果在集合中没找到，所以该日期就在集合最后一个范围里面
    return list.size()-1;
}
/**
 * 根据给定初始值，生成一个以step为步长的数组。
 * @param d 初始值
 * @param step 步长
 * @return
 */
public ArrayList<int[]> szTime(int d,int step){
    ArrayList<int[]> list = new ArrayList<int[]>();
    while(d<24*60){
		int [] time = new int[2];
		//如果第2位数组大于等于24，则跳转至开始出
	    if((d+step)/60>=24){
	    	time = new int[]{d,(d+step)%60};	  		
	    }else{
	    	time = new int[]{d,d+step};		    	
	    }
      	d += step;
      	list.add(time);
    }
	return list;
}



@Test
public void TestTime(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		ArrayList<int[]> list = szTime(0, 15);
//		ArrayList<int[]> list1 = szTime(5, 15);
		int i = 0;
//		for (int[] ss : list) {
			// System.out.println((i++)+"+++++"+ss[0]/60+":"+ss[0]%60+"-->"+ss[1]/60+":"+ss[1]%60);
//		}
		/*
		 * 2014-04-26 21:41:08 2014-04-23 21:48:49 2014-04-25 21:33:48
		 * 2014-04-24 21:41:36
		 */
		try {
//			System.out
//					.println(indexTime(sdf.parse("2014-04-26 21:41:08"), list));
//			System.out.println(indexTime(sdf.parse("2014-04-23 21:48:49"),
//					list1));
//			System.out
//					.println(indexTime(sdf.parse("2014-04-25 21:33:48"), list));
//			System.out
//					.println(indexTime(sdf.parse("2014-04-24 21:17:02"), list));
//			System.out.println(indexTime(sdf.parse("2014-04-24 21:17:02"),
//					list1));
			// System.out.println(getDecsByIndex(1, 15,0));

			// System.out.println(sdf.parse("2014-06-21 20:00:00").getTime());
			// System.out.println(sdf.parse("2014-06-22 00:00:00").getTime());
//			query.must(termsQuery("cphm1", "粤B4HW87".split(",")));
//			query.must(termQuery("jcdid", "09000009"));
//			query.must(rangeQuery("tgsj")
//					.from(sdf.parse("2014-03-21 00:00:00").getTime())
//					.to(sdf.parse("2014-03-29 00:00:00").getTime()).includeLower(true)
//					.includeUpper(true))
			
			String requestXml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root><head><maxReturnCount>20</maxReturnCount>" +
					"</head><body><data><hphm>粤B4HW87</hphm><kssj>2014-02-02 00:00:00</kssj><jssj>2014-06-08 14:55:00</jssj></data></body></root>";
			FrequentlyAppear f = new FrequentlyAppear();
			System.out.println(f.frequentlyApp(requestXml));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  

}
	