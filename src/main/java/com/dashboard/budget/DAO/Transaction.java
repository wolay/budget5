package com.dashboard.budget.DAO;

import java.util.Date;

public class Transaction {
	private String code;
	private Date date;
	private String decription;
	private double amount;
	private String category;
	
	public Transaction(String code, Date date, String decription, double amount, String category) {
		this.code = code;
		this.date = date;
		this.decription = decription;
		this.amount = amount;
		this.category = category;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDecription() {
		return decription;
	}

	public void setDecription(String decription) {
		this.decription = decription;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return "Transaction [code=" + code + ", date=" + date + ", decription=" + decription + ", amount=" + amount
				+ ", category=" + category + "]";
	}

}
