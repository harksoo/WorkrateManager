package com.sovate.workratemanager.bean;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityWorkRate {

	public static DateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH");

	@SerializedName("userId")
	String userId = "";

	@SerializedName("mac")
	String mac = "";

	@SerializedName("sportId")
	String sportId = "";

	// 날짜 데이터도 string으로 처리 하도록 구성
	@SerializedName("collectDt")
	String collectDt = "";

	@SerializedName("steps")
	String steps = "";

	@SerializedName("calorie")
	String calorie = "";

	@SerializedName("distance")
	String distance = "";

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getSportId() {
		return sportId;
	}
	public void setSportId(String sportId) {
		this.sportId = sportId;
	}
	public String getCollectDt() {
		return collectDt;
	}
	public void setCollectDt(String collectDt) {
		this.collectDt = collectDt;
	}
	public String getSteps() {
		return steps;
	}
	public void setSteps(String steps) {
		this.steps = steps;
	}
	public String getCalorie() {
		return calorie;
	}
	public void setCalorie(String calorie) {
		this.calorie = calorie;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
	
	@Override
	public String toString() {
		return "ActivityWorkRate [userId=" + userId + ", mac=" + mac + ", sportId=" + sportId + ", collectDt="
				+ collectDt + ", steps=" + steps + ", calorie=" + calorie + ", distance=" + distance + "]";
	}
	
	
	
}
