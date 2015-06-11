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
		Config config = Config.getInstance();//配置信息类
		Document document = null;
		XmlCreater xmlcreate = new XmlCreater();
		
		//条件变量
		String hphm = null;//号牌号码
		String hpzl = null;//号牌种类
		String cplx = null;//车牌类型
		String jcdid = null;//监测点ID
		String gcxh = null;//过车序号（图片id）
		String kssj = null;//开始时间
		String jssj = null;//截止时间
		String cd = null;//车道
		String cb = null;//车标
		String sd = null;//速度
		String hmdCphm = null;//红名单车牌号码
		String business = null;//业务查询类型
		String sbzt = "" ;//识别状态，已识别，未识别
		String groupName = "";//分组字段
		try {
			document = (Document) DocumentHelper.parseText(xml);
			Element root = document.getRootElement();//获取根节点
			Element head = (Element) root.selectNodes("head").get(0);
			Element body = (Element) root.selectNodes("body").get(0);
			Element data = (Element) body.selectNodes("data").get(0);
			
			kssj = data.element("kssj").getText();//起始时间
			jssj = data.element("jssj").getText();//截止时间
			hpzl = data.element("hpzl").getText();//号牌种类
			hphm = data.element("hphm").getText();//号牌号码
			cplx = data.element("cplx").getText();//车牌类型
			gcxh = data.element("tpid").getText();//过车序号
			jcdid = data.element("jcdid").getText();//监测点ID
			cd = data.element("cd").getText();//车道
			cb = data.element("cb").getText();//车标
			sd = data.element("sd").getText();//速度
			hmdCphm = data.element("hmdCphm").getText();//红名单车牌号码
			
			sbzt =  head.element("sbzt").getText();//识别状态
			groupName = head.element("groupName").getText();//分组字段
			business = head.element("type").getText();//查询业务类型
		} catch (Exception e) {
			//"09:xml文件格式无法解析，请检查！"
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
