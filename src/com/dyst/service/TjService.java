package com.dyst.service;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;

import com.dyst.elasticsearch.util.ESFacter;
import com.dyst.elasticsearch.util.ESutil;
import com.dyst.util.Config;
import com.dyst.util.XmlCreater;

public class TjService {
	/**
	 * 
	 * @param xml
	 * @param businessType
	 * @param flag
	 * @return
	 */
	public String tjcx(String xml){
		Config config = Config.getInstance();//������Ϣ��
		Document document = null;
		XmlCreater xmlcreate = new XmlCreater();
		
		//��������
		String hphm = null;//���ƺ���
		String hpzl = null;//��������
		String cplx = null;//��������
		String jcdid = null;//����ID
		String gcxh = null;//������ţ�ͼƬid��
		String kssj = null;//��ʼʱ��
		String jssj = null;//��ֹʱ��
		String cd = null;//����
		String cb = null;//����
		String sd = null;//�ٶ�
		String hmdCphm = null;//���������ƺ���
		String business = null;//ҵ���ѯ����
		String sbzt = "" ;//ʶ��״̬����ʶ��δʶ��
		String groupName = "";//�����ֶ�
		try {
			document = (Document) DocumentHelper.parseText(xml);
			Element root = document.getRootElement();//��ȡ���ڵ�
			Element head = (Element) root.selectNodes("head").get(0);
			Element body = (Element) root.selectNodes("body").get(0);
			Element data = (Element) body.selectNodes("data").get(0);
			
			kssj = data.element("kssj").getText();//��ʼʱ��
			jssj = data.element("jssj").getText();//��ֹʱ��
			hpzl = data.element("hpzl").getText();//��������
			hphm = data.element("hphm").getText();//���ƺ���
			cplx = data.element("cplx").getText();//��������
			gcxh = data.element("tpid").getText();//�������
			jcdid = data.element("jcdid").getText();//����ID
			cd = data.element("cd").getText();//����
			cb = data.element("cb").getText();//����
			sd = data.element("sd").getText();//�ٶ�
			hmdCphm = data.element("hmdCphm").getText();//���������ƺ���
			
			sbzt =  head.element("sbzt").getText();//ʶ��״̬
			groupName = head.element("groupName").getText();//�����ֶ�
			business = head.element("type").getText();//��ѯҵ������
		} catch (Exception e) {
			//"09:xml�ļ���ʽ�޷����������飡"
			e.printStackTrace();
			return  xmlcreate.createErrorXml(config.getErrorCode09());
		}
		try {
			BoolQueryBuilder query = ESutil.getFacterQuery(kssj, jssj, hpzl,
						hphm, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, sbzt);
			ESFacter es = new ESFacter();
			return xmlcreate.createTjxml(es.facetByCon(query, groupName));
		} catch (Exception e) {
			e.printStackTrace();
			return  xmlcreate.createErrorXml(config.getErrorCode08());
		}
	}
}
