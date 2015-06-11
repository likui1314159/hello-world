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
	 * �켣��ѯ����������ҵ����������
	 * @param xml           XML������
	 * @param businessType  ��ѯҵ������
	 * @param flag          1:���ؼ�¼��0:���ؼ�¼������  ����ʶ���¼���Ǽ�¼����
	 * @param ���ؼ�¼��������ʶ���¼
	 */
	public String gjcx(String xml, String businessType, String flag){
		Config config = Config.getInstance();//������Ϣ��
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Document document = null;
		XmlCreater xmlcreate = new XmlCreater();
		
		//��������
		String cphid = null;//���ƺ���
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
		String strPage = null;//ҳ����ʾ��¼��
		String strFrom = null;//��ʼ��¼��
		String business = null;//ҵ���ѯ����
		String sort = "";//�����ֶ�
		String sortType = "" ;//�����ֶ�����˳�򣬽��������
		
		try {
			document = (Document) DocumentHelper.parseText(xml);
			Element root = document.getRootElement();//��ȡ���ڵ�
			Element head = (Element) root.selectNodes("head").get(0);
			Element body = (Element) root.selectNodes("body").get(0);
			Element data = (Element) body.selectNodes("data").get(0);
			
			strPage = head.element("pagesize").getText();//ҳ����ʾ��¼��
			strFrom = head.element("from").getText();//��ʼ��¼��
			kssj = data.element("kssj").getText();//��ʼʱ��
			jssj = data.element("jssj").getText();//��ֹʱ��
			hpzl = data.element("hpzl").getText();//��������
			cphid = data.element("hphm").getText();//���ƺ���
			cplx = data.element("cplx").getText();//��������
			gcxh = data.element("tpid").getText();//�������
			jcdid = data.element("jcdid").getText();//����ID
			cd = data.element("cd").getText();//����
			cb = data.element("cb").getText();//����
			sd = data.element("sd").getText();//�ٶ�
			hmdCphm = data.element("hmdCphm").getText();//���������ƺ���
			business =businessType;//ҵ���ѯ����
			try {
				sort = head.element("sort").getText();//�����ֶ�
				sortType = head.element("sortType").getText();//�����ֶ�
			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("û������Ҫ��");
			}
		} catch (Exception e) {
			//"09:xml�ļ���ʽ�޷����������飡"
			return xmlcreate.createErrorXml(config.getErrorCode09());
		}
		
		//�жϸ���ʱ�䣬��ѯOracle���ݿ⻹��ES��
		Date midDate = null;
		Date ksDate = null;
		Date jzDate = null;
		try {
			ksDate = sdf.parse(kssj);//��ʼʱ��
			jzDate = sdf.parse(jssj);//����ʱ��
			
			//Oracle���ES���ѯ�ֽ��,,,����������
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			midDate = df.parse(InterUtil.getTime(Integer.parseInt(config.getBeforeDate())));
//			System.out.println("�м�ʱ�䣺"+sdf.format(midDate));
		} catch (ParseException e) {
			//"03:ʱ���ʽ���Ϸ�����Ϊ�գ����飡"
			return  xmlcreate.createErrorXml(config.getErrorCode03());
		}
		
		//----��ҳ��������-----
		int pagesize = 0;//ÿҳ��ʾ������
		int from = 0;//�ӵڼ�����ʼȡ
		int allowCount = Integer.parseInt(config.getMaxCount());//��������ؼ�¼����
		int pageCount = Integer.parseInt(config.getPageCount());;//��ҳ��ѯ��������صļ�¼��
		int maxOrder = Integer.parseInt(config.getMaxOrder());//������������ܼ�¼��
		
		if(strPage != null && !"".equals(strPage)){
			try {
				pagesize = Integer.parseInt(strPage);
				from = Integer.parseInt(strFrom);
				if(pagesize > 0 && pagesize > pageCount){//����ҳ��¼��
					//"05:��ҳ��ѯ��¼�������������ֵ��"
					return xmlcreate.createErrorXml(config.getErrorCode05());
				}else if(pagesize < 0 || from < 0){
					//"11:��ҳ�������Ϸ���"
					return xmlcreate.createErrorXml(config.getErrorCode11());
				}
				//���շ�ҳ��ѯ����¼����������ֵʱ�����ٷ��أ���ΪES�����ķ�ҳʱ��
				//���ڲ�ѯ�������⣬���׵����ڴ�����������
				if((pagesize+from)>maxOrder){
					//��ҳ���������޶�ֵ
					return xmlcreate.createErrorXml(config.getErrorCode05());
				}
			} catch (Exception e) {
				//08:������ҳ���������쳣
				return  xmlcreate.createErrorXml(config.getErrorCode08());
			}
		}
	
		SearchInfo sqlInfo = new SearchInfo();//oracle��sql���������
		Search search = new Search();//Oracleʵ�ֲ�ѯ��
		ESsearcherFilter essearch = new ESsearcherFilter();
		SearchHits hits = null;
		FilterBuilder filter = null;
//		QueryBuilder query = null;
		CountDownLatch threadsSignal;
		List<Sbnew> listtx = new ArrayList<Sbnew>();//��Ų�ѯ�����
		int queryCount = 0;//�����ѯ��������
		if(ksDate.after(midDate) || ksDate.equals(midDate)){//�����ʼʱ����ڵ�ǰʱ���ȥn��ǰ��ʱ�䣬��ֻ��ѯoracle��
			//Oracle����
//			System.out.println("��ѯOracle");
			try {
				//��ѯ���Ͻ����������sqlInfo.getSB31SqlByConΪ31һ�ű��������ʽ,
				//sqlInfo.getSqlByConΪһ�ű��ʵ�ַ�ʽ���޸�ʱ���涼��Ҫ�޸�
				if("0".equals(flag.trim())){
					queryCount = search.getTDCPGJCXCount(sqlInfo.getSqlByCon(kssj, jssj, hpzl, 
							cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "1"));
					return xmlcreate.createCountXml(queryCount, 0);//����xml�����ؽ��
				}
				
				//������ǲ�ѯ���������ҳ��ѯ����
				if(pagesize > 0){
					listtx = search.TDCPGJCX(sqlInfo.getSqlByCon(kssj, jssj, hpzl, cphid, 
							cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0"), from, pagesize,sort,sortType);
				}else {//���ط��ϲ�ѯ��������������
					queryCount = search.getTDCPGJCXCount(sqlInfo.getSqlByCon(kssj, jssj, hpzl, cphid, 
							cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "1"));
					if(queryCount > allowCount){//�����ѯ������������������򷵻ش���
						//"07:��ѯ�����Ϊ"+queryCount+"����������������������С������Χ��
						return xmlcreate.createErrorXml(config.getErrorCode07());
					}else{
						listtx = search.TDCPGJCX(sqlInfo.getSqlByCon(kssj, jssj, hpzl, cphid, 
								cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0"), 0, queryCount,sort,sortType);	
					}
				}
			} catch (SQLException e) {
				//"04:��ѯ���ݿ�����쳣������ϵ����Ա��"
				return xmlcreate.createErrorXml(config.getErrorCode04());
			}
			return xmlcreate.createXml(hmdCphm, listtx, null);
		}else if(jzDate.before(midDate)){//�����ֹʱ��С�ڵ�ǰʱ���ȥN��ǰ��ʱ�䣬��ֻ��ѯES��
			//����FilterBuilder filter
//			System.out.println("��ѯES��");
//			filter = ESutil.getFilterByCon(kssj, jssj, hpzl, cphid, cplx, gcxh, jcdid, business);
			filter = ESutil.getFilterByCon(kssj, jssj, hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, 
					hmdCphm, business);//׼����ѯ����
//			query = ESutil.getQueryBuilderByCon(kssj, jssj, hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd,
//					hmdCphm, business);
			
			try {
				//��ѯ���Ͻ��������
				
				if("0".equals(flag.trim())){
					queryCount = essearch.getTdcpgjcxCount(filter, business);
					return xmlcreate.createCountXml(0, queryCount);
				}
				
				
				//������ǲ�ѯ���������ҳ��ѯ����
				if(pagesize > 0){
					if(!"".equals(sort.trim())&&!"".equals(sortType)){//���������
			    		queryCount = essearch.getTdcpgjcxCount(filter, business);
			    		if(queryCount > maxOrder){
			    			//��¼����������������ֵ
//							return xmlcreate.createErrorXml(config.getErrorCode14()+";�����������������ֵΪ"+config.getMaxOrder());
			    			hits = essearch.tdcpgjcx(filter, from, pagesize, business,"","");
			    		}else{
			    			//������ָ����Χ��
			    			hits = essearch.tdcpgjcx(filter, from, pagesize, business,sort,sortType);
			    		}
			    	}else{
			    		hits = essearch.tdcpgjcx(filter, from, pagesize, business,sort,sortType);
			    	} 
					
				}else{//���ط������ݵ�ȫ����¼
					queryCount = essearch.getTdcpgjcxCount(filter, business);
					if(queryCount > allowCount){//�����ѯ������������������򷵻ش���
						//"07:��ѯ�����Ϊ"+queryCount+"����������������������С������Χ��"
						return xmlcreate.createErrorXml(config.getErrorCode07());
					}else{
						hits = essearch.tdcpgjcx(filter, 0, queryCount, business,sort,sortType);//ͨ��query��ѯES��
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				//"04:��ѯ���ݿ�����쳣������ϵ����Ա��"
				return xmlcreate.createErrorXml(config.getErrorCode04());
			}
			return  xmlcreate.createXml(hmdCphm, null, hits);
		}else {//��ѯ������
//			System.out.println("��ѯOracle��ES��");
			try {
				if("0".equals(flag.trim())){//��ѯ�������ݿ���Ͻ��������
					String oraCountSql = sqlInfo.getSqlByCon(sdf.format(midDate), jssj, 
							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "1");
					// ES//��ѯ����ʱ��Ϊ�м��ʱ��
					filter = ESutil.getFilterByCon(kssj, sdf.format(midDate), 
							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business);
//					query = ESutil.getQueryBuilderByCon(kssj, sdf.format(midDate), 
//							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business);
					//��ѯoracle��ES�����������¼����
					threadsSignal = new CountDownLatch(2);//���������߳���
					ESCountThreadByQuery escount = new ESCountThreadByQuery(threadsSignal, filter, business);//��ѯ��¼����
					escount.start();
					
					OracleCountThread oraclecount = new OracleCountThread(threadsSignal, oraCountSql);//ִ��oracle�߳�
					oraclecount.start();
					threadsSignal.await();//�ȴ�Oracle��ES���ѯ��ϡ���ֱ���߳���Ϊ�㣩
					
					//��ȡ�������
					int oraCou = oraclecount.count; 
					int esCou = escount.count;
					
					return xmlcreate.createCountXml(oraCou, esCou);
				}
				
				//������ǲ�ѯ���������ҳ��ѯ����
				if(pagesize > 0){
					/**
					 * ��ҳ��ѯ
					 * 	1.���Ȳ�ѯOracle���ݿ���������ļ�¼����queryCount��
					 * 	2.���ݸ�����fromֵ��pagesizeֵ�����from>=queryCount,��ֱ�Ӳ�ѯES�⣬
					 *    ���queryCount>=from+pagesize,��ֻ��ѯOralce���ݿ⣬������Ҫ��ѯ�������ݿ⣻
					 * 	3.�����Ҫ��ѯ�������ݿ⣬Oracle���ݿ��ѯ��ΧΪfrom��queryCount,ES���ѯ��ΧΪ0��pagesize-(queryCount-from)
					 */
					//Oracle����������¼��
					queryCount = search.getTDCPGJCXCount(sqlInfo.getSqlByCon(sdf.format(midDate), jssj, 
							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "1"));
					
					//ES����������¼��
					filter = ESutil.getFilterByCon(kssj, sdf.format(midDate), 
							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business);
					
//					query = ESutil.getQueryBuilderByCon(kssj, sdf.format(midDate), 
//							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business);
					
				    if(queryCount >= from + pagesize){//���ORACLE������ѯ�������������Ѿ��ﵽ�����������������ֻ��ѯORACLE��
				    	listtx = search.TDCPGJCX(sqlInfo.getSqlByCon(sdf.format(midDate), jssj,
				    			 hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0"), from, pagesize,sort,sortType);
				    	return xmlcreate.createXml(hmdCphm, listtx,null);
				    }else if(from >= queryCount){//��������������ҳ������ORACLE�⣬���ѯES��
				    	
				    	if(!"".equals(sort.trim())&&!"".equals(sortType)){//���������
				    		int escount = essearch.getTdcpgjcxCount(filter, business);//��ѯ����ES�����ļ�¼��
				    		if(escount > maxOrder){//��ѯ��¼��������ָ��ֵ����ѯ����
				    			//��¼����������������ֵ
//								return xmlcreate.createErrorXml(config.getErrorCode14()+";�����������������ֵΪ"+config.getMaxOrder());
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
					
						//��ѯ������
						threadsSignal = new CountDownLatch(2);//���������߳���
						ESThreadByQuery es = null;
						if(!"".equals(sort.trim())&&!"".equals(sortType)){//���������
				    		Integer escount = essearch.getTdcpgjcxCount(filter, business);//����������es��¼����
				    		if(escount > maxOrder){
				    			//��¼����������������ֵ
//								return xmlcreate.createErrorXml(config.getErrorCode14()+";�����������������ֵΪ"+config.getMaxOrder());
				    			es = new ESThreadByQuery(threadsSignal, filter, 0, 
										pagesize - (queryCount - from), business,"","");//ִ��Es�߳�
								es.start();
				    		}else{
				    			es = new ESThreadByQuery(threadsSignal, filter, 0, 
										pagesize - (queryCount - from), business,sort,sortType);//ִ��Es�߳�
								es.start();
				    		}
				    	}else{
				    		es = new ESThreadByQuery(threadsSignal, filter, 0, 
									pagesize - (queryCount - from), business,sort,sortType);//ִ��Es�߳�
							es.start();
				    	}
						
						String sql = sqlInfo.getSqlByCon(sdf.format(midDate), jssj,
								hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business,"0");
						OracleThread oracle = new OracleThread(threadsSignal, sql, from, queryCount - from,sort,sortType);//ִ��oracle�߳�
						oracle.start();
						threadsSignal.await();//�ȴ�Oracle��ES���ѯ��ϡ�
						
						return xmlcreate.createXml(hmdCphm, oracle.listtx, es.hits);
					}
				}else{
					/**
					 *�������з��������ļ�¼ 
					 *1.��������ѯ����������������ļ�¼���������¼����������ֵ�������쳣��Ϣ��
					 *2.�ڲ�ѯ�������¼�ۺϺͼ�¼ʱ��ʹ���̲߳���������ͬʱ��ѯ�������ݿ���Ϣ�������Ӧʱ�䣻
					 */
					// ��ѯ��ʼʱ��Ϊ�м�ʱ���
					String oraCountSql = sqlInfo.getSqlByCon(sdf.format(midDate), jssj, 
							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "1");
					// ES//��ѯ����ʱ��Ϊ�м��ʱ��
					filter = ESutil.getFilterByCon(kssj, sdf.format(midDate), 
							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business);
//					query = ESutil.getQueryBuilderByCon(kssj, sdf.format(midDate), 
//							hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business);
					//��ѯoracle��es�����������¼����
					threadsSignal = new CountDownLatch(2);//���������߳���
					ESCountThreadByQuery escount = new ESCountThreadByQuery(threadsSignal, filter, business);//��ѯ��¼����
					escount.start();
					
					OracleCountThread oraclecount = new OracleCountThread(threadsSignal, oraCountSql);//ִ��oracle�߳�
					oraclecount.start();
					threadsSignal.await();//�ȴ�Oracle��ES���ѯ��ϡ�
					
					//��ȡ���
					int oraCou = oraclecount.count; 
					int esCou  = escount.count ;
					queryCount = oraCou + esCou;
					
					if(queryCount > allowCount){//��ѯ�����������������
						//"07:��ѯ�����Ϊ"+queryCount+"����������������������С������Χ��"
						return  xmlcreate.createErrorXml(config.getErrorCode07());
					}else if(queryCount>maxOrder){
						return  xmlcreate.createErrorXml(config.getErrorCode14()+";�����������������ֵΪ"+config.getMaxOrder());
					}else{
						String sql = sqlInfo.getSqlByCon(sdf.format(midDate), jssj,
								hpzl, cphid, cplx, gcxh, jcdid, cd, cb, sd, hmdCphm, business, "0");
						
						threadsSignal = new CountDownLatch(2);//���������߳���
						ESThreadByQuery es = new ESThreadByQuery(threadsSignal, filter, 0, esCou, business,sort,sortType);//ִ��Es�߳�
						es.start();
						
						OracleThread oracle = new OracleThread(threadsSignal, sql, 0, oraCou,sort,sortType);//ִ��oracle�߳�
						oracle.start();
						threadsSignal.await();//�ȴ�Oracle��ES���ѯ��ϡ�
						
						return xmlcreate.createXml(hmdCphm, oracle.listtx, es.hits);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				//"06:��ѯ�����쳣������ϵ����Ա��"
				return xmlcreate.createErrorXml(config.getErrorCode06());
			}
		}
	}
  
	
	/**
	 * �켣ͼƬ����
	 * @param xml           XML������
	 * @param ���ؼ�¼ 
	 */
	@SuppressWarnings("unchecked")
	public String tpcx(String xml,String businessType) {
		//������Ϣ��
		Config config = Config.getInstance();
		
		Document document = null;
		XmlCreater xmlcreate = new XmlCreater();
		Element body = null;
		CountDownLatch threadsSignal;
		
		List<Element> listTpid = new ArrayList<Element>();//ͼƬid����
		List listPic = new ArrayList();//ͼƬ��ַ����
		String tpid = "";
		try {
			document = (Document) DocumentHelper.parseText(xml);
			Element root = document.getRootElement();
			body = (Element) root.selectNodes("body").get(0);
			listTpid = body.selectNodes("tpid");
			
			//ͼƬidΪ��
			if(listTpid == null || listTpid.size() == 0){
				//"10:����ͼƬid�޷�������"
				return xmlcreate.createErrorXml(config.getErrorCode10());
			}
		} catch (DocumentException e) {
			//"09����XML�ļ������쳣
			return xmlcreate.createErrorXml(config.getErrorCode09());
		}
		
		int threadNum = 3;//ÿ�������߳���
		try {
			threadNum = Integer.parseInt(config.getNumThread().trim());
		} catch (Exception e) {
		}
		
		try {
			int len = listTpid.size();//ͼƬ��
			for (int j = 0;j < len;j+=threadNum) {
				if(len - j < threadNum){
					threadsSignal = new CountDownLatch(len - j);//����en-beg���߳�,������threadNum��ʱ
				}else{
					threadsSignal = new CountDownLatch(threadNum);//����threadNum���߳�
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
		}//�ȴ��߳���ִ�����
		
		return xmlcreate.createPicPath(listPic);
	}
}