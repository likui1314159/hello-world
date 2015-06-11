package com.dyst.entites;

import java.util.Set;

@SuppressWarnings("serial")
public class PfTemp implements java.io.Serializable,Comparable<PfTemp>{

	private String jcdid;
	private int index1;
	private int index2;
	private int count;
	private String tpid;
	private Set<String> setTpid; 
	public String getJcdid() {
		return jcdid;
	}
	public int getIndex1() {
		return index1;
	}
	public int getIndex2() {
		return index2;
	}
	public void setJcdid(String jcdid) {
		this.jcdid = jcdid;
	}
	public void setIndex1(int index1) {
		this.index1 = index1;
	}
	public void setIndex2(int index2) {
		this.index2 = index2;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getTpid() {
		return tpid;
	}
	public Set<String> getSetTpid() {
		return setTpid;
	}
	public void setTpid(String tpid) {
		this.tpid = tpid;
	}
	public void setSetTpid(Set<String> setTpid) {
		this.setTpid = setTpid;
	}
	public int compareTo(PfTemp o) {
		return this.jcdid.compareTo(o.getJcdid());
	}
}