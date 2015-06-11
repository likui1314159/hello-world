package com.dyst.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.dyst.elasticsearch.ESsearcherFilter;
import com.dyst.oracle.Search;
import com.dyst.util.Config;
import com.dyst.util.InterUtil;
import com.dyst.util.XmlCreater;

public class SbUpdate {
	/**
	 * ����ʶ����¼
	 * @param xml
	 * @return
	 */
	public String updateSb(String xml){
		Document document = null;
		XmlCreater xmlcreate = new XmlCreater();
		Config config = Config.getInstance();//������Ϣ��
		
		//-----------1------------------��ȡ����-------------------------
//		String hpzl = null;
		String cphid = null;
		String cplx = null;
		String gcxh = null;
		String sbsj = null;
//		String jcdid = null;
		try {
			document = (Document) DocumentHelper.parseText(xml);
			Element root = document.getRootElement();
//			Element head = (Element) root.selectNodes("head").get(0);
			Element body = (Element) root.selectNodes("body").get(0);
			Element data = (Element) body.selectNodes("data").get(0);
			
//			hpzl = data.element("hpzl").getText();//��������
			cphid = data.element("hphm").getText();//���ƺ���
			cplx = data.element("cplx").getText();//��������
			gcxh = data.element("tpid").getText();//�������
			sbsj = data.element("sbsj").getText();//ʶ��ʱ��
//			jcdid = data.element("jcdid").getText();//����ID
//			business =businessType;//ҵ���ѯ����
		} catch (Exception e) {
			e.printStackTrace();
			return xmlcreate.createErrorXml(config.getErrorCode09());//"09:xml�ļ���ʽ�޷����������飡"
		}
		
		//-----------2-------------------------------------------
		//�жϸ���ʱ�䣬��ѯOracle���ݿ⻹��ES��
		Date midDate = null;//oracle��es�ֽ�ʱ��
		Date sbDate = null;//ʶ���¼ʱ��
		SimpleDateFormat dfsb = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			//---�м�ʱ��---
			midDate = df.parse(InterUtil.getTime(Integer.parseInt(config.getBeforeDate())));
//			System.out.println("�м�ʱ�䣺" + sdf.format(midDate));
		
			//---ʶ���¼ʱ��---
			try {
//				sbDate = dfsb.parse(gcxh.substring(0, 14));
				sbDate = dfsb.parse(sbsj);
//				System.out.println("ʶ���¼ʱ�䣺" + sdf.format(sbDate));
			} catch (ParseException e) {
				return xmlcreate.createErrorXml(config.getErrorCode03());//"03:ʱ���ʽ���Ϸ�����Ϊ�գ����飡"
			}
			
			//����ʱ��ǰ�󣬸���ES�⻹��Oracle��
			if(sbDate.before(midDate)){
				ESsearcherFilter es = new ESsearcherFilter();
			    try {
					es.updateEsSb(gcxh, cphid, cplx);
					return xmlcreate.createUpdateXml("���¼�¼�ɹ�");
				} catch (Exception e) {
					e.printStackTrace();
					return xmlcreate.createErrorXml(config.getErrorCode13());
				}
				
			}else{
				Search oracle = new Search();
			    try {
			    	oracle.updateOracleSb(gcxh, cphid, cplx);
					return xmlcreate.createUpdateXml("���¼�¼�ɹ�");
				} catch (Exception e) {
					return xmlcreate.createErrorXml(config.getErrorCode13());
				}
			}
		} catch (ParseException e) {
			return xmlcreate.createErrorXml(config.getErrorCode12());
		}
	}
}
