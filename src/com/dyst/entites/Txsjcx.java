package com.dyst.entites;

import java.io.Serializable;
import java.sql.Timestamp;

public class Txsjcx  implements Serializable{

	private static final long serialVersionUID = 1L;
	//������ʹ��dbutils��ʹ��Date�����޷�ȡ��ʱ�䣬�ʲ���SQL��Timestamp��
	private Timestamp sbsj; //ͨ��ʱ��
	private String jcdid; //����id ,���ݿ��е���archar2
	private String  cdid;//����id;��sb����
	private String cphid;//���ƺ���
	private String cplx;//��������
	private String tpid1;//ͼƬid1
	public String getTpid2() {
		return tpid2;
	}
	public void setTpid2(String tpid2) {
		this.tpid2 = tpid2;
	}
	private String tpid2;//ͼƬid1
//	private String tpid2;
//	private String txtp;
//	private String  txtp2;	
//	private String hpzl;//ͼƬ����
//	private String cllx;//��������
	public Timestamp getSbsj() {
		return sbsj;
	}
	public void setSbsj(Timestamp sbsj) {
		this.sbsj = sbsj;
	}
	public String getJcdid() {
		return jcdid;
	}
	public void setJcdid(String jcdid) {
		this.jcdid = jcdid;
	}
	public String getCdid() {
		return cdid;
	}
	public void setCdid(String cdid) {
		this.cdid = cdid;
	}
	public String getCphid() {
		return cphid;
	}
	public void setCphid(String cphid) {
		this.cphid = cphid;
	}
	public String getCplx() {
		return cplx;
	}
	public void setCplx(String cplx) {
		this.cplx = cplx;
	}
	public String getTpid1() {
		return tpid1;
	}
	public void setTpid1(String tpid1) {
		this.tpid1 = tpid1;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
