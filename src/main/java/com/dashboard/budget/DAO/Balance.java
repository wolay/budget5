package com.dashboard.budget.DAO;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "balances")
public class Balance {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@Type(type = "date")
	private Date date;
	private Double amount;
	private Double difference;

	public Balance() {
	}

	public Balance(Date date, Double amount, Double difference) {
		this.date = date;
		this.amount = amount;
		this.difference = difference;
	}

	public Balance(Double amount, Double difference) {
		this.date = new Date();
		this.amount = amount;
		this.difference = difference;
	}

	public int getId() {
		return id;
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

	@Override
	public String toString() {
		return "Balance [id=" + id + ", date=" + date + ", amount=" + amount + ", difference=" + difference + "]";
	}

}
