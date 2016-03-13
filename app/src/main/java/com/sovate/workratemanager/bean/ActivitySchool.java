package com.sovate.workratemanager.bean;

import java.util.ArrayList;

public class ActivitySchool {

	String name = "";
	String id = "";
	String section = "";
	
	ArrayList<ActivityGrade> listGrade = new ArrayList<ActivityGrade>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public ArrayList<ActivityGrade> getListGrade() {
		return listGrade;
	}

	public void setListGrade(ArrayList<ActivityGrade> listGrade) {
		this.listGrade = listGrade;
	}

	@Override
	public String toString() {
		return "ActivitySchool [name=" + name + ", id=" + id + ", section=" + section + ", listGrade=" + listGrade
				+ "]";
	}
	
	
}
