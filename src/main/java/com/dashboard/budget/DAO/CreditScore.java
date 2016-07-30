package com.dashboard.budget.DAO;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "credit_scores")
public class CreditScore {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@Type(type = "date")
	private Date date;
	@ManyToOne
	@JoinColumn(name = "account_id")
	private Account account;
	private String name;
	private int score;
	private int difference;
	
	public CreditScore(){}
	public CreditScore(Account account, String name, int score, int difference) {
		this.date = new Date();
		this.account = account;
		this.name = name;
		this.score = score;
		this.difference = difference;
	}
	
	public int getId() {
		return id;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getScore() {
		return score;
	}
	
	public int getDifference() {
		return difference;
	}
	
	public Account getAccount(){
		return this.account;
	}

}
