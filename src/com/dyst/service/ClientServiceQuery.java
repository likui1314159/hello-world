package com.dyst.service;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.search.SearchHits;

import com.dyst.elasticsearch.ESsearcherFilter;
import com.dyst.elasticsearch.util.ESCountThreadByQuery;
import com.dyst.elasticsearch.util.ESThreadByQuery;
import com.dyst.elasticsearch.util.ESutil;
import com.dyst.oracle.OracleCountThread;
import com.dyst.oracle.OracleThread;
import com.dyst.oracle.Search;
import com.dyst.oracle.SearchInfo;
import com.dyst.util.Config;
import com.dyst.util.InterUtil;
import com.dyst.util.PicThread;
import com.dyst.util.XmlCreater;
import com.dyst.entites.Sbnew;
public class ClientServiceQuery {
	
	/**
	 * 轨迹查询方法，按照业务类型区分
	 * @param xml           XML请求报文
	 * @param businessType  查询业务类型
	 * @param flag          1:返回记录，0:返回记录总数，  返回识别记录还是记录总数
	 * @param 返回记录总数还是识别记录
	 */
	public String gjcx(String xml, String businessType, String flag){
		Config config = Config.getInstance();//配置信息类
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Document document = null;
		XmlCreater xmlcreate = new XmlCreater();
		
		//条件变量
		String cphid = null;//号牌号码
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
		String strPage = null;//页面显示记录数
		String strFrom = null;//起始记录数
		String business = null;//业务查询类型
		String sort = "";//排序字段
		String sortType = "" ;//排序字段排序顺序，降序或升序
		
		try {
			document = (Document) DocumentHelper.parseText(xml);
			Element root = document.getRootElement();//获取根节点
			Element head = (Element) root.selectNodes("head").get(0);
			Element body = (Element) root.selectNodes("body").get(0);
			Element data = (Element) body.selectNodes("data").get(0);
			
			strPage = head.element("pagesize").getText();//页面显示记录数
			strFrom = head.element("from").getText();//起始记录数
			kssj = data.element("kssj").getText();//起始时间
			jssj = data.element("jssj").getText();//截止时间
			hpzl = data.element("hpzl").getText();//号牌种类
			cphid = data.element("hphm").getText();//号牌号码
			cplx = data.element("cplx").getText();//车牌类型
			gcxh = data.element("tpid").getText();//过车序号
			jcdid = data.element("jcdid").getText();//监测点ID
			cd = data.element("cd").getText();//车道
			cb = data.element("cb").getText();//车标
			sd = data.element("sd").getText();//速度
			hmdCphm = data.element("hmdCphm").getText();//红名单车牌号码
			business =businessType;//业务查询类型
			try {
				sort = head.element("sort").getText();//排序字段
				sortType = head.element("sortType").getText();//排序字段
			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("没有排序要求");
			}
		} catch (Exception e) {
			//"09:xml文件格式无法解析，请检查！"
			return xmlcreate.createErrorXml(config.getErrorCode09());
		}
		
		//判断给定时间，查询Oracle数据库还是ES库
		Date midDate = null;
		Date ksDate = null;
		Date jzDate = null;
		try {
			ksDate = sdf.parse(kssj);//开始时间
			jzDate = sdf.parse(jssj);//结束时间
			
			//Oracle库和ES库查询分界点,,,参数可配置
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			midDate = df.parse(InterUtil.getTime(Integer.parseInt(config.getBeforeDate())));
//			System.out.println("中间时间："+sdf.format(midDate));
		} catch (ParseException e) {
			//"03:时间格式不合法或者为空，请检查！"
			return  xmlcreate.createErrorXml(config.getErrorCode03());
		}
		
		//----分页参数处理-----
		int pagesize = 0;//每页显示的数据
		int from = 0;//从第几条开始取
		int allowCount = Integer.parseInt(config.getMaxCount());//最大允许返回记录总数
		int pageCount = Integer.parseInt(config.getPageCount());;//分页查询最大允许返回的记录数
		int maxOrder = Integer.parseInt(config.getMaxOrder());//最大允许排序总记录数
		
		if(strPage != null && !"".equals(strPage)){
			try {
				pagesize = Integer.parseInt(strPage);
				from = Integer.parseInt(strFrom);
				if(pagesize > 0 && pagesize > pageCount){//检查分页记录数
					//"05:分页查询记录数超过最大允许值！"
					return xmlcreate.createErrorXml(config.getErrorCode05());
				}else if(pagesize < 0 || from < 0){
					//"11:分页参数不合法！"
					return xmlcreate.createErrorXml(config.getErrorCode11());
				}
				//按照分页查询，记录数超过设置值时，不再返回，因为ES做深层的分页时，
				//由于查询机制问题，容易导致内存溢出，库崩溃
				if((pagesize+from)>maxOrder){
					//分页条数超过限定值
					return xmlcreate.createErrorXml(config.getErrorCode05());
				}
			} catch (Exception e) {
				//08:解析分页参数出现异常
				return  xmlcreate.createErrorXml(config.getErrorCode08());
			}
		}
	
		SearchInfo sqlInfo = new SearchInfo();//oracle库sql语句生成类
		Search search = new Search();//Oracle实现查询类
		ESsearcherFilter essearch = new ESsearcherFilter();
		SearchHits hits = null;
		FilterBuilder filter = null;
//		QueryBuilder query = null;
		CountDownLatch threadsSignal;
		List<Sbnew> listtx = new ArrayList<Sbnew>();//存放查询结果集
		int queryCount = 0;//满足查询条件总数
		if(ksDate.after(midDate) || ksDate.equals(midDate)){//如果开始时间大于当前时间减去n天前的时间，则只查询oracle库
			//Oracle调用
//			System.out.println("查询Oracle");
			try {
				//查询符合结果的总数，sqlInfo.getSB31SqlByCon为31一张表的生成形式,
				//sqlInfo.getSqlByCon为一张表的实现方式，修改时下面都需要修改
				if("0".equals(flag.trim())){
					queryCount = search.getTDCPGJCXCount(sqlInfo.getSqlByCon(kssj, jssj, hpzl, 
							cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "1"));
					return xmlcreate.createCountXml(queryCount, 0);//创建xml，返回结果
				}
				
				//如果不是查询总数，则分页查询数据
				if(pagesize > 0){
					listtx = search.TDCPGJCX(sqlInfo.getSqlByCon(kssj, jssj, hpzl, cphid, 
							cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0"), from, pagesize,sort,sortType);
				}else {//返回符合查询条件的所有数据
					queryCount = search.getTDCPGJCXCount(sqlInfo.getSqlByCon(kssj, jssj, hpzl, cphid, 
							cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "1"));
					if(queryCount > allowCount){//如果查询结果集超过给定数，则返回错误。
						//"07:查询结果集为"+queryCount+"条，超出允许条数，请缩小检索范围！
						return xmlcreate.createErrorXml(config.getErrorCode07());
					}else{
						listtx = search.TDCPGJCX(sqlInfo.getSqlByCon(kssj, jssj, hpzl, cphid, 
								cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0"), 0, queryCount,sort,sortType);	
					}
				}
			} catch (SQLException e) {
				//"04:查询数据库出现异常，请联系管理员！"
				return xmlcreate.createErrorXml(config.getErrorCode04());
			}
			return xmlcreate.createXml(hmdCphm, listtx, null);
		}else if(jzDate.before(midDate)){//如果截止时间小于当前时间减去N天前的时间，则只查询ES库
			//生成FilterBuilder filter
//			System.out.println("查询ES库");
//			filter = ESutil.getFilterByCon(kssj, jssj, hpzl, cphid, cplx, gcxh, jcdid, business);
			filter = ESutil.getFilterByCon(kssj, jssj, hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, 
					hmdCphm, business);//准备查询条件
//			query = ESutil.getQueryBuilderByCon(kssj, jssj, hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd,
//					hmdCphm, business);
			
			try {
				//查询符合结果的总数
				
				if("0".equals(flag.trim())){
					queryCount = essearch.getTdcpgjcxCount(filter, business);
					return xmlcreate.createCountXml(0, queryCount);
				}
				
				
				//如果不是查询总数，则分页查询数据
				if(pagesize > 0){
					if(!"".equals(sort.trim())&&!"".equals(sortType)){//如果有排序
			    		queryCount = essearch.getTdcpgjcxCount(filter, business);
			    		if(queryCount > maxOrder){
			    			//记录总数超过允许排序值
//							return xmlcreate.createErrorXml(config.getErrorCode14()+";排序数据量最大允许值为"+config.getMaxOrder());
			    			hits = essearch.tdcpgjcx(filter, from, pagesize, business,"","");
			    		}else{
			    			//排序，在指定范围内
			    			hits = essearch.tdcpgjcx(filter, from, pagesize, business,sort,sortType);
			    		}
			    	}else{
			    		hits = essearch.tdcpgjcx(filter, from, pagesize, business,sort,sortType);
			    	} 
					
				}else{//返回符合数据的全部记录
					queryCount = essearch.getTdcpgjcxCount(filter, business);
					if(queryCount > allowCount){//如果查询结果集超过给定数，则返回错误。
						//"07:查询结果集为"+queryCount+"条，超出允许条数，请缩小检索范围！"
						return xmlcreate.createErrorXml(config.getErrorCode07());
					}else{
						hits = essearch.tdcpgjcx(filter, 0, queryCount, business,sort,sortType);//通过query查询ES库
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				//"04:查询数据库出现异常，请联系管理员！"
				return xmlcreate.createErrorXml(config.getErrorCode04());
			}
			return  xmlcreate.createXml(hmdCphm, null, hits);
		}else {//查询两个库
//			System.out.println("查询Oracle、ES库");
			try {
				if("0".equals(flag.trim())){//查询两个数据库符合结果的总数
					String oraCountSql = sqlInfo.getSqlByCon(sdf.format(midDate), jssj, 
							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "1");
					// ES//查询结束时间为中间点时间
					filter = ESutil.getFilterByCon(kssj, sdf.format(midDate), 
							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business);
//					query = ESutil.getQueryBuilderByCon(kssj, sdf.format(midDate), 
//							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business);
					//查询oracle和ES库符合条件记录总数
					threadsSignal = new CountDownLatch(2);//创建两个线程组
					ESCountThreadByQuery escount = new ESCountThreadByQuery(threadsSignal, filter, business);//查询记录总数
					escount.start();
					
					OracleCountThread oraclecount = new OracleCountThread(threadsSignal, oraCountSql);//执行oracle线程
					oraclecount.start();
					threadsSignal.await();//等待Oracle和ES库查询完毕。（直到线程数为零）
					
					//获取结果总数
					int oraCou = oraclecount.count; 
					int esCou = escount.count;
					
					return xmlcreate.createCountXml(oraCou, esCou);
				}
				
				//如果不是查询总数，则分页查询数据
				if(pagesize > 0){
					/**
					 * 分页查询
					 * 	1.首先查询Oracle数据库符合条件的记录总数queryCount；
					 * 	2.根据给定的from值和pagesize值，如果from>=queryCount,则直接查询ES库，
					 *    如果queryCount>=from+pagesize,则只查询Oralce数据库，否则需要查询两个数据库；
					 * 	3.如果需要查询两个数据库，Oracle数据库查询范围为from至queryCount,ES库查询范围为0至pagesize-(queryCount-from)
					 */
					//Oracle符合条件记录数
					queryCount = search.getTDCPGJCXCount(sqlInfo.getSqlByCon(sdf.format(midDate), jssj, 
							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "1"));
					
					//ES符合条件记录数
					filter = ESutil.getFilterByCon(kssj, sdf.format(midDate), 
							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business);
					
//					query = ESutil.getQueryBuilderByCon(kssj, sdf.format(midDate), 
//							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business);
					
				    if(queryCount >= from + pagesize){//如果ORACLE库所查询到的数据总数已经达到所请求的数据量，则只查询ORACLE库
				    	listtx = search.TDCPGJCX(sqlInfo.getSqlByCon(sdf.format(midDate), jssj,
				    			 hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0"), from, pagesize,sort,sortType);
				    	return xmlcreate.createXml(hmdCphm, listtx,null);
				    }else if(from >= queryCount){//如果所请求的数据页数不在ORACLE库，则查询ES库
				    	
				    	if(!"".equals(sort.trim())&&!"".equals(sortType)){//如果有排序
				    		int escount = essearch.getTdcpgjcxCount(filter, business);//查询满足ES条件的记录数
				    		if(escount > maxOrder){//查询记录总数大于指定值，查询不做
				    			//记录总数超过允许排序值
//								return xmlcreate.createErrorXml(config.getErrorCode14()+";排序数据量最大允许值为"+config.getMaxOrder());
				    			hits = essearch.tdcpgjcx(filter, from - queryCount, pagesize, businessType,"","");
				    			return  xmlcreate.createXml(hmdCphm, null,hits);
				    		}else{
				    			hits = essearch.tdcpgjcx(filter, from - queryCount, pagesize, businessType,sort,sortType);
				    			return  xmlcreate.createXml(hmdCphm, null,hits);
				    		}
				    	} 
													
				    	hits = essearch.tdcpgjcx(filter, from - queryCount, pagesize, businessType,sort,sortType);
				    	return  xmlcreate.createXml(hmdCphm, null,hits);
					}else{
					
						//查询两个库
						threadsSignal = new CountDownLatch(2);//创建两个线程组
						ESThreadByQuery es = null;
						if(!"".equals(sort.trim())&&!"".equals(sortType)){//如果有排序
				    		Integer escount = essearch.getTdcpgjcxCount(filter, business);//符合条件的es记录总数
				    		if(escount > maxOrder){
				    			//记录总数超过允许排序值
//								return xmlcreate.createErrorXml(config.getErrorCode14()+";排序数据量最大允许值为"+config.getMaxOrder());
				    			es = new ESThreadByQuery(threadsSignal, filter, 0, 
										pagesize - (queryCount - from), business,"","");//执行Es线程
								es.start();
				    		}else{
				    			es = new ESThreadByQuery(threadsSignal, filter, 0, 
										pagesize - (queryCount - from), business,sort,sortType);//执行Es线程
								es.start();
				    		}
				    	}else{
				    		es = new ESThreadByQuery(threadsSignal, filter, 0, 
									pagesize - (queryCount - from), business,sort,sortType);//执行Es线程
							es.start();
				    	}
						
						String sql = sqlInfo.getSqlByCon(sdf.format(midDate), jssj,
								hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business,"0");
						OracleThread oracle = new OracleThread(threadsSignal, sql, from, queryCount - from,sort,sortType);//执行oracle线程
						oracle.start();
						threadsSignal.await();//等待Oracle和ES库查询完毕。
						
						return xmlcreate.createXml(hmdCphm, oracle.listtx, es.hits);
					}
				}else{
					/**
					 *返回所有符合条件的记录 
					 *1.按条件查询出两个库符合条件的记录数，如果记录数超过给定值，返回异常信息；
					 *2.在查询两个库记录综合和记录时，使用线程并发技术，同时查询两个数据库信息，提高响应时间；
					 */
					// 查询起始时间为中间时间点
					String oraCountSql = sqlInfo.getSqlByCon(sdf.format(midDate), jssj, 
							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "1");
					// ES//查询结束时间为中间点时间
					filter = ESutil.getFilterByCon(kssj, sdf.format(midDate), 
							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business);
//					query = ESutil.getQueryBuilderByCon(kssj, sdf.format(midDate), 
//							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business);
					//查询oracle和es库符合条件记录总数
					threadsSignal = new CountDownLatch(2);//创建两个线程组
					ESCountThreadByQuery escount = new ESCountThreadByQuery(threadsSignal, filter, business);//查询记录总数
					escount.start();
					
					OracleCountThread oraclecount = new OracleCountThread(threadsSignal, oraCountSql);//执行oracle线程
					oraclecount.start();
					threadsSignal.await();//等待Oracle和ES库查询完毕。
					
					//获取结果
					int oraCou = oraclecount.count; 
					int esCou  = escount.count ;
					queryCount = oraCou + esCou;
					
					if(queryCount > allowCount){//查询结果集超过给定数。
						//"07:查询结果集为"+queryCount+"条，超出允许条数，请缩小检索范围！"
						return  xmlcreate.createErrorXml(config.getErrorCode07());
					}else if(queryCount>maxOrder){
						return  xmlcreate.createErrorXml(config.getErrorCode14()+";排序数据量最大允许值为"+config.getMaxOrder());
					}else{
						String sql = sqlInfo.getSqlByCon(sdf.format(midDate), jssj,
								hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0");
						
						threadsSignal = new CountDownLatch(2);//创建两个线程组
						ESThreadByQuery es = new ESThreadByQuery(threadsSignal, filter, 0, esCou, business,sort,sortType);//执行Es线程
						es.start();
						
						OracleThread oracle = new OracleThread(threadsSignal, sql, 0, oraCou,sort,sortType);//执行oracle线程
						oracle.start();
						threadsSignal.await();//等待Oracle和ES库查询完毕。
						
						return xmlcreate.createXml(hmdCphm, oracle.listtx, es.hits);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				//"06:查询出现异常，请联系管理员！"
				return xmlcreate.createErrorXml(config.getErrorCode06());
			}
		}
	}
  
	
	/**
	 * 轨迹图片方法
	 * @param xml           XML请求报文
	 * @param 返回记录 
	 */
	@SuppressWarnings("unchecked")
	public String tpcx(String xml,String businessType) {
		//配置信息类
		Config config = Config.getInstance();
		
		Document document = null;
		XmlCreater xmlcreate = new XmlCreater();
		Element body = null;
		CountDownLatch threadsSignal;
		
		List<Element> listTpid = new ArrayList<Element>();//图片id集合
		List listPic = new ArrayList();//图片地址集合
		String tpid = "";
		try {
			document = (Document) DocumentHelper.parseText(xml);
			Element root = document.getRootElement();
			body = (Element) root.selectNodes("body").get(0);
			listTpid = body.selectNodes("tpid");
			
			//图片id为空
			if(listTpid == null || listTpid.size() == 0){
				//"10:给定图片id无法解析！"
				return xmlcreate.createErrorXml(config.getErrorCode10());
			}
		} catch (DocumentException e) {
			//"09解析XML文件出现异常
			return xmlcreate.createErrorXml(config.getErrorCode09());
		}
		
		int threadNum = 3;//每次启动线程数
		try {
			threadNum = Integer.parseInt(config.getNumThread().trim());
		} catch (Exception e) {
		}
		
		try {
			int len = listTpid.size();//图片数
			for (int j = 0;j < len;j+=threadNum) {
				if(len - j < threadNum){
					threadsSignal = new CountDownLatch(len - j);//创建en-beg个线程,当不足threadNum个时
				}else{
					threadsSignal = new CountDownLatch(threadNum);//创建threadNum个线程
				}
				for(int i = j;i < listTpid.size() && i < j + threadNum;i++){
					tpid = ((Element)listTpid.get(i)).getText();
					if(tpid == null || "".equals(tpid)){
						continue;
					}
					
					PicThread index = new PicThread(threadsSignal, tpid, businessType, listPic);
					index.start();
				}
				threadsSignal.await();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}//等待线程组执行完成
		
		return xmlcreate.createPicPath(listPic);
	}
}