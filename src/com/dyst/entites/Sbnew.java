package com.dyst.entites;

import java.sql.Timestamp;

/**
 * Sbnew entity. @author MyEclipse Persistence Tools
 */

@SuppressWarnings("serial")
public class Sbnew implements java.io.Serializable {

	// Fields

	private String tpid1;
	private String cphm1;
	private String jcdid;
	private String cdid;
	private Timestamp tgsj;
	private String cplx1;
	private String cb;
	private String cllx;
	private String tpid2;
	private String spurl;
	private String bl;
	private Double fbcd;
	private String byzd1;
	private String byzd2;
	private Long fqh;
	private String byzd3;
	private String cplx2;
	private String hpsfwc;
	private String qhsfyz;
	private String bcbz;
	private String cphm2;
	private String hpzl;
	private Timestamp scsj;
	private String jcklx;
	private String csys;
	private String xxkbm;
	private String wflx;
	private Double sd;
	private String qdid;
	private String xsfx;
	private Integer tpzs;
	private String tpid3;
	private String tpid4;
	private String tpid5;
	private String qpsfwc;
	private Integer zxd;

	// Constructors

	/** default constructor */
	public Sbnew() {
	}

	/** minimal constructor */
	public Sbnew(String jcdid, Long fqh, String cphm2) {
		this.jcdid = jcdid;
		this.fqh = fqh;
		this.cphm2 = cphm2;
	}

	/** full constructor */
	public Sbnew(String cphm1, String jcdid, String cdid, Timestamp tgsj,
			String cplx1, String cb, String cllx, String tpid2, String spurl,
			String bl, Double fbcd, String byzd1, String byzd2, Long fqh,
			String byzd3, String cplx2, String hpsfwc, String qhsfyz,
			String bcbz, String cphm2, String hpzl, Timestamp scsj, String jcklx,
			String csys, String xxkbm, String wflx, Double sd, String qdid,
			String xsfx, Integer tpzs, String tpid3, String tpid4,
			String tpid5, String qpsfwc, Integer zxd) {
		this.cphm1 = cphm1;
		this.jcdid = jcdid;
		this.cdid = cdid;
		this.tgsj = tgsj;
		this.cplx1 = cplx1;
		this.cb = cb;
		this.cllx = cllx;
		this.tpid2 = tpid2;
		this.spurl = spurl;
		this.bl = bl;
		this.fbcd = fbcd;
		this.byzd1 = byzd1;
		this.byzd2 = byzd2;
		this.fqh = fqh;
		this.byzd3 = byzd3;
		this.cplx2 = cplx2;
		this.hpsfwc = hpsfwc;
		this.qhsfyz = qhsfyz;
		this.bcbz = bcbz;
		this.cphm2 = cphm2;
		this.hpzl = hpzl;
		this.scsj = scsj;
		this.jcklx = jcklx;
		this.csys = csys;
		this.xxkbm = xxkbm;
		this.wflx = wflx;
		this.sd = sd;
		this.qdid = qdid;
		this.xsfx = xsfx;
		this.tpzs = tpzs;
		this.tpid3 = tpid3;
		this.tpid4 = tpid4;
		this.tpid5 = tpid5;
		this.qpsfwc = qpsfwc;
		this.zxd = zxd;
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

	public String getCdid() {
		return this.cdid;
	}

	public void setCdid(String cdid) {
		this.cdid = cdid;
	}

	public Timestamp getTgsj() {
		return this.tgsj;
	}

	public void setTgsj(Timestamp tgsj) {
		this.tgsj = tgsj;
	}

	public String getCplx1() {
		return this.cplx1;
	}

	public void setCplx1(String cplx1) {
		this.cplx1 = cplx1;
	}

	public String getCb() {
		return this.cb;
	}

	public void setCb(String cb) {
		this.cb = cb;
	}

	public String getCllx() {
		return this.cllx;
	}

	public void setCllx(String cllx) {
		this.cllx = cllx;
	}

	public String getTpid2() {
		return this.tpid2;
	}

	public void setTpid2(String tpid2) {
		this.tpid2 = tpid2;
	}

	public String getSpurl() {
		return this.spurl;
	}

	public void setSpurl(String spurl) {
		this.spurl = spurl;
	}

	public String getBl() {
		return this.bl;
	}

	public void setBl(String bl) {
		this.bl = bl;
	}

	public Double getFbcd() {
		return this.fbcd;
	}

	public void setFbcd(Double fbcd) {
		this.fbcd = fbcd;
	}

	public String getByzd1() {
		return this.byzd1;
	}

	public void setByzd1(String byzd1) {
		this.byzd1 = byzd1;
	}

	public String getByzd2() {
		return this.byzd2;
	}

	public void setByzd2(String byzd2) {
		this.byzd2 = byzd2;
	}

	public Long getFqh() {
		return this.fqh;
	}

	public void setFqh(Long fqh) {
		this.fqh = fqh;
	}

	public String getByzd3() {
		return this.byzd3;
	}

	public void setByzd3(String byzd3) {
		this.byzd3 = byzd3;
	}

	public String getCplx2() {
		return this.cplx2;
	}

	public void setCplx2(String cplx2) {
		this.cplx2 = cplx2;
	}

	public String getHpsfwc() {
		return this.hpsfwc;
	}

	public void setHpsfwc(String hpsfwc) {
		this.hpsfwc = hpsfwc;
	}

	public String getQhsfyz() {
		return this.qhsfyz;
	}

	public void setQhsfyz(String qhsfyz) {
		this.qhsfyz = qhsfyz;
	}

	public String getBcbz() {
		return this.bcbz;
	}

	public void setBcbz(String bcbz) {
		this.bcbz = bcbz;
	}

	public String getCphm2() {
		return this.cphm2;
	}

	public void setCphm2(String cphm2) {
		this.cphm2 = cphm2;
	}

	public String getHpzl() {
		return this.hpzl;
	}

	public void setHpzl(String hpzl) {
		this.hpzl = hpzl;
	}

	public Timestamp getScsj() {
		return this.scsj;
	}

	public void setScsj(Timestamp scsj) {
		this.scsj = scsj;
	}

	public String getJcklx() {
		return this.jcklx;
	}

	public void setJcklx(String jcklx) {
		this.jcklx = jcklx;
	}

	public String getCsys() {
		return this.csys;
	}

	public void setCsys(String csys) {
		this.csys = csys;
	}

	public String getXxkbm() {
		return this.xxkbm;
	}

	public void setXxkbm(String xxkbm) {
		this.xxkbm = xxkbm;
	}

	public String getWflx() {
		return this.wflx;
	}

	public void setWflx(String wflx) {
		this.wflx = wflx;
	}

	public Double getSd() {
		return this.sd;
	}

	public void setSd(Double sd) {
		this.sd = sd;
	}

	public String getQdid() {
		return this.qdid;
	}

	public void setQdid(String qdid) {
		this.qdid = qdid;
	}

	public String getXsfx() {
		return this.xsfx;
	}

	public void setXsfx(String xsfx) {
		this.xsfx = xsfx;
	}

	public Integer getTpzs() {
		return this.tpzs;
	}

	public void setTpzs(Integer tpzs) {
		this.tpzs = tpzs;
	}

	public String getTpid3() {
		return this.tpid3;
	}

	public void setTpid3(String tpid3) {
		this.tpid3 = tpid3;
	}

	public String getTpid4() {
		return this.tpid4;
	}

	public void setTpid4(String tpid4) {
		this.tpid4 = tpid4;
	}

	public String getTpid5() {
		return this.tpid5;
	}

	public void setTpid5(String tpid5) {
		this.tpid5 = tpid5;
	}

	public String getQpsfwc() {
		return this.qpsfwc;
	}

	public void setQpsfwc(String qpsfwc) {
		this.qpsfwc = qpsfwc;
	}

	public Integer getZxd() {
		return this.zxd;
	}

	public void setZxd(Integer zxd) {
		this.zxd = zxd;
	}
}