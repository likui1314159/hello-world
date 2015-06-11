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
	 * ���ݲ���List,ת����ͨ������XML�ĵ��ַ���
	 * @param list
	 * @return XML�����ļ�
	 */
	public String createXml(String homdFlag, List list, SearchHits hits){
		/*
		 * ����listֵ����XML�ļ������ת�����ַ���������
		 */
//		Date date1 = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		//����xml�ļ�
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_rowdata1 = doc.addElement("root");
		//���headԪ��
		Element el_head = el_rowdata1.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
	    Element el_message = el_head.addElement("message");
	    el_success.setText("1");
	    
	    //��������
	    int lenES = 0;
	    int lenOralce = 0;
	    if(hits != null){//ES ���ѯ����
//	    	lenES = hits.getTotalHits();//��ȡ����
	    	lenES = hits.getHits().length;
	    }
	    if(list != null && list.size() > 0){//Oralce���ѯ����
	    	lenOralce = list.size();
	    }
	    el_count.setText("" + (lenOralce + lenES));
	    el_message.setText("��ѯ�ɹ�");
	    
	    //ΪbodyԪ�ظ�ֵ
	    Element  el_body = el_rowdata1.addElement("body");
	    Sbnew sb = new Sbnew();
	    //Oralce���ѯ�����
		if(list != null && list.size() > 0) {
			//�����������Ϊ�գ�����ʧ�ܣ�������countΪ0��messageΪ���ݿ�����ʧ��
			//����Ϊbody�ڵ������	
			for(int i = 0;i < list.size();i++) {	
				//tx=(Txsjcx)list.get(i);//�ɵ�ʵ�ַ�ʽ
				sb = (Sbnew)list.get(i);
				
				//�ж��Ƿ����غ�������������غ����������������
				Integer tpzs = sb.getTpzs();
				//homdFlagΪnull��Ϊ�ջ�0ʱ��ֻҪ�ó���Ϊ��������������
				if((homdFlag == null || "".equals(homdFlag) || !"0".equals(homdFlag)) 
						&& JjhomdOracle.hideJjhomd(sb.getCphm1(), sb.getCplx1())){//����
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
				    	
//				//��ֵ
//				el_hphm.setText(tx.getCphid());
//				el_sbsj.setText(sdf.format(tx.getSbsj()));
//				el_jcdid.setText(tx.getJcdid());
//				el_jcdmc.setText("");//��������
//				el_cplx.setText(tx.getCplx());
//				el_tp1.setText(tx.getTpid1());
//				el_tp2.setText("");
//				el_cdid.setText(tx.getCdid());
				//��ֵ,һ�ű�ʵ�ַ�ʽ
				el_hphm.setText(sb.getCphm1());
				el_cplx.setText(sb.getCplx1());
				
				el_jcdid.setText(sb.getJcdid());
//				el_jcdmc.setText("");//��������
				el_cdid.setText(sb.getCdid());
				
				Timestamp tgsj = sb.getTgsj();
				el_sbsj.setText(tgsj == null? "":sdf.format(tgsj));
				
				el_tpzs.setText("" + sb.getTpzs());//ͼƬ����
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
		
		//ES���ѯ�����
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
		            while(iterator.hasNext()){//һ����¼
		            	final SearchHitField hitfield = iterator.next();
		            	map.put(hitfield.getName(), hitfield.getValue().toString());
//		            	System.out.print(hitfield.getName()+"=="+hitfield.getValue()+"-----");
		            }
		            
		            //�ж��Ƿ����غ�������������غ����������������
		            String tpzs = (String)map.get("tpzs");
		            //homdFlagΪnull��Ϊ�ջ�0ʱ��ֻҪ�ó���Ϊ��������������
					if((homdFlag == null || "".equals(homdFlag) || !"0".equals(homdFlag)) 
							&& JjhomdOracle.hideJjhomd((String)map.get("cphm1"), (String)map.get("cplx1"))){//����
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
//				    el_jcdmc.setText("");//��������
				    el_sbsj.setText("".equals(map.get("tgsj")) ? "": 
				    	sdf.format(new Date(Long.parseLong(map.get("tgsj")))));
				   
				    el_tpzs.setText(tpzs == null? "":tpzs);//ͼƬ����;
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
	
		String str_xml = doc.asXML();//ת�����ַ���
	//	System.out.println(str_xml);
				
//		Date date2 = new Date();
//		double d = (date2.getTime() - date1.getTime());
//		System.out.println("����XML�ļ���ʱ��"+d/1000+"��");
				
		return str_xml;
	}
	
	/**
	 * ����oracle��ES��ѯ�����¼����ͨ��XML��ʽ���ظ��û�<br>
	 * @param oracleCount�� Oracle��ѯ��¼����<br>
	 * @param esCount�� ES��ѯ��¼����<br>
	 * @return  xml ����
	 */
	public String createCountXml(int oracleCount, int esCount){
		/*
		 * ����listֵ����XML�ļ������ת�����ַ���������
		 */
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_rowdata1 = doc.addElement("root");
		//���headԪ��
		Element el_head = el_rowdata1.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
	    Element el_message = el_head.addElement("message");
	    
	    el_success.setText("1") ;
	    el_count.setText("" + (oracleCount + esCount));
	    el_message.setText("��ѯ�ɹ�");
	    
		String str_xml = doc.asXML();//ת�����ַ���
		return str_xml;
	}
	
	/**
	 * ��ѯʧ�ܣ�����ָ�������xml�ļ�
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public String createErrorXml(String message){
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_rowdata1 = doc.addElement("root");
		// ���headԪ��
		Element el_head = el_rowdata1.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
		Element el_message = el_head.addElement("message");

		el_success.setText("0");//ʧ��
		el_count.setText("0");
		el_message.setText(message);//ʧ����Ϣ����
		String str_xml = doc.asXML();// ת�����ַ���
		return str_xml;
	}
	
	/**
	 * ͼƬ��ѯ�����ص�xml�ļ�
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public String createPicPath(List<String> listPic){
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_rowdata1 = doc.addElement("root");
		// ���headԪ��
		Element el_head = el_rowdata1.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
		Element el_message = el_head.addElement("message");
		Element el_body = el_rowdata1.addElement("body");

		el_success.setText("1");//ʧ��
		el_message.setText("ͼƬ��ַ��ѯ�ɹ�");//
		if(listPic == null || listPic.size() == 0){//����Ϊ��
			el_count.setText("0");
			return doc.asXML();// ת�����ַ���
		}
		
		//�����ȡ��������򷵻ز�ѯ���
		el_count.setText("" + listPic.size());//��ѯ����
		for (int j = 0;j < listPic.size();j++) {
			Element el_path = el_body.addElement("path");
	    	//��ֵ
			el_path.setText(listPic.get(j));
		}     
		String str_xml = doc.asXML();// ת�����ַ���
		// System.out.println(str_xml);
		return str_xml;
	}
	
	/**
	 * ����ʶ�𣬷��ص�xml�ļ�
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public String createUpdateXml(String message){
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_rowdata1 = doc.addElement("root");
		// ���headԪ��
		Element el_head = el_rowdata1.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
		Element el_message = el_head.addElement("message");

		el_success.setText("1");//
		el_count.setText("");
		el_message.setText(message);//��Ϣ����
		String str_xml = doc.asXML();// ת�����ַ���
		return str_xml;
	}
	
	/**
	 * ͳ�Ʋ�ѯ�������
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public String createTjxml(TermsFacet tf){
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_root = doc.addElement("root");
		// ���headԪ��
		Element el_head = el_root.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
		Element el_message = el_head.addElement("message");
		Element el_body = el_root.addElement("body");

		el_success.setText("1");//
		el_message.setText("ͳ�Ʋ�ѯ�ɹ�");//
//		for(TermsFacet.Entry entry:tf){
////			System.out.print((i++)+"�ֶ�ֵ:"+entry.getTerm()+"   ");
////			System.out.println("����Ƶ��:"+entry.getCount());
//		}
		//�����ȡ��������򷵻ز�ѯ���
		el_count.setText("" + tf.getTotalCount());//��ѯ����
		
		for(TermsFacet.Entry entry:tf){
			Element el_data = el_body.addElement("data");
			Element groupName = el_data.addElement("groupName");
			Element el_value = el_data.addElement("value");
	    	//��ֵ
			groupName.setText(""+entry.getTerm());//�����ֶ�ֵ
			el_value.setText(""+entry.getCount());//����ֵ
		}     
		String str_xml = doc.asXML();// ת�����ַ���
		// System.out.println(str_xml);
		return str_xml;
	}
	/**
	 * ����Ƶ�����ֵ����
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public  String createFrequently(List<SbC> listC,int count,int maxReturnValue){
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_root = doc.addElement("root");
		// ���headԪ��
		Element el_head = el_root.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
		Element el_message = el_head.addElement("message");
		Element el_body = el_root.addElement("body");

		el_success.setText("1");//
		el_message.setText("ͳ�Ʋ�ѯ�ɹ�");//
		//�����ȡ��������򷵻ز�ѯ���
		el_count.setText("" + count);//��ѯ����
		//�������������еı���
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
			
		    HashSet<String> set = (HashSet<String>) pt.getSetTpid();//ͼƬid����
		    
			//��ֵ
			jcdid.setText(""+pt.getJcdid());//
			//pt.getCount()��ȡ������ʵ�ʿ��ܲ�����ȡͼƬid��Ψһֵ��
			frequency.setText(""+set.size());//
			//���������ܳ��ִ����еı���
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
		return doc.asXML();// ת�����ַ���
	}
	/**
	 * ����Ƶ�����ֵ����
	 * @param 
	 * @return
	 * @throws Exception
	 */
	public  String createPzfx(ArrayList<SbPz> listC,int count){
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GB2312");
		Element el_root = doc.addElement("root");
		// ���headԪ��
		Element el_head = el_root.addElement("head");
		Element el_success = el_head.addElement("success");
		Element el_count = el_head.addElement("count");
		Element el_message = el_head.addElement("message");
		Element el_body = el_root.addElement("body");

		el_success.setText("1");//
		el_message.setText("ͳ�Ʋ�ѯ�ɹ�");//
		//�����ȡ��������򷵻ز�ѯ���
		el_count.setText("" + count);//��ѯ����
		
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
			<cphm1>��B12345 </cphm1>
						<cphm2>��B12346 </cphm2>
						<frequecy>0.5</ frequecy>
			</data>
			<data>
			<cphm1>��B12347cphm1</cphm1>
						<cphm2>��B12348cphm2</cphm2>
						<frequecy>0.6</ frequecy>
			</data>
			��
				</body>
			</root>
		 */
		
		for(int i=0;i<listC.size();i++){
			SbPz pt = listC.get(i);
			
			Element el_data = el_body.addElement("data");
			Element cphm1 = el_data.addElement("cphm1");
			Element cphm2 = el_data.addElement("cphm2");
			Element frequecy = el_data.addElement("frequency");
			
			//��ֵ
			cphm1.setText(pt.getCphm1());//
			cphm2.setText(pt.getCphm2());
			frequecy.setText(""+pt.getPropability());//
		}
		return doc.asXML();// ת�����ַ���
	}
}
