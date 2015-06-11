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
	 * 更新识别表记录
	 * @param xml
	 * @return
	 */
	public String updateSb(String xml){
		Document document = null;
		XmlCreater xmlcreate = new XmlCreater();
		Config config = Config.getInstance();//配置信息类
		
		//-----------1------------------获取参数-------------------------
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
			
//			hpzl = data.element("hpzl").getText();//号牌种类
			cphid = data.element("hphm").getText();//号牌号码
			cplx = data.element("cplx").getText();//车牌类型
			gcxh = data.element("tpid").getText();//过车序号
			sbsj = data.element("sbsj").getText();//识别时间
//			jcdid = data.element("jcdid").getText();//监测点ID
//			business =businessType;//业务查询类型
		} catch (Exception e) {
			e.printStackTrace();
			return xmlcreate.createErrorXml(config.getErrorCode09());//"09:xml文件格式无法解析，请检查！"
		}
		
		//-----------2-------------------------------------------
		//判断给定时间，查询Oracle数据库还是ES库
		Date midDate = null;//oracle与es分解时间
		Date sbDate = null;//识别记录时间
		SimpleDateFormat dfsb = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			//---中间时间---
			midDate = df.parse(InterUtil.getTime(Integer.parseInt(config.getBeforeDate())));
//			System.out.println("中间时间：" + sdf.format(midDate));
		
			//---识别记录时间---
			try {
//				sbDate = dfsb.parse(gcxh.substring(0, 14));
				sbDate = dfsb.parse(sbsj);
//				System.out.println("识别记录时间：" + sdf.format(sbDate));
			} catch (ParseException e) {
				return xmlcreate.createErrorXml(config.getErrorCode03());//"03:时间格式不合法或者为空，请检查！"
			}
			
			//根据时间前后，更新ES库还是Oracle库
			if(sbDate.before(midDate)){
				ESsearcherFilter es = new ESsearcherFilter();
			    try {
					es.updateEsSb(gcxh, cphid, cplx);
					return xmlcreate.createUpdateXml("更新记录成功");
				} catch (Exception e) {
					e.printStackTrace();
					return xmlcreate.createErrorXml(config.getErrorCode13());
				}
				
			}else{
				Search oracle = new Search();
			    try {
			    	oracle.updateOracleSb(gcxh, cphid, cplx);
					return xmlcreate.createUpdateXml("更新记录成功");
				} catch (Exception e) {
					return xmlcreate.createErrorXml(config.getErrorCode13());
				}
			}
		} catch (ParseException e) {
			return xmlcreate.createErrorXml(config.getErrorCode12());
		}
	}
}
