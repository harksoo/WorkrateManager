package com.sovate.workratemanager.bean;

import java.util.HashMap;
import java.util.List;


public class ActivityBaseInfo {

	HashMap<String, ActivitySchool> mapSchool = new HashMap<String, ActivitySchool>();
	
	List<ActivitySport> listSport;


	


	public HashMap<String, ActivitySchool> getMapSchool() {
		return mapSchool;
	}

	public void setMapSchool(HashMap<String, ActivitySchool> mapSchool) {
		this.mapSchool = mapSchool;
	}

	public List<ActivitySport> getListSport() {
		return listSport;
	}

	public void setListSport(List<ActivitySport> listSport) {
		this.listSport = listSport;
	}

	@Override
	public String toString() {
		return "ActivityBaseInfo [mapSchool=" + mapSchool + ", listSport=" + listSport + "]";
	}

}
