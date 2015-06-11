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
	
	@Test//����Ƶ�����ֵ����
    public void PfcxfxTest() {   
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			    BoolQueryBuilder query = boolQuery();
				query.must(termsQuery("cphm1", "��B4HW87".split(",")));
//				query.must(rangeQuery("tgsj")
//						.from(sdf.parse("2014-03-21 00:00:00").getTime())
//						.to(sdf.parse("2014-03-29 00:00:00").getTime()).includeLower(true)
//						.includeUpper(true))
//						;
		   //��ѯES���ݿ��ʶ������
			SearchResponse response = client.prepareSearch("sb").setTypes("sb")
					.setQuery(query).setFrom(0).setSize(500)
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
// 				System.out.println(s.getCphm1());
 				listA.add(temp);
 			}
 			//���ռ���id����
 			Collections.sort(listA);//
 			
 			/*
 			 * //���ɼ���B1��B2,B1��Ӧindex1��B2��ӦIndex2
 			 * ��A�����ʱ���1�͵ص���ͬ�����ݽ��з��飬��ͳ�Ƹ�������Ϊ
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
 			
// 			��B1��B2���ݽ��й鲢���鲢�������ص��ʱ��������ͬ�ģ�����
// 			������ӣ����鲢���д�뵽C������B1��B2������ֱ��д��C
 			List<SbC> listC = new ArrayList<SbC>();
 			for(int i=0;i<listB1.size();i++){
 				PfTemp tmp = listB1.get(i);
 				SbC c = new SbC();
 				c.setJcdid(tmp.getJcdid());//�ص�
 				//ʱ�������
 				c.setDescription(getDecsByIndex(tmp.getIndex1(), 15,0));//
 				c.setCount(tmp.getCount());//�ȰѼ���B1��count��ӵ�c��
 				for(int j=0;j<listB2.size();j++){
 					PfTemp tmpB = listB2.get(j);
 					if(tmp.getJcdid().equals(tmpB.getJcdid())&&tmp.getIndex1()==tmpB.getIndex2()){
 						c.setCount(c.getCount()+tmpB.getCount());
 						listB2.remove(j);//��B1�鲢��ɾ������Ԫ��
 					}
 				}
 				listC.add(c);
 			}
 			for(int i=0;listB2!=null&&i<listB2.size();i++){
 				PfTemp tmp = listB2.get(i);
 				SbC c = new SbC();
 				c.setJcdid(tmp.getJcdid());//�ص�
 				//ʱ�������
 				c.setDescription(getDecsByIndex(tmp.getIndex2(), 15, 5));//
 				c.setCount(tmp.getCount());//�ȰѼ���B1��count��ӵ�c��
 				listC.add(c);
 			}
 			Collections.sort(listC);//
 			for(SbC pt : listC){
 				System.out.println(pt.getJcdid()+"----"+pt.getCount()+"----"+pt.getDescription());
 			}
// 			���C�д���ǰ20λ���ݣ���������Ϊ���ص㣬ʱ��ˣ�
// 			ʱ��μ��㷽��Ϊ����ʱ���1��ʱ��ζ�Ӧ�����ұ߽�����5����.
// 			���磺ʱ���Ϊ1��ʱ���[0:00 0:15]->[0:00 0:20]
 			
 			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(client!=null){
				client.close();
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
		String desc = "["+(index*step-start)/60+":"+(index*step-start)%60+" "
		  +((index*step-start)+step)/60+":"+((index*step-start)+step)%60+"]->[" +
		  "["+(index*step-start)/60+":"+(index*step-start)%60+" "
		  +((index*step-start)+20)/60+":"+((index*step-start)+20)%60+"]";
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
//	    System.out.println(sdf.format(d));
//	    System.out.println(d.getHours()+"---"+d.getMinutes());
	    int time = d.getHours()*60+d.getMinutes();
//	    System.out.println(time+"ʱ��ֵ");
	    for(int i=0; i<list.size();i++){
	    	int begin = list.get(i)[0];
	    	int end = list.get(i)[1];
	    	if(time>=begin&&time<end){
//	    		System.out.println(begin+"----------"+end);
	    		return i;
	    	}
	    }
//	    System.out.println(+list.get(list.size()-1)[0]+"===="+list.get(list.size()-1)[1]+"------"+d);
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
	