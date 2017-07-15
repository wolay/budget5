package com.dashboard.budget.DAO;

public enum DataRetrievalStatus {

	SUCCESS("S"), 
	NO_MATCH_FOR_TOTAL("T"),
	NAVIGATION_BROKEN("N"),
	SERVICE_UNAVAILABLE("U"),
	UNKNOWN("?");
	
	String abbr;
	DataRetrievalStatus(String a){
		this.abbr = a;
	} 
	
	public String getAbbr(){
		return abbr;
	}
	
}
