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
@Table(name = "accounts_details_navigation")
public class AccountDetailsNavigation {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@OneToOne(mappedBy = "accountDetailsNavigation")
	private Account account;
	private String allAccountsLinkLocator;
	private String transactionsPageUrl;
	private String detailsLinkLocator;
	private String detailsLinkSupLocator;
	private boolean switchWindowFotTransactions;
	private String actionToSwitchPeriod;
	private String periodSwitchLocator;
	private String periodSwitchPreLocator;
	private String periodSwitchPostLocator;
	private String secretQuestionLocator;
	private String secretAnswerLocator;
	private String secretSubmitLocator;
	

	public AccountDetailsNavigation() {
	}

	public AccountDetailsNavigation(Account account, String allAccountsLinkLocator, String transactionsPageUrl,
			String detailsLinkLocator, String actionToSwitchPeriod, String periodSwitchLocator,
			String periodSwitchPreLocator, String periodSwitchPostLocator, String secretQuestionLocator, String secretAnswerLocator, String secretSubmitLocator) {
		this.account = account;
		this.allAccountsLinkLocator = allAccountsLinkLocator;
		this.transactionsPageUrl = transactionsPageUrl;
		this.detailsLinkLocator = detailsLinkLocator;
		this.actionToSwitchPeriod = actionToSwitchPeriod;
		this.periodSwitchLocator = periodSwitchLocator;
		this.periodSwitchPreLocator = periodSwitchPreLocator;
		this.periodSwitchPostLocator = periodSwitchPostLocator;
		this.secretQuestionLocator = secretQuestionLocator;
		this.secretAnswerLocator = secretAnswerLocator;
		this.secretSubmitLocator = secretSubmitLocator;

		this.account.setAccountDetailsNavigation(this);
	}

	public int getId() {
		return id;
	}

	public Account getAccount() {
		return account;
	}

	public By getAllAccountsLinkLocator() {
		return Util.getByLocator(allAccountsLinkLocator);
	}

	public String getTransactionsPageUrl() {
		return transactionsPageUrl;
	}

	public By getDetailsLinkLocator() {
		return Util.getByLocator(detailsLinkLocator);
	}

	public By getDetailsLinkSupLocator() {
		return Util.getByLocator(detailsLinkSupLocator);
	}

	public boolean getSwitchWindowForTransactions() {
		return switchWindowFotTransactions;
	}

	public By getPeriodSwitchLocator() {
		return Util.getByLocator(periodSwitchLocator);
	}

	public String getActionToSwitchPeriod() {
		return actionToSwitchPeriod;
	}

	public By getPeriodSwitchPreLocator() {
		return Util.getByLocator(periodSwitchPreLocator);
	}
	
	public By getPeriodSwitchPostLocator() {
		return Util.getByLocator(periodSwitchPostLocator);
	}

	public By getSecretQuestionLocator() {
		return Util.getByLocator(secretQuestionLocator);
	}

	public By getSecretAnswerLocator() {
		return Util.getByLocator(secretAnswerLocator);
	}
	
	public By getSecretSubmitLocator() {
		return Util.getByLocator(secretSubmitLocator);
	}

}
