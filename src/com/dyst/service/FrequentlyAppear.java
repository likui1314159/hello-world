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
	
	//����Ƶ�����ֵ����
    public String frequentlyApp(String requestXml) {   
    	
    	XmlCreater xml = new XmlCreater();
    	Config config = Config.getInstance();//������Ϣ��
		Document document = null;
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	//��������
		String cphm = null;//���ƺ���
		Date startTime = null;//��ʼʱ��
		Date endTime = null;//��ֹʱ��
		int maxReturnValue = 20;//Ĭ�Ϸ��ص�����¼����Ĭ��Ϊ20��
		//����XML�ļ�����ȡ����
		try {
			document = (Document) DocumentHelper.parseText(requestXml);
			Element root = document.getRootElement();//��ȡ���ڵ�
			Element head = (Element) root.selectNodes("head").get(0);
			Element body = (Element) root.selectNodes("body").get(0);
			Element data = (Element) body.selectNodes("data").get(0);
			
			startTime = sdf.parse(data.element("kssj").getText().trim());//��ʼʱ��
			endTime = sdf.parse(data.element("jssj").getText().trim());//��ֹʱ��
			cphm = data.element("hphm").getText().trim();//���ƺ���
			maxReturnValue = Integer.parseInt(head.element("maxReturnCount").getText().trim());
		} catch (Exception e) {
			//"09:xml�ļ���ʽ�޷����������飡"
			return xml.createErrorXml(config.getErrorCode09());
		}
	   //��ȡes���ݿ�����
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
	
		try {
			//����query��ѯ����
		    BoolQueryBuilder query = boolQuery();
		    //���ƺ���
		    if(cphm!=null&&!"".equals(cphm.trim())){
		    	query.must(termsQuery("cphm1", cphm));
		    }
//			query.must(termQuery("jcdid", "20504604"));
		    //ͨ��ʱ��
		    if(startTime!=null&&startTime!=null){
		    	query.must(rangeQuery("tgsj")
						.from(startTime.getTime())
						.to(endTime.getTime()).includeLower(true)
						.includeUpper(true))
						;
		    }

	   /*
	    * ��ѯES���ݿ��ʶ�����ݣ�ֻ����"cphm1","jcdid","cplx1","tgsj","tpid1"�ֶΣ�
	    * Ĭ��ֻ����10�����ݣ�����.setSize(9999)�ĳ�9999����Ϊ���ķ��ؼ�¼��
	    */
		SearchResponse response = client.prepareSearch("sb").setTypes("sb")
				.setQuery(query).setFrom(0).setSize(9999)
				.addFields(new String[]{"cphm1","jcdid","cplx1","tgsj","tpid1"})
				.setExplain(false)
				.execute().actionGet();
		SearchHits hits = response.getHits();
		System.out.println(hits.getTotalHits() + "-�ܼ�¼��");
		//��������
		Map<String, String> map;//= new LinkedHashMap<String, String>();
		List<Sbfx> listresult = new ArrayList<Sbfx>();//�������������н����
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
			//ʱ���1����ʼֵ��0��ʼ��
			ArrayList<int[]> listTime1 = szTime(0, 15);
			//ʱ���2����ʼֵ��00:05�ֿ�ʼ��
			ArrayList<int[]> listTime2 = szTime(5, 15);
			
			List<PfTemp> listA = new ArrayList<PfTemp>();
			PfTemp temp = null;
			//�������A
			for(Sbfx s:listresult){
				temp = new PfTemp();
				temp.setJcdid(s.getJcdid());//���ֵص�
				temp.setIndex1(indexTime(s.getTgsj(), listTime1));
				temp.setIndex2(indexTime(s.getTgsj(), listTime2));
				temp.setTpid(s.getTpid1());//ͼƬid
//				System.out.println(s.getCphm1());
				listA.add(temp);
			}
			//���ռ���id����
			Collections.sort(listA);//
			/*
			 ���ɼ���B1��B2,B1��Ӧindex1��B2��ӦIndex2
			��A�����ʱ���1�͵ص���ͬ�����ݽ��з��飬��ͳ�Ƹ�������Ϊ
			B1{�ص㣬ʱ���1�����ִ���}
			��A�����ʱ���2�͵ص���ͬ�����ݽ��з��飬��ͳ�Ƹ�������Ϊ
			B2{�ص㣬ʱ���2�����ִ���}
			 */
			List<PfTemp> listB1 = new ArrayList<PfTemp>();
			List<PfTemp> listB2 = new ArrayList<PfTemp>();
			
			//��������B1
			for(int i=0;i<listA.size();i++){
				PfTemp tmp = listA.get(i);
				if(!existSB(tmp.getJcdid(), tmp.getIndex1(), listB1, "1")){
					listB1.add(generateSB(tmp.getJcdid(), tmp.getIndex1(), listA, "1"));
				}
				if(!existSB(tmp.getJcdid(), tmp.getIndex2(), listB2, "2")){
					listB2.add(generateSB(tmp.getJcdid(), tmp.getIndex2(), listA, "2"));
				}
			}
//			��B1��B2���ݽ��й鲢���鲢�������ص��ʱ��������ͬ�ģ�����
//			������ӣ����鲢���д�뵽C������B1��B2������ֱ��д��C
			List<SbC> listC = new ArrayList<SbC>();
			for(int i=0;i<listB1.size();i++){
				PfTemp tmp = listB1.get(i);
				SbC c = new SbC();
				c.setJcdid(tmp.getJcdid());//�ص�
				c.setCount(tmp.getCount());//�ȰѼ���B1��count��ӵ�c��
				HashSet<String> set = new HashSet<String>();
				set.addAll(tmp.getSetTpid());
				
				boolean flag = false;//���ڿ�����ʾ��ʱ�����Ϣ��
				for(int j=0;j<listB2.size();j++){
					PfTemp tmpB = listB2.get(j);
					if(tmp.getJcdid().equals(tmpB.getJcdid())&&tmp.getIndex1()==tmpB.getIndex2()){
//						c.setCount((int)Math.ceil((double)(c.getCount()+tmpB.getCount())/2d));
						flag = true;//��־���ڼ���1��2�Ľ���
//						c.setCount(c.getCount()+tmpB.getCount());
						set.addAll(tmpB.getSetTpid());		
						listB2.remove(j);//��B1�鲢��ɾ������Ԫ��
						break;
					}
				}
				//ʱ�������
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
//				c.setJcdid(tmp.getJcdid());//�ص�
//				//ʱ�������
//				c.setDescription(getDecsByIndex2(tmp.getIndex2(), 15, 5));//
//				c.setCount(tmp.getCount());//�ȰѼ���B1��count��ӵ�c��
//				c.setSetTpid(tmp.getSetTpid());
//				listC.add(c);
//			}
			Collections.sort(listC);//
			
			return xml.createFrequently(listC, Long.valueOf(hits.getTotalHits()).intValue(),maxReturnValue);
			//����Ϊ�������
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
//			System.out.println("--------------ͳ�Ƶļ�¼����count="+count);
//			System.out.println("--------------ͳ�Ƶļ�¼����Tpidcount="+count1);
//			System.out.println("--------------ͳ�Ƶļ�¼����TpidSet="+set1.size());
//			for(Sbfx pt : listresult){
//				if(pt.getTgsj().getHours()==23||pt.getTgsj().getHours()==23){
//					System.out.println(sdf.format(pt.getTgsj()));
//	 			}
//			}
//			���C�д���ǰ20λ���ݣ���������Ϊ���ص㣬ʱ��ˣ�
//			ʱ��μ��㷽��Ϊ����ʱ���1��ʱ��ζ�Ӧ�����ұ߽�����5����.
//			���磺ʱ���Ϊ1��ʱ���[0:00 0:15]->[0:00 0:20]
	} catch (Exception e) {
		e.printStackTrace();
		return xml.createErrorXml("Ƶ�����ֵ��������");
	}finally{
		if(ecclient!=null){
			ecclient.freeConnection("es", client);
		}
		
	}
} 
    
    
    
/**
 * �����������ʱ���������Ϣ
 * ���磺ʱ���Ϊ1��ʱ���[0:00 0:15]->[0:00 0:20]
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
 * �����������ʱ���������Ϣ
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
 * ��������B,����jcdid����ţ����Ҽ����д��ڸ�ָ��jacketed�������ͬ�ģ���count��1
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
		//ͼƬid��set���ϡ�ȥ��
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
 * �жϸ����ļ���id������Ƿ��ڼ����д���	
 * @param jcdid
 * @param index
 * @param list
 * @param flag �ж���index1����index2
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
 * �����ڵ� Сʱ*60+���� �������ֵ�ڼ����е�����ֵ
 * @param d ����
 * @param list �������鼯�ϣ�int[2] ÿ��ʵ�������ʼֵ�ͽ���ֵ��Χ
 * @return
 */
public int indexTime(Date d, ArrayList<int[]> list){
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(d.getTime());
//    System.out.println(sdf.format(d));
//    System.out.println(d.getHours()+"---"+d.getMinutes());
    int time = d.getHours()*60+d.getMinutes();
//    System.out.println(time+"ʱ��ֵ");
    for(int i=0; i<list.size();i++){
    	int begin = list.get(i)[0];
    	int end = list.get(i)[1];
    	if(time>=begin&&time<end){
//    		System.out.println(begin+"----------"+end);
    		return i;
    	}
    }
//    System.out.println(+list.get(list.size()-1)[0]+"===="+list.get(list.size()-1)[1]+"------"+d);
    //����ڼ�����û�ҵ������Ը����ھ��ڼ������һ����Χ����
    return list.size()-1;
}
/**
 * ���ݸ�����ʼֵ������һ����stepΪ���������顣
 * @param d ��ʼֵ
 * @param step ����
 * @return
 */
public ArrayList<int[]> szTime(int d,int step){
    ArrayList<int[]> list = new ArrayList<int[]>();
    while(d<24*60){
		int [] time = new int[2];
		//�����2λ������ڵ���24������ת����ʼ��
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
//			query.must(termsQuery("cphm1", "��B4HW87".split(",")));
//			query.must(termQuery("jcdid", "09000009"));
//			query.must(rangeQuery("tgsj")
//					.from(sdf.parse("2014-03-21 00:00:00").getTime())
//					.to(sdf.parse("2014-03-29 00:00:00").getTime()).includeLower(true)
//					.includeUpper(true))
			
			String requestXml = "<?xml version=\"1.0\" encoding=\"GB2312\"?><root><head><maxReturnCount>20</maxReturnCount>" +
					"</head><body><data><hphm>��B4HW87</hphm><kssj>2014-02-02 00:00:00</kssj><jssj>2014-06-08 14:55:00</jssj></data></body></root>";
			FrequentlyAppear f = new FrequentlyAppear();
			System.out.println(f.frequentlyApp(requestXml));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  

}
	