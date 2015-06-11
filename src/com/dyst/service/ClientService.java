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
//	 * 轨迹查询方法，按照业务类型区分
//	 * 
//	 * @param xml
//	 *            XML请求报文
//	 * @param businessType
//	 *            查询业务类型
//	 * @param flag
//	 *            1:返回记录，0:返回记录总数， 返回识别记录还是记录总数
//	 * @param 返回记录总数还是识别记录
//	 */
//	@SuppressWarnings("unchecked")
//	public String gjcx(String xml, String businessType, String flag) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		Document document = null;
//		XmlCreater xmlcreate = new XmlCreater();
//		
//		//条件变量
//		String cphid = null;//号牌号码
//		String hpzl = null;//号牌种类
//		String cplx = null;//车牌类型
//		String jcdid = null;//监测点ID
//		String gcxh = null;//过车序号（图片id）
//		String kssj = null;//开始时间
//		String jssj = null;//截止时间
//		String cd = null;//车道
//		String cb = null;//车标
//		String sd = null;//速度
//		String hmdCphm = null;//红名单车牌号码
//		String strPage = null;//页面显示记录数
//		String strFrom = null;//起始记录数
//		String business = null;//业务查询类型
//		
//		Config config = Config.getInstance();// 配置信息类
//		try {
//			document = (Document) DocumentHelper.parseText(xml);
//			Element root = document.getRootElement();
//			Element head = (Element) root.selectNodes("head").get(0);
//			Element body = (Element) root.selectNodes("body").get(0);
//			Element data = (Element) body.selectNodes("data").get(0);
//
//			strPage = head.element("pagesize").getText();// 页面显示记录数
//			strFrom = head.element("from").getText();// 起始记录数
//			kssj = data.element("kssj").getText();// 起始时间
//			jssj = data.element("jssj").getText();// 截止时间
//			hpzl = data.element("hpzl").getText();// 号牌种类
//			cphid = data.element("hphm").getText();// 号牌号码
//			cplx = data.element("cplx").getText();// 车牌类型
//			gcxh = data.element("tpid").getText();// 过车序号
//			jcdid = data.element("jcdid").getText();// 监测点ID
//			cd = data.element("cd").getText();//车道
//			cb = data.element("cb").getText();//车标
//			sd = data.element("sd").getText();//速度
//			hmdCphm = data.element("hmdCphm").getText();//红名单车牌号码
//			business = businessType;// 业务查询类型
//		} catch (Exception e) {
//			// "09:xml文件格式无法解析，请检查！"
//			return xmlcreate.createErrorXml(config.getErrorCode09());
//		}
//
//		// 判断给定时间，查询Oracle数据库还是ES库
//		Date midDate = null;
//		Date ksDate = null;
//		Date jzDate = null;
//		try {
//			// Oracle库和ES库查询分界点,,,参数可配置
//			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//			midDate = df.parse(InterUtil.getTime(Integer.parseInt(config
//					.getBeforeDate())));
//			System.out.println("中间时间：" + sdf.format(midDate));
//			ksDate = sdf.parse(kssj);// 开始时间
//			jzDate = sdf.parse(jssj);// 结束时间
//		} catch (ParseException e) {
//			// "03:时间格式不合法或者为空，请检查！"
//			return xmlcreate.createErrorXml(config.getErrorCode03());
//		}
//		List listtx = new ArrayList();
//		SearchHits hits = null;
//
//		int queryCount = 0;// 满足查询条件总数
//		int allowCount = Integer.parseInt(config.getMaxCount());// 最大允许返回记录总数
//		int pageCount = Integer.parseInt(config.getPageCount());
//		// 分页查询最大允许返回的记录数
//		SearchInfo sqlInfo = new SearchInfo();// oracle库sql语句生成类
//		Search search = new Search();// Oracle实现查询类
//		ESsearcherFilter essearch = new ESsearcherFilter();
//		FilterBuilder filter = null;
//		CountDownLatch threadsSignal;
//		int pagesize = 0;
//		int from = 0;
//		if (strPage != null && !"".equals(strPage)) {
//			try {
//				pagesize = Integer.parseInt(strPage);
//				from = Integer.parseInt(strFrom);
//				if (pagesize > 0 && pagesize > pageCount) {// 检查分页记录数
//					// "05:分页查询记录数超过最大允许值！"
//					return xmlcreate.createErrorXml(config.getErrorCode05());
//				} else if (pagesize < 0 || from < 0) {
//					// "11:分页参数不合法！"
//					return xmlcreate.createErrorXml(config.getErrorCode11());
//				}
//			} catch (Exception e) {
//				// 08:解析分页参数出现异常
//				return xmlcreate.createErrorXml(config.getErrorCode08());
//			}
//		}
//
//		if (ksDate.after(midDate)) {// 如果开始时间大于当前时间减去n天前的时间，则只查询oracle库
//			// Oracle调用
//			System.out.println("查询Oracle");
//			try {
//				if ("0".equals(flag.trim())) {// 查询符合结果的总数
//					queryCount = search.getTDCPGJCXCount(sqlInfo.getSqlByCon(
//							kssj, jssj, hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm,
//							business, "1"));
//					return xmlcreate.createCountXml(queryCount, 0);
//				}
//
//				if (pagesize > 0) {// 分页查询
//					listtx = search.TDCPGJCX(sqlInfo.getSqlByCon(kssj, jssj,
//							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0"),
//							from, pagesize);
//				} else {// 返回符合查询条件的所有数据
//					queryCount = search.getTDCPGJCXCount(sqlInfo.getSqlByCon(
//							kssj, jssj, hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, 
//							business, "1"));
//					if (queryCount > allowCount) {// 查询结果集超过给定数。
//						// "07:查询结果集为"+queryCount+"条，超出允许条数，请缩小检索范围！
//						return xmlcreate
//								.createErrorXml(config.getErrorCode07());
//					} else {
//						listtx = search.TDCPGJCX(sqlInfo.getSqlByCon(kssj,
//								jssj, hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business,
//								"0"), 0, queryCount);
//					}
//				}
//			} catch (SQLException e) {
//				// "04:查询数据库出现异常，请联系管理员！"
//				return xmlcreate.createErrorXml(config.getErrorCode04());
//			}
//			return xmlcreate.createXml(listtx, null);
//		} else if (jzDate.before(midDate)) {// 如果截止时间小于当前时间减去N天前的时间，则只查询ES库
//			// 生成FilterBuilder filter
//			System.out.println("查询ES库");
//			filter = ESutil.getFilterByCon(kssj, jssj, hpzl, cphid, cplx, gcxh,
//					jcdid, hmdCphm, business);
//
//			try {
//				if ("0".equals(flag.trim())) {// 查询符合结果的总数
//					queryCount = essearch.getTdcpgjcxCount(filter, business);
//					return xmlcreate.createCountXml(0, queryCount);
//				}
//
//				if (pagesize > 0) {// 分页查询
//					hits = essearch.tdcpgjcx(filter, from, pagesize, business);
//				} else {// 返回符合数据的全部记录
//					queryCount = essearch.getTdcpgjcxCount(filter, business);
//					if (queryCount > allowCount) {// 查询结果集超过给定数。
//						// "07:查询结果集为"+queryCount+"条，超出允许条数，请缩小检索范围！"
//						return xmlcreate
//								.createErrorXml(config.getErrorCode07());
//					} else {
//						hits = essearch.tdcpgjcx(filter, 0, queryCount,
//								business);// 通过filter查询ES库
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				// "04:查询数据库出现异常，请联系管理员！"
//				return xmlcreate.createErrorXml(config.getErrorCode04());
//			}
//			return xmlcreate.createXml(null, hits);
//		} else {// 查询两个库
//			try {
//				if ("0".equals(flag.trim())) {// 查询两个数据库符合结果的总数
//					String oraCountSql = sqlInfo.getSqlByCon(sdf
//							.format(midDate), jssj, hpzl, cphid, cplx, gcxh,
//							jcdid, cd, cb, sd, hmdCphm, business, "1");
//					// ES//查询结束时间为中间点时间
//					filter = ESutil.getFilterByCon(kssj, sdf.format(midDate),
//							hpzl, cphid, cplx, gcxh, jcdid, hmdCphm, business);
//					// 查询oracle和ＥＳ库符合条件记录总数
//					threadsSignal = new CountDownLatch(2);// 创建两个线程组
//					ESCountThread escount = new ESCountThread(threadsSignal,
//							filter, business);// 查询记录总数
//					escount.start();
//					OracleCountThread oraclecount = new OracleCountThread(
//							threadsSignal, oraCountSql);// 执行oracle线程
//					oraclecount.start();
//					threadsSignal.await();// 等待Oracle和ES库查询完毕。
//					int oraCou = oraclecount.count;
//					int esCou = escount.count;
//					return xmlcreate.createCountXml(oraCou, esCou);
//				}
//
//				if (pagesize > 0) {
//					/**
//					 * 分页查询 1.首先查询Oracle数据库符合条件的记录总数queryCount；
//					 * 2.根据给定的from值和pagesize值，如果from>=queryCount,则直接查询ＥＳ库，
//					 * 如果queryCount>=from+pagesize,则只查询Oralce数据库，否则需要查询两个数据库；
//					 * 3.如果需要查询两个数据库
//					 * ，Oracle数据库查询范围为from至queryCount,ES库查询范围为0至pagesize
//					 * -(queryCount-from)
//					 */
//					// Oracle符合条件记录数
//					queryCount = search.getTDCPGJCXCount(sqlInfo.getSqlByCon(
//							sdf.format(midDate), jssj, hpzl, cphid, cplx, gcxh,
//							jcdid, cd, cb, sd, hmdCphm, business, "1"));
//					filter = ESutil.getFilterByCon(kssj, sdf.format(midDate),
//							hpzl, cphid, cplx, gcxh, jcdid, hmdCphm, business);
//
//					if (queryCount >= from + pagesize) {// 查询ORACLE库
//						listtx = search.TDCPGJCX(sqlInfo.getSqlByCon(sdf
//								.format(midDate), jssj, hpzl, cphid, cplx,
//								gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0"), from, pagesize);
//						return xmlcreate.createXml(listtx, null);
//					} else if (from >= queryCount) {// 查询ES库
//						hits = essearch.tdcpgjcx(filter, from, pagesize,
//								businessType);
//						return xmlcreate.createXml(null, hits);
//					} else {
//						// 查询两个库
//						threadsSignal = new CountDownLatch(2);// 创建两个线程组
//						ESThread es = new ESThread(threadsSignal, filter, 0,
//								pagesize - (queryCount - from), business);// 执行Es线程
//						es.start();
//						// System.out.println("ES+查询记录总数");
//						String sql = sqlInfo.getSqlByCon(sdf.format(midDate),
//								jssj, hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business,
//								"0");
//						OracleThread oracle = new OracleThread(threadsSignal,
//								sql, from, queryCount - from);// 执行oracle线程
//						oracle.start();
//						threadsSignal.await();// 等待Oracle和ES库查询完毕。
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
//					 *返回所有符合条件的记录 1.按条件查询出两个库符合条件的记录数，如果记录数超过给定值，返回异常信息；
//					 * 2.在查询两个库记录综合和记录时，使用线程并发技术，同时查询两个数据库信息，提高响应时间；
//					 */
//					// 查询起始时间为中间时间点
//					String sql = sqlInfo.getSqlByCon(sdf.format(midDate), jssj,
//							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0");
//					String oraCountSql = sqlInfo.getSqlByCon(sdf
//							.format(midDate), jssj, hpzl, cphid, cplx, gcxh,
//							jcdid, cd, cb, sd, hmdCphm, business, "1");
//					// ES//查询结束时间为中间点时间
//					filter = ESutil.getFilterByCon(kssj, sdf.format(midDate),
//							hpzl, cphid, cplx, gcxh, jcdid, hmdCphm, business);
//					// 查询oracle和ＥＳ库符合条件记录总数
//					threadsSignal = new CountDownLatch(2);// 创建两个线程组
//					ESCountThread escount = new ESCountThread(threadsSignal,
//							filter, business);// 查询记录总数
//					escount.start();
//					OracleCountThread oraclecount = new OracleCountThread(
//							threadsSignal, oraCountSql);// 执行oracle线程
//					oraclecount.start();
//					threadsSignal.await();// 等待Oracle和ES库查询完毕。
//					int oraCou = oraclecount.count;
//					int esCou = escount.count;
//					queryCount = oraCou + esCou;
//
//					if (queryCount > allowCount) {// 查询结果集超过给定数。
//						// "07:查询结果集为"+queryCount+"条，超出允许条数，请缩小检索范围！"
//						return xmlcreate
//								.createErrorXml(config.getErrorCode07());
//					} else {
//						threadsSignal = new CountDownLatch(2);// 创建两个线程组
//						ESThread es = new ESThread(threadsSignal, filter, 0,
//								esCou, business);// 执行Es线程
//						es.start();
//						// System.out.println("ES+查询记录总数");
//						OracleThread oracle = new OracleThread(threadsSignal,
//								sql, 0, oraCou);// 执行oracle线程
//						oracle.start();
//						threadsSignal.await();// 等待Oracle和ES库查询完毕。
//						System.out.println("");
//						return xmlcreate.createXml(oracle.listtx, es.hits);
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				// "06:查询出现异常，请联系管理员！"
//				return xmlcreate.createErrorXml(config.getErrorCode06());
//			}
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	public String tpcx(String xml) {
//		Config config = Config.getInstance();// 配置信息类
//		Document document = null;
//		String tpid = "";
//		List<Element> listTpid = new ArrayList<Element>();// 图片id集合
//		XmlCreater xmlcreate = new XmlCreater();
//		CountDownLatch threadsSignal;
//		List listPic = new ArrayList();// 图片地址集合
//		Element body = null;
//		try {
//			document = (Document) DocumentHelper.parseText(xml);
//			Element root = document.getRootElement();
//			body = (Element) root.selectNodes("body").get(0);
//			listTpid = body.selectNodes("tpid");
//		} catch (DocumentException e) {
//			// "09解析XML文件出现异常
//			return xmlcreate.createErrorXml(config.getErrorCode09());
//		}// 字符串转换为document文件。
//		if (listTpid == null || listTpid.size() == 0) {
//			// "10:给定图片id无法解析！"
//			return xmlcreate.createErrorXml(config.getErrorCode10());
//		}
//		int threadNum = 3;// 每次启动线程数
//		try {
//			threadNum = Integer.parseInt(config.getNumThread().trim());
//		} catch (Exception e) {
//		}
//		int len = listTpid.size();// 图片数
//		for (int j = 0; j < len; j += threadNum) {
//			if (len - j < threadNum) {
//				threadsSignal = new CountDownLatch(len - j);// 创建en-beg个线程,当不足threadNum个时
//			} else {
//				threadsSignal = new CountDownLatch(threadNum);// 创建threadNum个线程
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
//			}// 等待线程组执行完成
//		}
//		return xmlcreate.createPicPath(listPic);
//	}
//}
