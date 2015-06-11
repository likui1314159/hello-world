package com.dyst.entites;

/**
 * Jcd entity. @author MyEclipse Persistence Tools
 */

@SuppressWarnings("serial")
public class Jcd implements java.io.Serializable {
	private String id;
	private String tpcflj;

	// Constructors

	/** default constructor */
	public Jcd() {
	}

	/** minimal constructor */
	public Jcd(String id, String tpcflj) {
		this.id = id;
		this.tpcflj = tpcflj;
	}

	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getTpcflj() {
		return tpcflj;
	}

	public void setTpcflj(String tpcflj) {
		this.tpcflj = tpcflj;
	}
}