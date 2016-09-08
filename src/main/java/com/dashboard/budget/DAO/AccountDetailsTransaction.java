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
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	@OneToOne(mappedBy="accountDetailsTransaction")
	private Account account;
	private String transTableLocator;
	private String transTableSupLocator;
	private String transDateLocator;
	private Integer transDateFormat;
	private String transDescriptionLocator;
	private String transDescriptionSupLocator;
	private String transAmountLocator;
	private String transAmountSupLocator;
	private String transCategoryNavLocator;
	private String transCategoryLocator;
	private String transCategorySupLocator;
	
	public AccountDetailsTransaction(){}
	public AccountDetailsTransaction(Account account, String transTableLocator, String transTableSupLocator, String transDateLocator, Integer transDateFormat,
			String transDescriptionLocator, String transDescriptionSupLocator, String transAmountLocator,
			String transAmountSupLocator, String transCategoryNavLocator, String transCategoryLocator, String transCategorySupLocator) {
		super();
		this.account = account;
		this.transTableLocator = transTableLocator;
		this.transTableSupLocator = transTableSupLocator;
		this.transDateLocator = transDateLocator;
		this.transDateFormat = transDateFormat;
		this.transDescriptionLocator = transDescriptionLocator;
		this.transDescriptionSupLocator = transDescriptionSupLocator;
		this.transAmountLocator = transAmountLocator;
		this.transAmountSupLocator = transAmountSupLocator;
		this.transCategoryNavLocator = transCategoryNavLocator;
		this.transCategoryLocator = transCategoryLocator;
		this.transCategorySupLocator = transCategorySupLocator;
		
		this.account.setAccountDetailsTransaction(this);
	}
	
	public int getId() {
		return id;
	}
	
	public Account getAccount(){
		return account;
	}
	
	public By getTransTableLocator() {
		return Util.getByLocator(transTableLocator);
	}

	public By getTransTableSupLocator() {
		return Util.getByLocator(transTableSupLocator);
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

	public String getTransDescriptionSupLocator() {
		return transDescriptionSupLocator;
	}

	public String getTransAmountLocator() {
		return transAmountLocator;
	}

	public String getTransAmountSupLocator() {
		return transAmountSupLocator;
	}
	
	public String getTransCategoryNavLocator() {
		return transCategoryNavLocator;
	}
	
	public String getTransCategoryLocator() {
		return transCategoryLocator;
	}	
	
	public String getTransCategorySupLocator() {
		return transCategorySupLocator;
	}
	
}
