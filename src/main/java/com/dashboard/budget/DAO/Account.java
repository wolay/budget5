package com.dashboard.budget.DAO;

import com.dashboard.budget.DataHandler;

public class Account{
	
	private int code;
	private String name;
	private String bank;
	private String browser;
	private String owner;
	public Integer priority; 
	
	public Account(int code, String name, String bank, String browser, String owner, int priority) {
		this.code    = code;
		this.name    = name;
		this.bank    = bank;
		this.browser = browser;
		this.owner   = owner;
		this.priority = priority;
	}
	
	public Account(String name, String bank, String owner, int priority) {
		this.name    = name;
		this.bank    = bank;
		this.owner   = owner;
		this.priority = priority;
	}

	public int getCode() {
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

	@Override
	public String toString() {
		return "Account [code=" + code + ", name=" + name + ", bank=" + bank + ", browser=" + browser + ", owner="
				+ owner + ", priority=" + priority + "]";
	}

}
