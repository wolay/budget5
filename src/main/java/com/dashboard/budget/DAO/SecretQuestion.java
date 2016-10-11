package com.dashboard.budget.DAO;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "secret_questions")
public class SecretQuestion {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@ManyToOne
	@JoinColumn(name = "bank_id")
	private Bank bank;
	private String question;
	private String answer;
	
	public SecretQuestion(){}
	public SecretQuestion(Bank bank, String question, String answer) {
		this.bank = bank;
		this.question = question;
		this.answer = answer;
	}
	
	public int getId() {
		return id;
	}
	
	public Bank getBank() {
		return bank;
	}
	
	public String getQuestion() {
		return question;
	}
	
	public String getAnswer() {
		return answer;
	}
	
	@Override
	public String toString() {
		return "SecretQuestion [id=" + id + ", bank=" + bank + ", question=" + question + ", answer=" + answer + "]";
	}
	
}
