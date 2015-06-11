package com.dyst.util;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class Config {

	private static Config config = new Config();

	private Config() {
		init();// 初始化配置文件信息
	}

	public static Config getInstance() {
		return config;
	}

	public static Config getConfig() {
		return config;
	}

	// 查询N天前的数据，默认为0
	public String beforeDate = "0";
	public String pageCount = "20";// 分页查询时每页最大允许返回记录数
	public String maxCount = "2000";// 查询全部记录数，最大允许返回的记录数
	public String serverIp = "";// 集群服务IP,多个IP以半角逗号分隔
	public String logFolder = "/";
	public String maxConnection = "10";// 最大允许连接数
	public String initConnection = "4";// 初始化允许连接数
	public String timeOut = "10000";

	public String errorCode01 = "";// /接口查询异常错误代码
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

	// oracle 配置信息
	private String user;
	private String password;
	private String url;
	private String driver;
	private String dbMaxCon;
	private String dbInit;
	private String dbtimeOut;

	// 图片配置
	public String numThread = "1";// 图片每次同步查询记录数
	
	private String CacheUrl;
	private String StorageNum;
	private String StorageUrl;
	private String picURL = "";// 图片URL路径
	
	private String gcscpicUrl = "";//违法图片存放路径
	private String wftpURL = "";//违法图片访问url
	
	private String getJcdTime;//定时加载监测点
	private String getJjhomdTime;//定时加载一级红名单

	private String qz1 = "";
	private String qz2 = "";
    private String maxOrder = "10000000";//最大允许排序数
    
    private String sysFlag = "";//
    private String picCall = "";
    
	private void init() {
		SAXReader reader = new SAXReader();
		InputStream in = Config.class.getResourceAsStream("/config.xml");
		Document document = null;
		try {
			document = (Document) reader.read(in);
			Element root = document.getRootElement();
			// ES库配置信息
			beforeDate = root.element("beforeDate").getText().trim();
			pageCount = root.element("pageCount").getText().trim();
			maxCount = root.element("maxCount").getText().trim();
			logFolder = root.element("logFolder").getText();// 日志文件存放目录
			serverIp = root.element("serverIp").getText();// 集群服务器IP地址集合
			timeOut = root.element("timeOut").getText();// 连接池默认连接超时时间
			initConnection = root.element("initConnection").getText();// 线程池初始化线程连接数
			maxConnection = root.element("maxConnection").getText();// 线程池最大线程连接数

			// 错误码定义
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

			// 数据库配置信息
			user = root.element("user").getText();
			password = root.element("password").getText();
			url = root.element("url").getText();
			driver = root.element("driver").getText();
			dbMaxCon = root.element("dbMaxCon").getText();
			dbInit = root.element("dbInit").getText();
			dbtimeOut = root.element("dbtimeOut").getText();

			// 图片调用配置信息
			CacheUrl = root.element("CacheUrl").getText();
			StorageNum = root.element("StorageNum").getText();
			StorageUrl = root.element("StorageUrl").getText();
			picURL = root.element("picURL").getText();
			
			numThread = root.element("threadNum").getText();// 线程池多线程并发数
			
			wftpURL = root.element("wftpURL").getText();
			gcscpicUrl = root.element("gcscpicUrl").getText();
			
			getJcdTime = root.element("getJcdTime").getText();// 定时加载监测点
			getJjhomdTime = root.element("getJjhomdTime").getText();//定时加载一级红名单

			qz1 = root.element("qz1").getText();
			qz2 = root.element("qz2").getText();
			maxOrder = root.element("maxOrder").getText().trim();
			
			sysFlag = root.element("sysFlag").getText().trim();
			picCall = root.element("picCall").getText().trim();
			    
		} catch (DocumentException e) {
			StringUtil.writerTXT(logFolder, "请检查配置文件路径是否正确？" + e);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			StringUtil.writerTXT(logFolder, "XML文件解析出错！" + e);
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
