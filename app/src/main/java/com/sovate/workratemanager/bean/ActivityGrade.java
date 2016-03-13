package com.sovate.workratemanager.bean;

public class ActivityGrade {

	String id = "";
	String name = "";
	String classCount = "";
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getClassCount() {
		return classCount;
	}
	public void setClassCount(String classCount) {
		this.classCount = classCount;
	}
	
	@Override
	public String toString() {
		return "ActivityGrade [id=" + id + ", name=" + name + ", classCount=" + classCount + "]";
	}
	
	
}
