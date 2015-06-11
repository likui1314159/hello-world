package com.dyst.entites;

@SuppressWarnings("serial")
public class SbTemp implements java.io.Serializable{

	private String cphm1;
	private int count;
	private int[] sequence;
	private double propability;
	
	public String getCphm1() {
		return cphm1;
	}
	public void setCphm1(String cphm1) {
		this.cphm1 = cphm1;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int[] getSequence() {
		return sequence;
	}
	public void setSequence(int[] sequence) {
		this.sequence = sequence;
	}
	public double getPropability() {
		return propability;
	}
	public void setPropability(double propability) {
		this.propability = propability;
	}



}