package com.dyst.webservice;

public interface IInAccessService {
	/**
	 * �����켣��ѯ 
	 * @param systemType   ϵͳ����<br>
	 * @param businessType ҵ������<br>
	 * @param sn           У����<br>
	 * @param data         �Ƿ񷵻ز�ѯ����<br>
	 * @param xml          ����XML����<br>
	 * @return XML�ļ��ַ���
	 */
	public String executes(String systemType, String businessType, String sn, String data, String xml);
}