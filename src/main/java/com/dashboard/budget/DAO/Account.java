package com.dashboard.budget.DAO;

import com.dashboard.budget.DataHandler;

public class Account {
	
	private String code;
	private String name;
	private String bank;
	private String browser;
	private String owner;
	private int priority; 
	
	public Account(String code, String name, String bank, String browser, String owner, int priority) {
		this.code    = code;
		this.name    = name;
		this.bank    = bank;
		this.browser = browser;
		this.owner   = owner;
		this.priority = priority;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getBank() {
		return bank;
	}
	
	public String getBrowser() {
		return browser;
	}

	public String getOwner() {
		return owner;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public String getUrl(){
		return DataHandler.getAccountsDetailsByAccount(this).getUrl();
	}

}
