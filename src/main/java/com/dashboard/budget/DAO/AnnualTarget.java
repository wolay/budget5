package com.dashboard.budget.DAO;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "annual_target")
public class AnnualTarget{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@Type(type = "date")
	private Date date;
	private Double amount;

	public AnnualTarget() {
	}

	public AnnualTarget(Date date, Double amount) {
		this.date = date;
		this.amount = amount;
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
}
