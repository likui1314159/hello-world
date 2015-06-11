//package com.dyst.service;
//
//import java.sql.SQLException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//
//import org.dom4j.Document;
//import org.dom4j.DocumentException;
//import org.dom4j.DocumentHelper;
//import org.dom4j.Element;
//import org.elasticsearch.index.query.FilterBuilder;
//import org.elasticsearch.search.SearchHits;
//
//import com.dyst.elasticsearch.ESsearcherFilter;
//import com.dyst.elasticsearch.util.ESCountThread;
//import com.dyst.elasticsearch.util.ESThread;
//import com.dyst.elasticsearch.util.ESutil;
//import com.dyst.oracle.OracleCountThread;
//import com.dyst.oracle.OracleThread;
//import com.dyst.oracle.Search;
//import com.dyst.oracle.SearchInfo;
//import com.dyst.util.Config;
//import com.dyst.util.InterUtil;
//import com.dyst.util.PicThread;
//import com.dyst.util.XmlCreater;
//
//public class ClientService {
//
//	/**
//	 * �켣��ѯ����������ҵ����������
//	 * 
//	 * @param xml
//	 *            XML������
//	 * @param businessType
//	 *            ��ѯҵ������
//	 * @param flag
//	 *            1:���ؼ�¼��0:���ؼ�¼������ ����ʶ���¼���Ǽ�¼����
//	 * @param ���ؼ�¼��������ʶ���¼
//	 */
//	@SuppressWarnings("unchecked")
//	public String gjcx(String xml, String businessType, String flag) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		Document document = null;
//		XmlCreater xmlcreate = new XmlCreater();
//		
//		//��������
//		String cphid = null;//���ƺ���
//		String hpzl = null;//��������
//		String cplx = null;//��������
//		String jcdid = null;//����ID
//		String gcxh = null;//������ţ�ͼƬid��
//		String kssj = null;//��ʼʱ��
//		String jssj = null;//��ֹʱ��
//		String cd = null;//����
//		String cb = null;//����
//		String sd = null;//�ٶ�
//		String hmdCphm = null;//���������ƺ���
//		String strPage = null;//ҳ����ʾ��¼��
//		String strFrom = null;//��ʼ��¼��
//		String business = null;//ҵ���ѯ����
//		
//		Config config = Config.getInstance();// ������Ϣ��
//		try {
//			document = (Document) DocumentHelper.parseText(xml);
//			Element root = document.getRootElement();
//			Element head = (Element) root.selectNodes("head").get(0);
//			Element body = (Element) root.selectNodes("body").get(0);
//			Element data = (Element) body.selectNodes("data").get(0);
//
//			strPage = head.element("pagesize").getText();// ҳ����ʾ��¼��
//			strFrom = head.element("from").getText();// ��ʼ��¼��
//			kssj = data.element("kssj").getText();// ��ʼʱ��
//			jssj = data.element("jssj").getText();// ��ֹʱ��
//			hpzl = data.element("hpzl").getText();// ��������
//			cphid = data.element("hphm").getText();// ���ƺ���
//			cplx = data.element("cplx").getText();// ��������
//			gcxh = data.element("tpid").getText();// �������
//			jcdid = data.element("jcdid").getText();// ����ID
//			cd = data.element("cd").getText();//����
//			cb = data.element("cb").getText();//����
//			sd = data.element("sd").getText();//�ٶ�
//			hmdCphm = data.element("hmdCphm").getText();//���������ƺ���
//			business = businessType;// ҵ���ѯ����
//		} catch (Exception e) {
//			// "09:xml�ļ���ʽ�޷����������飡"
//			return xmlcreate.createErrorXml(config.getErrorCode09());
//		}
//
//		// �жϸ���ʱ�䣬��ѯOracle���ݿ⻹��ES��
//		Date midDate = null;
//		Date ksDate = null;
//		Date jzDate = null;
//		try {
//			// Oracle���ES���ѯ�ֽ��,,,����������
//			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//			midDate = df.parse(InterUtil.getTime(Integer.parseInt(config
//					.getBeforeDate())));
//			System.out.println("�м�ʱ�䣺" + sdf.format(midDate));
//			ksDate = sdf.parse(kssj);// ��ʼʱ��
//			jzDate = sdf.parse(jssj);// ����ʱ��
//		} catch (ParseException e) {
//			// "03:ʱ���ʽ���Ϸ�����Ϊ�գ����飡"
//			return xmlcreate.createErrorXml(config.getErrorCode03());
//		}
//		List listtx = new ArrayList();
//		SearchHits hits = null;
//
//		int queryCount = 0;// �����ѯ��������
//		int allowCount = Integer.parseInt(config.getMaxCount());// ��������ؼ�¼����
//		int pageCount = Integer.parseInt(config.getPageCount());
//		// ��ҳ��ѯ��������صļ�¼��
//		SearchInfo sqlInfo = new SearchInfo();// oracle��sql���������
//		Search search = new Search();// Oracleʵ�ֲ�ѯ��
//		ESsearcherFilter essearch = new ESsearcherFilter();
//		FilterBuilder filter = null;
//		CountDownLatch threadsSignal;
//		int pagesize = 0;
//		int from = 0;
//		if (strPage != null && !"".equals(strPage)) {
//			try {
//				pagesize = Integer.parseInt(strPage);
//				from = Integer.parseInt(strFrom);
//				if (pagesize > 0 && pagesize > pageCount) {// ����ҳ��¼��
//					// "05:��ҳ��ѯ��¼�������������ֵ��"
//					return xmlcreate.createErrorXml(config.getErrorCode05());
//				} else if (pagesize < 0 || from < 0) {
//					// "11:��ҳ�������Ϸ���"
//					return xmlcreate.createErrorXml(config.getErrorCode11());
//				}
//			} catch (Exception e) {
//				// 08:������ҳ���������쳣
//				return xmlcreate.createErrorXml(config.getErrorCode08());
//			}
//		}
//
//		if (ksDate.after(midDate)) {// �����ʼʱ����ڵ�ǰʱ���ȥn��ǰ��ʱ�䣬��ֻ��ѯoracle��
//			// Oracle����
//			System.out.println("��ѯOracle");
//			try {
//				if ("0".equals(flag.trim())) {// ��ѯ���Ͻ��������
//					queryCount = search.getTDCPGJCXCount(sqlInfo.getSqlByCon(
//							kssj, jssj, hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm,
//							business, "1"));
//					return xmlcreate.createCountXml(queryCount, 0);
//				}
//
//				if (pagesize > 0) {// ��ҳ��ѯ
//					listtx = search.TDCPGJCX(sqlInfo.getSqlByCon(kssj, jssj,
//							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0"),
//							from, pagesize);
//				} else {// ���ط��ϲ�ѯ��������������
//					queryCount = search.getTDCPGJCXCount(sqlInfo.getSqlByCon(
//							kssj, jssj, hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, 
//							business, "1"));
//					if (queryCount > allowCount) {// ��ѯ�����������������
//						// "07:��ѯ�����Ϊ"+queryCount+"����������������������С������Χ��
//						return xmlcreate
//								.createErrorXml(config.getErrorCode07());
//					} else {
//						listtx = search.TDCPGJCX(sqlInfo.getSqlByCon(kssj,
//								jssj, hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business,
//								"0"), 0, queryCount);
//					}
//				}
//			} catch (SQLException e) {
//				// "04:��ѯ���ݿ�����쳣������ϵ����Ա��"
//				return xmlcreate.createErrorXml(config.getErrorCode04());
//			}
//			return xmlcreate.createXml(listtx, null);
//		} else if (jzDate.before(midDate)) {// �����ֹʱ��С�ڵ�ǰʱ���ȥN��ǰ��ʱ�䣬��ֻ��ѯES��
//			// ����FilterBuilder filter
//			System.out.println("��ѯES��");
//			filter = ESutil.getFilterByCon(kssj, jssj, hpzl, cphid, cplx, gcxh,
//					jcdid, hmdCphm, business);
//
//			try {
//				if ("0".equals(flag.trim())) {// ��ѯ���Ͻ��������
//					queryCount = essearch.getTdcpgjcxCount(filter, business);
//					return xmlcreate.createCountXml(0, queryCount);
//				}
//
//				if (pagesize > 0) {// ��ҳ��ѯ
//					hits = essearch.tdcpgjcx(filter, from, pagesize, business);
//				} else {// ���ط������ݵ�ȫ����¼
//					queryCount = essearch.getTdcpgjcxCount(filter, business);
//					if (queryCount > allowCount) {// ��ѯ�����������������
//						// "07:��ѯ�����Ϊ"+queryCount+"����������������������С������Χ��"
//						return xmlcreate
//								.createErrorXml(config.getErrorCode07());
//					} else {
//						hits = essearch.tdcpgjcx(filter, 0, queryCount,
//								business);// ͨ��filter��ѯES��
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				// "04:��ѯ���ݿ�����쳣������ϵ����Ա��"
//				return xmlcreate.createErrorXml(config.getErrorCode04());
//			}
//			return xmlcreate.createXml(null, hits);
//		} else {// ��ѯ������
//			try {
//				if ("0".equals(flag.trim())) {// ��ѯ�������ݿ���Ͻ��������
//					String oraCountSql = sqlInfo.getSqlByCon(sdf
//							.format(midDate), jssj, hpzl, cphid, cplx, gcxh,
//							jcdid, cd, cb, sd, hmdCphm, business, "1");
//					// ES//��ѯ����ʱ��Ϊ�м��ʱ��
//					filter = ESutil.getFilterByCon(kssj, sdf.format(midDate),
//							hpzl, cphid, cplx, gcxh, jcdid, hmdCphm, business);
//					// ��ѯoracle�ͣţӿ����������¼����
//					threadsSignal = new CountDownLatch(2);// ���������߳���
//					ESCountThread escount = new ESCountThread(threadsSignal,
//							filter, business);// ��ѯ��¼����
//					escount.start();
//					OracleCountThread oraclecount = new OracleCountThread(
//							threadsSignal, oraCountSql);// ִ��oracle�߳�
//					oraclecount.start();
//					threadsSignal.await();// �ȴ�Oracle��ES���ѯ��ϡ�
//					int oraCou = oraclecount.count;
//					int esCou = escount.count;
//					return xmlcreate.createCountXml(oraCou, esCou);
//				}
//
//				if (pagesize > 0) {
//					/**
//					 * ��ҳ��ѯ 1.���Ȳ�ѯOracle���ݿ���������ļ�¼����queryCount��
//					 * 2.���ݸ�����fromֵ��pagesizeֵ�����from>=queryCount,��ֱ�Ӳ�ѯ�ţӿ⣬
//					 * ���queryCount>=from+pagesize,��ֻ��ѯOralce���ݿ⣬������Ҫ��ѯ�������ݿ⣻
//					 * 3.�����Ҫ��ѯ�������ݿ�
//					 * ��Oracle���ݿ��ѯ��ΧΪfrom��queryCount,ES���ѯ��ΧΪ0��pagesize
//					 * -(queryCount-from)
//					 */
//					// Oracle����������¼��
//					queryCount = search.getTDCPGJCXCount(sqlInfo.getSqlByCon(
//							sdf.format(midDate), jssj, hpzl, cphid, cplx, gcxh,
//							jcdid, cd, cb, sd, hmdCphm, business, "1"));
//					filter = ESutil.getFilterByCon(kssj, sdf.format(midDate),
//							hpzl, cphid, cplx, gcxh, jcdid, hmdCphm, business);
//
//					if (queryCount >= from + pagesize) {// ��ѯORACLE��
//						listtx = search.TDCPGJCX(sqlInfo.getSqlByCon(sdf
//								.format(midDate), jssj, hpzl, cphid, cplx,
//								gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0"), from, pagesize);
//						return xmlcreate.createXml(listtx, null);
//					} else if (from >= queryCount) {// ��ѯES��
//						hits = essearch.tdcpgjcx(filter, from, pagesize,
//								businessType);
//						return xmlcreate.createXml(null, hits);
//					} else {
//						// ��ѯ������
//						threadsSignal = new CountDownLatch(2);// ���������߳���
//						ESThread es = new ESThread(threadsSignal, filter, 0,
//								pagesize - (queryCount - from), business);// ִ��Es�߳�
//						es.start();
//						// System.out.println("ES+��ѯ��¼����");
//						String sql = sqlInfo.getSqlByCon(sdf.format(midDate),
//								jssj, hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business,
//								"0");
//						OracleThread oracle = new OracleThread(threadsSignal,
//								sql, from, queryCount - from);// ִ��oracle�߳�
//						oracle.start();
//						threadsSignal.await();// �ȴ�Oracle��ES���ѯ��ϡ�
//						return xmlcreate.createXml(oracle.listtx, es.hits);
//						// listtx = search.TDCPGJCX(sqlInfo.getSqlByCon(kssj,
//						// jssj, hpzl, cphid, cplx, gcxh, jcdid,
//						// business,"0"),from,queryCount-from);
//						// hits
//						// =essearch.tdcpgjcx(filter,0,pagesize-(queryCount-from));
//						// return xmlcreate.createXml(listtx,hits);
//					}
//
//				} else {
//					/**
//					 *�������з��������ļ�¼ 1.��������ѯ����������������ļ�¼���������¼����������ֵ�������쳣��Ϣ��
//					 * 2.�ڲ�ѯ�������¼�ۺϺͼ�¼ʱ��ʹ���̲߳���������ͬʱ��ѯ�������ݿ���Ϣ�������Ӧʱ�䣻
//					 */
//					// ��ѯ��ʼʱ��Ϊ�м�ʱ���
//					String sql = sqlInfo.getSqlByCon(sdf.format(midDate), jssj,
//							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0");
//					String oraCountSql = sqlInfo.getSqlByCon(sdf
//							.format(midDate), jssj, hpzl, cphid, cplx, gcxh,
//							jcdid, cd, cb, sd, hmdCphm, business, "1");
//					// ES//��ѯ����ʱ��Ϊ�м��ʱ��
//					filter = ESutil.getFilterByCon(kssj, sdf.format(midDate),
//							hpzl, cphid, cplx, gcxh, jcdid, hmdCphm, business);
//					// ��ѯoracle�ͣţӿ����������¼����
//					threadsSignal = new CountDownLatch(2);// ���������߳���
//					ESCountThread escount = new ESCountThread(threadsSignal,
//							filter, business);// ��ѯ��¼����
//					escount.start();
//					OracleCountThread oraclecount = new OracleCountThread(
//							threadsSignal, oraCountSql);// ִ��oracle�߳�
//					oraclecount.start();
//					threadsSignal.await();// �ȴ�Oracle��ES���ѯ��ϡ�
//					int oraCou = oraclecount.count;
//					int esCou = escount.count;
//					queryCount = oraCou + esCou;
//
//					if (queryCount > allowCount) {// ��ѯ�����������������
//						// "07:��ѯ�����Ϊ"+queryCount+"����������������������С������Χ��"
//						return xmlcreate
//								.createErrorXml(config.getErrorCode07());
//					} else {
//						threadsSignal = new CountDownLatch(2);// ���������߳���
//						ESThread es = new ESThread(threadsSignal, filter, 0,
//								esCou, business);// ִ��Es�߳�
//						es.start();
//						// System.out.println("ES+��ѯ��¼����");
//						OracleThread oracle = new OracleThread(threadsSignal,
//								sql, 0, oraCou);// ִ��oracle�߳�
//						oracle.start();
//						threadsSignal.await();// �ȴ�Oracle��ES���ѯ��ϡ�
//						System.out.println("");
//						return xmlcreate.createXml(oracle.listtx, es.hits);
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				// "06:��ѯ�����쳣������ϵ����Ա��"
//				return xmlcreate.createErrorXml(config.getErrorCode06());
//			}
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	public String tpcx(String xml) {
//		Config config = Config.getInstance();// ������Ϣ��
//		Document document = null;
//		String tpid = "";
//		List<Element> listTpid = new ArrayList<Element>();// ͼƬid����
//		XmlCreater xmlcreate = new XmlCreater();
//		CountDownLatch threadsSignal;
//		List listPic = new ArrayList();// ͼƬ��ַ����
//		Element body = null;
//		try {
//			document = (Document) DocumentHelper.parseText(xml);
//			Element root = document.getRootElement();
//			body = (Element) root.selectNodes("body").get(0);
//			listTpid = body.selectNodes("tpid");
//		} catch (DocumentException e) {
//			// "09����XML�ļ������쳣
//			return xmlcreate.createErrorXml(config.getErrorCode09());
//		}// �ַ���ת��Ϊdocument�ļ���
//		if (listTpid == null || listTpid.size() == 0) {
//			// "10:����ͼƬid�޷�������"
//			return xmlcreate.createErrorXml(config.getErrorCode10());
//		}
//		int threadNum = 3;// ÿ�������߳���
//		try {
//			threadNum = Integer.parseInt(config.getNumThread().trim());
//		} catch (Exception e) {
//		}
//		int len = listTpid.size();// ͼƬ��
//		for (int j = 0; j < len; j += threadNum) {
//			if (len - j < threadNum) {
//				threadsSignal = new CountDownLatch(len - j);// ����en-beg���߳�,������threadNum��ʱ
//			} else {
//				threadsSignal = new CountDownLatch(threadNum);// ����threadNum���߳�
//			}
//			for (int i = j; i < listTpid.size() && i < j + threadNum; i++) {
//				tpid = ((Element) listTpid.get(i)).getText();
//				PicThread index = new PicThread(threadsSignal, tpid, "1",
//						listPic);
//				index.start();
//			}
//			try {
//				threadsSignal.await();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}// �ȴ��߳���ִ�����
//		}
//		return xmlcreate.createPicPath(listPic);
//	}
//}
