package com.dyst.entites;

import java.util.Set;

@SuppressWarnings("serial")
public class SbC implements java.io.Serializable,Comparable<SbC>{

	private String description;
	private Integer count;
	private String jcdid;
	private Set<String> setTpid; 
	
	public String getDescription() {
		return description;
	}
	public Integer getCount() {
		return count;
	}
	public String getJcdid() {
		return jcdid;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public void setJcdid(String jcdid) {
		this.jcdid = jcdid;
	}
	public Set<String> getSetTpid() {
		return setTpid;
	}
	public void setSetTpid(Set<String> setTpid) {
		this.setTpid = setTpid;
	}
	public int compareTo(SbC o) {
		return this.count.compareTo(o.getCount());
	}

}