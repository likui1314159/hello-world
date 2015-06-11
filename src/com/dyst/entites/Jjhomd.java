package com.dyst.entites;

import java.util.Date;

/**
 * @Entity Jjhomd  交警红名单表
 * @author chengaoke
 * @Date 2013-09-17
 */
@SuppressWarnings("serial")
public class Jjhomd implements java.io.Serializable {
	private Integer id;
	private String cphid;
	private String cplx;
	private String honmddj;
	private Date kssj;
	private Date jssj;
	private String zt;
	private String jlzt;


    // Constructors
    /** default constructor */
    public Jjhomd() {
    }

	/** minimal constructor */
    public Jjhomd(String cphid, String cplx, String honmddj, Date kssj, Date jssj, 
    		String zt, String jlzt) {
       this.cphid = cphid;
       this.cplx = cplx;
       this.honmddj = honmddj;
       this.kssj = kssj;
       this.jssj = jssj;
       this.zt = zt;
       this.jlzt = jlzt;
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public String getHonmddj() {
		return honmddj;
	}

	public void setHonmddj(String honmddj) {
		this.honmddj = honmddj;
	}

	public Date getKssj() {
		return kssj;
	}

	public void setKssj(Date kssj) {
		this.kssj = kssj;
	}

	public Date getJssj() {
		return jssj;
	}

	public void setJssj(Date jssj) {
		this.jssj = jssj;
	}

	public String getZt() {
		return zt;
	}

	public void setZt(String zt) {
		this.zt = zt;
	}

	public String getJlzt() {
		return jlzt;
	}

	public void setJlzt(String jlzt) {
		this.jlzt = jlzt;
	}
}