package com.dyst.entites;

import static org.elasticsearch.index.query.QueryBuilders.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;

import com.dyst.elasticsearch.util.ESClientManager;

public class Pfcxfx {
	
	@Test//车辆频繁出现点分析
    public void PfcxfxTest() {   
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES数据库连接池,获取数据连接
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			    BoolQueryBuilder query = boolQuery();
				query.must(termsQuery("cphm1", "粤B4HW87".split(",")));
//				query.must(rangeQuery("tgsj")
//						.from(sdf.parse("2014-03-21 00:00:00").getTime())
//						.to(sdf.parse("2014-03-29 00:00:00").getTime()).includeLower(true)
//						.includeUpper(true))
//						;
		   //查询ES数据库的识别数据
			SearchResponse response = client.prepareSearch("sb").setTypes("sb")
					.setQuery(query).setFrom(0).setSize(500)
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
//				System.out.println(hit.getId()+"=="+hit.getScore()+"");
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
// 				System.out.println(s.getCphm1());
 				listA.add(temp);
 			}
 			//按照监测点id排序
 			Collections.sort(listA);//
 			
 			/*
 			 * //生成集合B1和B2,B1对应index1，B2对应Index2
 			 * 对A数组的时间戳1和地点相同的数据进行分组，并统计个数，记为
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
 			
// 			对B1和B2数据进行归并，归并方法：地点和时间戳编号相同的，出现
// 			次数相加，将归并结果写入到C，否则B1，B2的数据直接写入C
 			List<SbC> listC = new ArrayList<SbC>();
 			for(int i=0;i<listB1.size();i++){
 				PfTemp tmp = listB1.get(i);
 				SbC c = new SbC();
 				c.setJcdid(tmp.getJcdid());//地点
 				//时间段描述
 				c.setDescription(getDecsByIndex(tmp.getIndex1(), 15,0));//
 				c.setCount(tmp.getCount());//先把集合B1的count添加到c中
 				for(int j=0;j<listB2.size();j++){
 					PfTemp tmpB = listB2.get(j);
 					if(tmp.getJcdid().equals(tmpB.getJcdid())&&tmp.getIndex1()==tmpB.getIndex2()){
 						c.setCount(c.getCount()+tmpB.getCount());
 						listB2.remove(j);//与B1归并后删除掉该元素
 					}
 				}
 				listC.add(c);
 			}
 			for(int i=0;listB2!=null&&i<listB2.size();i++){
 				PfTemp tmp = listB2.get(i);
 				SbC c = new SbC();
 				c.setJcdid(tmp.getJcdid());//地点
 				//时间段描述
 				c.setDescription(getDecsByIndex(tmp.getIndex2(), 15, 5));//
 				c.setCount(tmp.getCount());//先把集合B1的count添加到c中
 				listC.add(c);
 			}
 			Collections.sort(listC);//
 			for(SbC pt : listC){
 				System.out.println(pt.getJcdid()+"----"+pt.getCount()+"----"+pt.getDescription());
 			}
// 			输出C中次数前20位数据，数据描述为：地点，时间端：
// 			时间段计算方法为，以时间戳1的时间段对应，将右边界扩大5分钟.
// 			例如：时间戳为1，时间段[0:00 0:15]->[0:00 0:20]
 			
 			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(client!=null){
				client.close();
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
		String desc = "["+(index*step-start)/60+":"+(index*step-start)%60+" "
		  +((index*step-start)+step)/60+":"+((index*step-start)+step)%60+"]->[" +
		  "["+(index*step-start)/60+":"+(index*step-start)%60+" "
		  +((index*step-start)+20)/60+":"+((index*step-start)+20)%60+"]";
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
			for(PfTemp pt : list){
				if(jcdid.equals(pt.getJcdid())&&index==pt.getIndex1()){
					count++;
				}
			}
			p.setCount(count);
		}else if("2".equals(flag)){
			p.setJcdid(jcdid);
			p.setIndex2(index);
			for(PfTemp pt : list){
				if(jcdid.equals(pt.getJcdid())&&index==pt.getIndex2()){
					count++;
				}
			}
			p.setCount(count);
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
//	    System.out.println(sdf.format(d));
//	    System.out.println(d.getHours()+"---"+d.getMinutes());
	    int time = d.getHours()*60+d.getMinutes();
//	    System.out.println(time+"时间值");
	    for(int i=0; i<list.size();i++){
	    	int begin = list.get(i)[0];
	    	int end = list.get(i)[1];
	    	if(time>=begin&&time<end){
//	    		System.out.println(begin+"----------"+end);
	    		return i;
	    	}
	    }
//	    System.out.println(+list.get(list.size()-1)[0]+"===="+list.get(list.size()-1)[1]+"------"+d);
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
	  SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	  ArrayList<int[]> list = szTime(0,15) ;
//	  int i=0;
//	  for(int[] ss:list){
////		  System.out.println((i++)+"+++++"+ss[0]/60+":"+ss[0]%60+"-->"+ss[1]/60+":"+ss[1]%60);
//	  }
//	  System.out.println(sdf.format(new Date()));
//	  System.out.println(indexTime(new Date(), list));
	  System.out.println(getDecsByIndex(3, 15,5));
  }
	
}
	