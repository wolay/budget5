package com.dashboard.budget.DAO;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.openqa.selenium.By;

import com.dashboard.budget.Util;

@Entity
@Table(name = "accounts_details_total")
public class AccountDetailsTotal {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	@OneToOne
	@JoinColumn(name="account_id")
	private Account account;
	private String dueDateLocator;
	private String dueAmountLocator;

	public AccountDetailsTotal(Account account, String dueDateLocator, String dueAmountLocator) {
		super();
		this.account = account;
		this.dueDateLocator = dueDateLocator;
		this.dueAmountLocator = dueAmountLocator;
	}
	
	public int getId() {
		return id;
	}
	
	public By getDueDateLocator() {
		return Util.getByLocator(dueDateLocator);
	}
	
	public By getDueAmountLocator() {
		return Util.getByLocator(dueAmountLocator);
	}
}
