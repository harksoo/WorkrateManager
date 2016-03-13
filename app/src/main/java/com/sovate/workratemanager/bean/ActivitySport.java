package com.sovate.workratemanager.bean;

public class ActivitySport {

	String id = "";
	String name = "";
	String smallImagePath = "";
	String largeImagePath = "";
	
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
	public String getSmallImagePath() {
		return smallImagePath;
	}
	public void setSmallImagePath(String smallImagePath) {
		this.smallImagePath = smallImagePath;
	}
	public String getLargeImagePath() {
		return largeImagePath;
	}
	public void setLargeImagePath(String largeImagePath) {
		this.largeImagePath = largeImagePath;
	}
	
	@Override
	public String toString() {
		return "ActivitySport [id=" + id + ", name=" + name + ", smallImagePath=" + smallImagePath + ", largeImagePath="
				+ largeImagePath + "]";
	}
	
	
}
