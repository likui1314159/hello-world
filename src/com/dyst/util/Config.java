package com.dyst.util;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class Config {

	private static Config config = new Config();

	private Config() {
		init();// ��ʼ�������ļ���Ϣ
	}

	public static Config getInstance() {
		return config;
	}

	public static Config getConfig() {
		return config;
	}

	// ��ѯN��ǰ�����ݣ�Ĭ��Ϊ0
	public String beforeDate = "0";
	public String pageCount = "20";// ��ҳ��ѯʱÿҳ��������ؼ�¼��
	public String maxCount = "2000";// ��ѯȫ����¼������������صļ�¼��
	public String serverIp = "";// ��Ⱥ����IP,���IP�԰�Ƕ��ŷָ�
	public String logFolder = "/";
	public String maxConnection = "10";// �������������
	public String initConnection = "4";// ��ʼ������������
	public String timeOut = "10000";

	public String errorCode01 = "";// /�ӿڲ�ѯ�쳣�������
	public String errorCode02 = "";
	public String errorCode03 = "";
	public String errorCode04 = "";
	public String errorCode05 = "";
	public String errorCode06 = "";
	public String errorCode07 = "";
	public String errorCode08 = "";
	public String errorCode09 = "";
	public String errorCode10 = "";
	public String errorCode11 = "";
	public String errorCode12 = "";
	public String errorCode13 = "";
	public String errorCode14 = "";
	public String errorCode15 = "";

	// oracle ������Ϣ
	private String user;
	private String password;
	private String url;
	private String driver;
	private String dbMaxCon;
	private String dbInit;
	private String dbtimeOut;

	// ͼƬ����
	public String numThread = "1";// ͼƬÿ��ͬ����ѯ��¼��
	
	private String CacheUrl;
	private String StorageNum;
	private String StorageUrl;
	private String picURL = "";// ͼƬURL·��
	
	private String gcscpicUrl = "";//Υ��ͼƬ���·��
	private String wftpURL = "";//Υ��ͼƬ����url
	
	private String getJcdTime;//��ʱ���ؼ���
	private String getJjhomdTime;//��ʱ����һ��������

	private String qz1 = "";
	private String qz2 = "";
    private String maxOrder = "10000000";//�������������
    
    private String sysFlag = "";//
    private String picCall = "";
    
	private void init() {
		SAXReader reader = new SAXReader();
		InputStream in = Config.class.getResourceAsStream("/config.xml");
		Document document = null;
		try {
			document = (Document) reader.read(in);
			Element root = document.getRootElement();
			// ES��������Ϣ
			beforeDate = root.element("beforeDate").getText().trim();
			pageCount = root.element("pageCount").getText().trim();
			maxCount = root.element("maxCount").getText().trim();
			logFolder = root.element("logFolder").getText();// ��־�ļ����Ŀ¼
			serverIp = root.element("serverIp").getText();// ��Ⱥ������IP��ַ����
			timeOut = root.element("timeOut").getText();// ���ӳ�Ĭ�����ӳ�ʱʱ��
			initConnection = root.element("initConnection").getText();// �̳߳س�ʼ���߳�������
			maxConnection = root.element("maxConnection").getText();// �̳߳�����߳�������

			// �����붨��
			errorCode01 = root.element("errorCode01").getText();
			errorCode02 = root.element("errorCode02").getText();
			errorCode03 = root.element("errorCode03").getText();
			errorCode04 = root.element("errorCode04").getText();
			errorCode05 = root.element("errorCode05").getText();
			errorCode06 = root.element("errorCode06").getText();
			errorCode07 = root.element("errorCode07").getText();
			errorCode08 = root.element("errorCode08").getText();
			errorCode09 = root.element("errorCode09").getText();
			errorCode10 = root.element("errorCode10").getText();
			errorCode11 = root.element("errorCode11").getText();
			errorCode12 = root.element("errorCode12").getText();
			errorCode13 = root.element("errorCode13").getText();
			errorCode14 = root.element("errorCode14").getText();
			errorCode15 = root.element("errorCode15").getText();

			// ���ݿ�������Ϣ
			user = root.element("user").getText();
			password = root.element("password").getText();
			url = root.element("url").getText();
			driver = root.element("driver").getText();
			dbMaxCon = root.element("dbMaxCon").getText();
			dbInit = root.element("dbInit").getText();
			dbtimeOut = root.element("dbtimeOut").getText();

			// ͼƬ����������Ϣ
			CacheUrl = root.element("CacheUrl").getText();
			StorageNum = root.element("StorageNum").getText();
			StorageUrl = root.element("StorageUrl").getText();
			picURL = root.element("picURL").getText();
			
			numThread = root.element("threadNum").getText();// �̳߳ض��̲߳�����
			
			wftpURL = root.element("wftpURL").getText();
			gcscpicUrl = root.element("gcscpicUrl").getText();
			
			getJcdTime = root.element("getJcdTime").getText();// ��ʱ���ؼ���
			getJjhomdTime = root.element("getJjhomdTime").getText();//��ʱ����һ��������

			qz1 = root.element("qz1").getText();
			qz2 = root.element("qz2").getText();
			maxOrder = root.element("maxOrder").getText().trim();
			
			sysFlag = root.element("sysFlag").getText().trim();
			picCall = root.element("picCall").getText().trim();
			    
		} catch (DocumentException e) {
			StringUtil.writerTXT(logFolder, "���������ļ�·���Ƿ���ȷ��" + e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			StringUtil.writerTXT(logFolder, "XML�ļ���������" + e);
		}
	}

	public String getGcscpicUrl() {
		return gcscpicUrl;
	}

	public void setGcscpicUrl(String gcscpicUrl) {
		this.gcscpicUrl = gcscpicUrl;
	}

	public String getStorageNum() {
		return StorageNum;
	}

	public void setStorageNum(String storageNum) {
		StorageNum = storageNum;
	}

	public String getCacheUrl() {
		return CacheUrl;
	}

	public void setCacheUrl(String cacheUrl) {
		CacheUrl = cacheUrl;
	}

	public String getStorageUrl() {
		return StorageUrl;
	}

	public String getPicURL() {
		return picURL;
	}

	public void setPicURL(String picURL) {
		this.picURL = picURL;
	}

	public String getErrorCode01() {
		return errorCode01;
	}

	public String getErrorCode02() {
		return errorCode02;
	}

	public String getErrorCode03() {
		return errorCode03;
	}

	public String getErrorCode04() {
		return errorCode04;
	}

	public String getErrorCode05() {
		return errorCode05;
	}

	public String getErrorCode06() {
		return errorCode06;
	}

	public String getErrorCode07() {
		return errorCode07;
	}

	public String getErrorCode08() {
		return errorCode08;
	}

	public String getErrorCode09() {
		return errorCode09;
	}

	public String getErrorCode10() {
		return errorCode10;
	}

	public String getErrorCode11() {
		return errorCode11;
	}

	public String getErrorCode12() {
		return errorCode12;
	}

	public String getErrorCode13() {
		return errorCode13;
	}

	public void setErrorCode13(String errorCode13) {
		this.errorCode13 = errorCode13;
	}

	public String getErrorCode14() {
		return errorCode14;
	}

	public void setErrorCode14(String errorCode14) {
		this.errorCode14 = errorCode14;
	}

	public String getErrorCode15() {
		return errorCode15;
	}

	public void setErrorCode15(String errorCode15) {
		this.errorCode15 = errorCode15;
	}

	public String getQz1() {
		return qz1;
	}

	public void setQz1(String qz1) {
		this.qz1 = qz1;
	}

	public String getQz2() {
		return qz2;
	}

	public void setQz2(String qz2) {
		this.qz2 = qz2;
	}

	public String getDbtimeOut() {
		return dbtimeOut;
	}

	public String getDbMaxCon() {
		return dbMaxCon;
	}

	public void setDbMaxCon(String dbMaxCon) {
		this.dbMaxCon = dbMaxCon;
	}

	public String getDbInit() {
		return dbInit;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public String getUrl() {
		return url;
	}

	public String getDriver() {
		return driver;
	}

	public String getNumThread() {
		return numThread;
	}

	public String getTimeOut() {
		return timeOut;
	}

	public String getMaxConnection() {
		return maxConnection;
	}

	public String getInitConnection() {
		return initConnection;
	}

	public String getLogFolder() {
		return logFolder;
	}

	public String getBeforeDate() {
		return beforeDate;
	}

	public String getPageCount() {
		return pageCount;
	}

	public String getMaxCount() {
		return maxCount;
	}

	public String getServerIp() {
		return serverIp;
	}

	public String getGetJcdTime() {
		return getJcdTime;
	}

	public String getGetJjhomdTime() {
		return getJjhomdTime;
	}

	public String getWftpURL() {
		return wftpURL;
	}

	public String getMaxOrder() {
		return maxOrder;
	}

	public void setMaxOrder(String maxOrder) {
		this.maxOrder = maxOrder;
	}

	public String getSysFlag() {
		return sysFlag;
	}

	public void setSysFlag(String sysFlag) {
		this.sysFlag = sysFlag;
	}

	public String getPicCall() {
		return picCall;
	}

	public void setPicCall(String picCall) {
		this.picCall = picCall;
	}
}
