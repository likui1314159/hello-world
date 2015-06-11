package com.dyst.service;

import static org.elasticsearch.index.query.QueryBuilders.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.*;
import org.junit.Test;

import com.dyst.elasticsearch.util.ESClientManager;
import com.dyst.entites.*;
import com.dyst.util.Config;
import com.dyst.util.XmlCreater;


public class Ddpzfx {
   
	@Test
	public void testPz(){
//		//		    String jcdsz[] = "20501810,10300607,10100609,10100611,10200613,10100603,10300207,10300601".split(",");

		long startTime = System.currentTimeMillis();
		String xml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root><head>" +
				"<frequency>0.5</frequency><maxReturnRecord>10000</maxReturnRecord></head>" +
				"<body>" +
				"<data><jcdid>20501810</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-28 13:00:00</jssj></data>" +
				"<data><jcdid>10300607</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-28 13:00:00</jssj></data>" +
				"<data><jcdid>10100609</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-27 13:00:00</jssj></data>" +
				"<data><jcdid>10100611</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-27 13:00:00</jssj></data>" +
				"<data><jcdid>10200613</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-27 13:00:00</jssj></data>" +
				"<data><jcdid>10300207</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-27 13:00:00</jssj></data>" +
				"<data><jcdid>10100603</jcdid><kssj>2014-02-24 11:00:00</kssj><jssj>2014-02-27 13:00:00</jssj></data>" +
				"</body></root>";
		
		Ddpzfx dz = new Ddpzfx();
		System.out.println(dz.Pzfx(xml));
		
		System.out.println("碰撞分析耗时："+(System.currentTimeMillis()-startTime)/1000d+"秒");
	}
	
	//通过表中的值来查询 

	
    public String  Pzfx(String requestXml) {   
		///XML条件解析
		XmlCreater xml = new XmlCreater();
    	Config config = Config.getInstance();//配置信息类
		Document document = null;
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		//获取es数据库连接
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		
		double frequecy =0.1;//出现频率
		BoolQueryBuilder queryzh = boolQuery();
	    
	    ArrayList<String> jcdlist = new ArrayList<String>();

		queryzh.must(QueryBuilders.termQuery("qpsfwc", "1"));//已识别
		//最多返回记录数
		int maxReturnValueCount = 1000;
		//解析XML文件，获取参数
		try {
			document = (Document) DocumentHelper.parseText(requestXml);
			Element root = document.getRootElement();//获取根节点
			Element head = (Element) root.selectNodes("head").get(0);
			Element body = (Element) root.selectNodes("body").get(0);
			Element frequencyElement = (Element) head.selectNodes("frequency").get(0);
			Element maxReturnRecord = (Element) head.selectNodes("maxReturnRecord").get(0);
			
			 frequecy = Double.parseDouble(frequencyElement.getText().trim());
			 maxReturnValueCount = Integer.parseInt(maxReturnRecord.getText());
			//监测点和时间段参数获取
			Iterator itor = body.selectNodes("data").iterator();
			while(itor.hasNext()){
				Element el = (Element) itor.next();
				
				jcdlist.add(((Element)el.selectNodes("jcdid").get(0)).getText());
		    	BoolQueryBuilder query = boolQuery();
				query.must(termQuery("jcdid", ((Element)el.selectNodes("jcdid").get(0)).getText()));
//				query1.must(termsQuery("cphm1", "黑DV8660,湘AD5L59,粤BQ6K19".split(",")));
				query.must(rangeQuery("tgsj")
						.from(sdf.parse(((Element)el.selectNodes("kssj").get(0)).getText()).getTime())
						.to(sdf.parse(((Element)el.selectNodes("jssj").get(0)).getText()).getTime()).includeLower(true)
						.includeUpper(true))
						;
				queryzh.should(query);
				
				System.out.println(((Element)el.selectNodes("jcdid").get(0)).getText());
			}
		} catch (Exception e) {
			//"09:xml文件格式无法解析，请检查！"
			e.printStackTrace();
			return xml.createErrorXml(config.getErrorCode09());
		}
		queryzh.minimumNumberShouldMatch(1);
		
		try {
			//查询记录总数，超过设定值时不再做分析
			CountResponse countResponse = client.prepareCount("sb").setTypes("sb")
//			.setQuery(QueryBuilders.filteredQuery(queryzh,filter))
			.setQuery(queryzh)
			.execute().actionGet();
			System.out.println("Count 记录总数："+countResponse.getCount());
			if(countResponse.getCount()>1000000){
				System.out.println("查询范围太大，记录超过允许值"+countResponse.getCount());
				return xml.createErrorXml(config.getErrorCode15());
			}
			
////		    记K=[n/2+0.5],为最小置信度和支持度
//		    double k = jcdlist.size()/2d+0.5;//       n/2+0.5  jcdsz.length/6
//		    //下取整，正对偶数的
//		    k = Math.floor(k);
			double k = Math.ceil(jcdlist.size()*frequecy);
			
			
		   //查询ES数据库的识别数据
			int perSize = 200;//每次返回数据大小=perSize*分片数（150）=200*150
			SearchResponse response = client.prepareSearch("sb").setTypes("sb")
//					.setQuery(QueryBuilders.filteredQuery(queryzh,filter))
					.setQuery(queryzh)
					.setSearchType(SearchType.SCAN)//重新创建索引，需要使用sacn方式
					.setScroll(new Scroll(new TimeValue(60000)))//必须指定Scroll
					.setSize(200)
					.addFields(new String[]{"cphm1","jcdid","cplx1","tgsj","tpid1"})
					.setExplain(false)
					.execute().actionGet();
			
			Map<String, String> map;//= new LinkedHashMap<String, String>();
			List<Sbfx> listresult = new ArrayList<Sbfx>();//符合条件的所有结果集
			
			while(true){
				response = client.prepareSearchScroll(
						response.getScrollId()).setScroll(//通过scroll_ID执行查询
						new TimeValue(60000)).execute().actionGet();
				
				Sbfx sb = null;
	 			for(final SearchHit hit:response.getHits()){
//					System.out.println(hit.getId()+"=="+hit.getScore()+"");
	               final Iterator<SearchHitField> iterator = hit.iterator();	
	               map = new LinkedHashMap<String, String>();
	               while(iterator.hasNext()){
	            	 final SearchHitField hitfield = iterator.next();
	            	 map.put(hitfield.getName(),hitfield.getValue().toString());
//	            	 System.out.print("查询字段："+hitfield.getName());
//	            	 System.out.println(hitfield.getName()+"=="+hitfield.getValue()+"-----");
	               }
//	 				map = hit.getSource();
	               sb = new Sbfx();
	               sb.setCphm1(map.get("cphm1").toString());
	               sb.setCplx1(map.get("cplx1").toString());
	               sb.setJcdid(map.get("jcdid").toString());
	               sb.setTgsj(new Date(Long.parseLong(map.get("tgsj").toString())));
	               sb.setTpid1(map.get("tpid1").toString());
	               listresult.add(sb);
				}
	 			//没有符合条件的记录，终止查询
				if(response.getHits().getHits().length==0){
					break;
				}
			}
 			//定义一个容量为条件个数的数组，如果有记录，则内容为1，否则为0
 			int exist[]  ;
 			
 			//对结果集进行排序，根据cphid
 			Collections.sort(listresult);
 			
 			System.out.println("记录总数---------"+listresult.size());
// 			for(Sbfx s:listresult){
//// 				
// 				qs += s.getCphm1()+",";
// 			}
// 			System.out.println(qs);
 			//构建集合B
 			ArrayList<SbTemp> listB = new ArrayList<SbTemp>();
 			SbTemp sbtemp ;
 			for(int i=0;i<listresult.size();i++){
 				Sbfx sbjl = (Sbfx) listresult.get(i);
// 				System.out.println(sbjl.getCphm1());
 				if(!existListB(listB, sbjl.getCphm1()) ){//如果集合B中没有，则添加到集合B中
 					sbtemp = new SbTemp();
 					sbtemp.setCphm1(sbjl.getCphm1());//车牌号
 					//生成一个与监测点个数相同的数组，记录该车是经过的监测点集合
 					 exist = new int[jcdlist.size()];// = new String[2];
 					//查看记录中的jcdid在条件中的顺序
 					int index;
 					if(( index = getIndexjcd(jcdlist, sbjl.getJcdid()))!=-1){
 						exist[index]=1;//存在，赋值1
 					}
 					
 					//集合已做排序，所以接着一下的几个记录有可能与刚获取完的车牌号是一致的
 					for(int j=i+1;j<listresult.size();j++){
 						Sbfx sbj = (Sbfx) listresult.get(j);
// 						System.out.println("-------"+sbj.getCphm1());
 						//顺序获取相同车牌记录
 						if(sbjl.getCphm1().equals(sbj.getCphm1())){
 							if(( index = getIndexjcd(jcdlist, sbj.getJcdid()))!=-1){
 		 						exist[index]=1;//存在，赋值1
 		 					}
 						}else{
 							i=j;
 							break;
 						}
 					}
 					
 					sbtemp.setSequence(exist);
 					int cu = count(exist);
 					sbtemp.setCount(cu);
 					//添加到临时集合中,符合概率的记录车牌
 					if(cu>=k){
 						listB.add(sbtemp);
 					}
 				}
 			}
 			listB.trimToSize();
 			
 			ArrayList<SbPz> ss = (ArrayList<SbPz>) getPZcphm(listB, k,maxReturnValueCount);
 			
 			System.out.println("有可碰撞成功记录数："+ss.size());
// 			for(SbPz z :ss){
// 				System.out.println(z.getCphm1()+"--可能伴随车--"+z.getCphm2()+"---概率--"+z.getPropability());
// 			}
 			return xml.createPzfx(ss, ss.size());
		} catch (Exception e) {
			e.printStackTrace();
			return xml.createErrorXml("碰撞分析失败");
		}finally{
			if(ecclient!=null){
				ecclient.freeConnection("es", client);
			}
		}
    } 
/**
 * 对int数组各元素取和
 * @param exist 0,1数组
 * @return exist数组中1的个数
 */
	public int count(int[] exist){
		int sum=0;
		for(int i=0;i<exist.length;i++){
			sum+=exist[i];
		}
		return sum;
	}
	/**
	 * 查看监测点id在数组中出现的索引
	 * @param list 监测点集合
	 * @param jcdid 监测点id
	 * @return
	 */
	public int getIndexjcd(ArrayList<String> list,String jcdid){
		for(int i=0;i<list.size();i++){
			if(list.get(i).equals(jcdid)){
				return i;
			}
		}
		return -1;
	}
	/**
	 * 判断指定的车牌号是否存在集合ListB中
	 * @param listB
	 * @param cphm
	 * @return
	 */
	public boolean existListB(ArrayList<SbTemp> listB,String cphm){
		for(int i=0;i<listB.size();i++){
			SbTemp sbjl = (SbTemp) listB.get(i);
				if(sbjl.getCphm1().equals(cphm)){
					return true;
				}
			}
		return false;
	}
	/**
	 * 针对集合listB，
	 * @param listB
	 * L 最小允许概率
	 * @return
	 */
	public ArrayList<SbPz> getPZcphm(ArrayList<SbTemp> listB,double L,int maxReturnValueCount){
		ArrayList<SbPz> listpz = new ArrayList<SbPz>();
		int maxValuesum=1;
		//保留两位小数
		DecimalFormat df = new DecimalFormat("#.0000");
		for(int i=0;i<listB.size();i++){
			
			for(int j=i+1;j<listB.size();j++){
				int M=0;
				int [] a = listB.get(i).getSequence();
				int [] b = listB.get(j).getSequence();
				//判断a和b数组每位是否相同，相同的M加1，作为两个车牌相同经过地点的概率
				for(int k=0;k<a.length;k++){
					if(a[k]==b[k]&&a[k]==1){
						M++;
					}
				}
				if(M>=L){
					SbPz sp = new SbPz();
					sp.setCphm1(listB.get(i).getCphm1());//车牌号码1
					sp.setCphm2(listB.get(j).getCphm1());//车牌号码2
					//可能出现的概率
					sp.setPropability(Double.parseDouble(df.format((M/(double)a.length))));
					listpz.add(sp);
					maxValuesum++;
					//做最大返回记录限制，由于不正常的条件可能导致系统内存溢出，数据量太大
					if(maxValuesum>maxReturnValueCount){
						return listpz;
					}
					
				}
			}
		}
		
		return listpz;
	}
	
}
