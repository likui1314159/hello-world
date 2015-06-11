package com.dyst.entites;

import java.io.Serializable;
import java.sql.Timestamp;

public class Txsjcx  implements Serializable{

	private static final long serialVersionUID = 1L;
	//由于在使用dbutils中使用Date类型无法取到时间，故采用SQL的Timestamp类
	private Timestamp sbsj; //通过时间
	private String jcdid; //监测点id ,数据库中的是archar2
	private String  cdid;//车道id;在sb表中
	private String cphid;//号牌号码
	private String cplx;//车牌类型
	private String tpid1;//图片id1
	public String getTpid2() {
		return tpid2;
	}
	public void setTpid2(String tpid2) {
		this.tpid2 = tpid2;
	}
	private String tpid2;//图片id1
//	private String tpid2;
//	private String txtp;
//	private String  txtp2;	
//	private String hpzl;//图片张数
//	private String cllx;//车辆类型
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
