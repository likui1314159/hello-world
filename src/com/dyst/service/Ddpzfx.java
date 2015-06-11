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
		
		System.out.println("��ײ������ʱ��"+(System.currentTimeMillis()-startTime)/1000d+"��");
	}
	
	//ͨ�����е�ֵ����ѯ 

	
    public String  Pzfx(String requestXml) {   
		///XML��������
		XmlCreater xml = new XmlCreater();
    	Config config = Config.getInstance();//������Ϣ��
		Document document = null;
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		//��ȡes���ݿ�����
		ESClientManager ecclient = ESClientManager.getInstance();
		Client client = ecclient.getConnection("es");//ES���ݿ����ӳ�,��ȡ��������
		
		double frequecy =0.1;//����Ƶ��
		BoolQueryBuilder queryzh = boolQuery();
	    
	    ArrayList<String> jcdlist = new ArrayList<String>();

		queryzh.must(QueryBuilders.termQuery("qpsfwc", "1"));//��ʶ��
		//��෵�ؼ�¼��
		int maxReturnValueCount = 1000;
		//����XML�ļ�����ȡ����
		try {
			document = (Document) DocumentHelper.parseText(requestXml);
			Element root = document.getRootElement();//��ȡ���ڵ�
			Element head = (Element) root.selectNodes("head").get(0);
			Element body = (Element) root.selectNodes("body").get(0);
			Element frequencyElement = (Element) head.selectNodes("frequency").get(0);
			Element maxReturnRecord = (Element) head.selectNodes("maxReturnRecord").get(0);
			
			 frequecy = Double.parseDouble(frequencyElement.getText().trim());
			 maxReturnValueCount = Integer.parseInt(maxReturnRecord.getText());
			//�����ʱ��β�����ȡ
			Iterator itor = body.selectNodes("data").iterator();
			while(itor.hasNext()){
				Element el = (Element) itor.next();
				
				jcdlist.add(((Element)el.selectNodes("jcdid").get(0)).getText());
		    	BoolQueryBuilder query = boolQuery();
				query.must(termQuery("jcdid", ((Element)el.selectNodes("jcdid").get(0)).getText()));
//				query1.must(termsQuery("cphm1", "��DV8660,��AD5L59,��BQ6K19".split(",")));
				query.must(rangeQuery("tgsj")
						.from(sdf.parse(((Element)el.selectNodes("kssj").get(0)).getText()).getTime())
						.to(sdf.parse(((Element)el.selectNodes("jssj").get(0)).getText()).getTime()).includeLower(true)
						.includeUpper(true))
						;
				queryzh.should(query);
				
				System.out.println(((Element)el.selectNodes("jcdid").get(0)).getText());
			}
		} catch (Exception e) {
			//"09:xml�ļ���ʽ�޷����������飡"
			e.printStackTrace();
			return xml.createErrorXml(config.getErrorCode09());
		}
		queryzh.minimumNumberShouldMatch(1);
		
		try {
			//��ѯ��¼�����������趨ֵʱ����������
			CountResponse countResponse = client.prepareCount("sb").setTypes("sb")
//			.setQuery(QueryBuilders.filteredQuery(queryzh,filter))
			.setQuery(queryzh)
			.execute().actionGet();
			System.out.println("Count ��¼������"+countResponse.getCount());
			if(countResponse.getCount()>1000000){
				System.out.println("��ѯ��Χ̫�󣬼�¼��������ֵ"+countResponse.getCount());
				return xml.createErrorXml(config.getErrorCode15());
			}
			
////		    ��K=[n/2+0.5],Ϊ��С���ŶȺ�֧�ֶ�
//		    double k = jcdlist.size()/2d+0.5;//       n/2+0.5  jcdsz.length/6
//		    //��ȡ��������ż����
//		    k = Math.floor(k);
			double k = Math.ceil(jcdlist.size()*frequecy);
			
			
		   //��ѯES���ݿ��ʶ������
			int perSize = 200;//ÿ�η������ݴ�С=perSize*��Ƭ����150��=200*150
			SearchResponse response = client.prepareSearch("sb").setTypes("sb")
//					.setQuery(QueryBuilders.filteredQuery(queryzh,filter))
					.setQuery(queryzh)
					.setSearchType(SearchType.SCAN)//���´�����������Ҫʹ��sacn��ʽ
					.setScroll(new Scroll(new TimeValue(60000)))//����ָ��Scroll
					.setSize(200)
					.addFields(new String[]{"cphm1","jcdid","cplx1","tgsj","tpid1"})
					.setExplain(false)
					.execute().actionGet();
			
			Map<String, String> map;//= new LinkedHashMap<String, String>();
			List<Sbfx> listresult = new ArrayList<Sbfx>();//�������������н����
			
			while(true){
				response = client.prepareSearchScroll(
						response.getScrollId()).setScroll(//ͨ��scroll_IDִ�в�ѯ
						new TimeValue(60000)).execute().actionGet();
				
				Sbfx sb = null;
	 			for(final SearchHit hit:response.getHits()){
//					System.out.println(hit.getId()+"=="+hit.getScore()+"");
	               final Iterator<SearchHitField> iterator = hit.iterator();	
	               map = new LinkedHashMap<String, String>();
	               while(iterator.hasNext()){
	            	 final SearchHitField hitfield = iterator.next();
	            	 map.put(hitfield.getName(),hitfield.getValue().toString());
//	            	 System.out.print("��ѯ�ֶΣ�"+hitfield.getName());
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
	 			//û�з��������ļ�¼����ֹ��ѯ
				if(response.getHits().getHits().length==0){
					break;
				}
			}
 			//����һ������Ϊ�������������飬����м�¼��������Ϊ1������Ϊ0
 			int exist[]  ;
 			
 			//�Խ�����������򣬸���cphid
 			Collections.sort(listresult);
 			
 			System.out.println("��¼����---------"+listresult.size());
// 			for(Sbfx s:listresult){
//// 				
// 				qs += s.getCphm1()+",";
// 			}
// 			System.out.println(qs);
 			//��������B
 			ArrayList<SbTemp> listB = new ArrayList<SbTemp>();
 			SbTemp sbtemp ;
 			for(int i=0;i<listresult.size();i++){
 				Sbfx sbjl = (Sbfx) listresult.get(i);
// 				System.out.println(sbjl.getCphm1());
 				if(!existListB(listB, sbjl.getCphm1()) ){//�������B��û�У�����ӵ�����B��
 					sbtemp = new SbTemp();
 					sbtemp.setCphm1(sbjl.getCphm1());//���ƺ�
 					//����һ������������ͬ�����飬��¼�ó��Ǿ����ļ��㼯��
 					 exist = new int[jcdlist.size()];// = new String[2];
 					//�鿴��¼�е�jcdid�������е�˳��
 					int index;
 					if(( index = getIndexjcd(jcdlist, sbjl.getJcdid()))!=-1){
 						exist[index]=1;//���ڣ���ֵ1
 					}
 					
 					//���������������Խ���һ�µļ�����¼�п�����ջ�ȡ��ĳ��ƺ���һ�µ�
 					for(int j=i+1;j<listresult.size();j++){
 						Sbfx sbj = (Sbfx) listresult.get(j);
// 						System.out.println("-------"+sbj.getCphm1());
 						//˳���ȡ��ͬ���Ƽ�¼
 						if(sbjl.getCphm1().equals(sbj.getCphm1())){
 							if(( index = getIndexjcd(jcdlist, sbj.getJcdid()))!=-1){
 		 						exist[index]=1;//���ڣ���ֵ1
 		 					}
 						}else{
 							i=j;
 							break;
 						}
 					}
 					
 					sbtemp.setSequence(exist);
 					int cu = count(exist);
 					sbtemp.setCount(cu);
 					//��ӵ���ʱ������,���ϸ��ʵļ�¼����
 					if(cu>=k){
 						listB.add(sbtemp);
 					}
 				}
 			}
 			listB.trimToSize();
 			
 			ArrayList<SbPz> ss = (ArrayList<SbPz>) getPZcphm(listB, k,maxReturnValueCount);
 			
 			System.out.println("�п���ײ�ɹ���¼����"+ss.size());
// 			for(SbPz z :ss){
// 				System.out.println(z.getCphm1()+"--���ܰ��泵--"+z.getCphm2()+"---����--"+z.getPropability());
// 			}
 			return xml.createPzfx(ss, ss.size());
		} catch (Exception e) {
			e.printStackTrace();
			return xml.createErrorXml("��ײ����ʧ��");
		}finally{
			if(ecclient!=null){
				ecclient.freeConnection("es", client);
			}
		}
    } 
/**
 * ��int�����Ԫ��ȡ��
 * @param exist 0,1����
 * @return exist������1�ĸ���
 */
	public int count(int[] exist){
		int sum=0;
		for(int i=0;i<exist.length;i++){
			sum+=exist[i];
		}
		return sum;
	}
	/**
	 * �鿴����id�������г��ֵ�����
	 * @param list ���㼯��
	 * @param jcdid ����id
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
	 * �ж�ָ���ĳ��ƺ��Ƿ���ڼ���ListB��
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
	 * ��Լ���listB��
	 * @param listB
	 * L ��С�������
	 * @return
	 */
	public ArrayList<SbPz> getPZcphm(ArrayList<SbTemp> listB,double L,int maxReturnValueCount){
		ArrayList<SbPz> listpz = new ArrayList<SbPz>();
		int maxValuesum=1;
		//������λС��
		DecimalFormat df = new DecimalFormat("#.0000");
		for(int i=0;i<listB.size();i++){
			
			for(int j=i+1;j<listB.size();j++){
				int M=0;
				int [] a = listB.get(i).getSequence();
				int [] b = listB.get(j).getSequence();
				//�ж�a��b����ÿλ�Ƿ���ͬ����ͬ��M��1����Ϊ����������ͬ�����ص�ĸ���
				for(int k=0;k<a.length;k++){
					if(a[k]==b[k]&&a[k]==1){
						M++;
					}
				}
				if(M>=L){
					SbPz sp = new SbPz();
					sp.setCphm1(listB.get(i).getCphm1());//���ƺ���1
					sp.setCphm2(listB.get(j).getCphm1());//���ƺ���2
					//���ܳ��ֵĸ���
					sp.setPropability(Double.parseDouble(df.format((M/(double)a.length))));
					listpz.add(sp);
					maxValuesum++;
					//����󷵻ؼ�¼���ƣ����ڲ��������������ܵ���ϵͳ�ڴ������������̫��
					if(maxValuesum>maxReturnValueCount){
						return listpz;
					}
					
				}
			}
		}
		
		return listpz;
	}
	
}
