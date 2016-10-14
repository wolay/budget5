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
	@ManyToOne
	@JoinColumn(name = "account_id")
	private Account account;
	private String question;
	private String answer;

	public SecretQuestion() {
	}

	public SecretQuestion(Bank bank, Account account, String question, String answer) {
		this.bank = bank;
		this.account = account;
		this.question = question;
		this.answer = answer;
	}

	public int getId() {
		return id;
	}

	public Bank getBank() {
		return bank;
	}

	public Account getAccount() {
		return account;
	}

	public String getQuestion() {
		return question;
	}

	public String getAnswer() {
		return answer;
	}

	@Override
	public String toString() {
		return "SecretQuestion [id=" + id + ", bank=" + bank + ", account=" + account + ", question=" + question
				+ ", answer=" + answer + "]";
	}

}
