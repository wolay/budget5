package com.dashboard.budget.DAO;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.dashboard.budget.DataHandler;

@Entity
@Table(name = "accounts")
public class Account{
	
	@Id
	private int id;
	private String name;
	private String bank;
	private String browser;
	private String owner;
	public Integer priority; 
	
	public Account(){}
	public Account(int id, String name, String bank, String browser, String owner, int priority) {
		this.id    = id;
		this.name    = name;
		this.bank    = bank;
		this.browser = browser;
		this.owner   = owner;
		this.priority = priority;
	}

	public int getId() {
		return id;
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
		return "Account [id=" + id + ", name=" + name + ", bank=" + bank + ", browser=" + browser + ", owner="
				+ owner + ", priority=" + priority + "]";
	}

}
