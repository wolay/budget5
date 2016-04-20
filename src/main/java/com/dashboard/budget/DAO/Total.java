package com.dashboard.budget.DAO;

import java.util.Date;

public class Total implements Comparable<Object>{
	
	private Date date;
	private Account account;
	private Double amount;
	private Double difference;
	private DataRetrievalStatus status;
	
	public Total(Date date, Account account, Double amount, Double difference) {
		this.date = date;
		this.account = account;
		this.amount = amount;
		this.difference = difference;
	}
	
	public Total(Account account, Double amount, Double difference, DataRetrievalStatus status) {
		this.date = new Date();
		this.account = account;
		this.amount = amount;
		this.difference = difference;
		this.status = status;
	}
	
	public Total(Account account, Double amount, Double difference) {
		this.date = new Date();
		this.account = account;
		this.amount = amount;
		this.difference = difference;
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	public Double getAmount() {
		return amount;
	}
	
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	
	public Double getDifference() {
		return difference;
	}
	
	public void setDifference(Double difference) {
		this.difference = difference;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}
	
	public DataRetrievalStatus getStatus() {
		return status;
	}

	public void setStatus(DataRetrievalStatus status) {
		this.status = status;
	}


	@Override
	public int compareTo(Object o) {
		int compareQuantity = Integer.valueOf(((Total) o).getAccount().getCode()); 
		
		//ascending order
		return Integer.valueOf(this.getAccount().getCode()) - compareQuantity;
	}
}
