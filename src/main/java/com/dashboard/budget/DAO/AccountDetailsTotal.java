package com.dashboard.budget.DAO;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
	@OneToOne(mappedBy="accountDetailsTotal")
	private Account account;
	private String dueDateLocator;
	private String dueAmountLocator;
	
	public AccountDetailsTotal(){}
	public AccountDetailsTotal(Account account, String dueDateLocator, String dueAmountLocator) {
		super();
		this.account = account;
		this.dueDateLocator = dueDateLocator;
		this.dueAmountLocator = dueAmountLocator;
		
		this.account.setAccountDetailsTotal(this);
	}
	
	public int getId() {
		return id;
	}
	
	public Account getAccount(){
		return account;
	}
	
	public By getDueDateLocator() {
		return Util.getByLocator(dueDateLocator);
	}
	
	public By getDueAmountLocator() {
		return Util.getByLocator(dueAmountLocator);
	}
}
