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
@Table(name = "accounts_details_transaction")
public class AccountDetailsTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@OneToOne(mappedBy = "accountDetailsTransaction")
	private Account account;
	private String transTableLocator;
	private String transDateLocator;
	private Integer transDateFormat;
	private String transDescriptionLocator;
	private String transAmountLocator;
	private String transCategoryLocator;

	public AccountDetailsTransaction() {
	}

	public AccountDetailsTransaction(Account account, String transTableLocator, String transDateLocator,
			Integer transDateFormat, String transDescriptionLocator, String transAmountLocator,
			String transCategoryLocator) {
		super();
		this.account = account;
		this.transTableLocator = transTableLocator;
		this.transDateLocator = transDateLocator;
		this.transDateFormat = transDateFormat;
		this.transDescriptionLocator = transDescriptionLocator;
		this.transAmountLocator = transAmountLocator;
		this.transCategoryLocator = transCategoryLocator;

		this.account.setAccountDetailsTransaction(this);
	}

	public int getId() {
		return id;
	}

	public Account getAccount() {
		return account;
	}

	public By getTransTableLocator() {
		return Util.getByLocator(transTableLocator);
	}

	public String getTransDateLocator() {
		return transDateLocator;
	}

	public Integer getTransDateFormat() {
		return transDateFormat;
	}

	public String getTransDescriptionLocator() {
		return transDescriptionLocator;
	}

	public String getTransAmountLocator() {
		return transAmountLocator;
	}
	
	public String getTransCategoryLocator() {
		return transCategoryLocator;
	}

}
