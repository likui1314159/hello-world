package com.dyst.entites;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Sbnew entity. @author MyEclipse Persistence Tools
 */

@SuppressWarnings("serial")
public class Sbfx implements java.io.Serializable ,Comparable<Sbfx>{

	// Fields

	private String tpid1;
	private String cphm1;
	private String jcdid;
	private Date tgsj;
	private String cplx1;
	private String tpid2;
	private String cphm2;
	private Timestamp scsj;

	// Constructors

	/** default constructor */
	public Sbfx() {
	}

	/** minimal constructor */
	public Sbfx(String jcdid, Long fqh, String cphm2) {
		this.jcdid = jcdid;
		this.cphm2 = cphm2;
	}


	// Property accessors

	public String getTpid1() {
		return this.tpid1;
	}

	public void setTpid1(String tpid1) {
		this.tpid1 = tpid1;
	}

	public String getCphm1() {
		return this.cphm1;
	}

	public void setCphm1(String cphm1) {
		this.cphm1 = cphm1;
	}

	public String getJcdid() {
		return this.jcdid;
	}

	public void setJcdid(String jcdid) {
		this.jcdid = jcdid;
	}


	public Date getTgsj() {
		return this.tgsj;
	}

	public void setTgsj(Date tgsj) {
		this.tgsj = tgsj;
	}

	public String getCplx1() {
		return this.cplx1;
	}

	public void setCplx1(String cplx1) {
		this.cplx1 = cplx1;
	}


	public String getTpid2() {
		return tpid2;
	}

	public void setTpid2(String tpid2) {
		this.tpid2 = tpid2;
	}

	public String getCphm2() {
		return cphm2;
	}

	public void setCphm2(String cphm2) {
		this.cphm2 = cphm2;
	}

	public Timestamp getScsj() {
		return scsj;
	}

	public void setScsj(Timestamp scsj) {
		this.scsj = scsj;
	}

	public int compareTo(Sbfx o) {
		return this.cphm1.compareTo(o.getCphm1());
	}

}