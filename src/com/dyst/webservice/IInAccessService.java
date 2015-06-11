package com.dyst.webservice;

public interface IInAccessService {
	/**
	 * 车辆轨迹查询 
	 * @param systemType   系统类型<br>
	 * @param businessType 业务类型<br>
	 * @param sn           校验码<br>
	 * @param data         是否返回查询数据<br>
	 * @param xml          请求XML报文<br>
	 * @return XML文件字符串
	 */
	public String executes(String systemType, String businessType, String sn, String data, String xml);
}