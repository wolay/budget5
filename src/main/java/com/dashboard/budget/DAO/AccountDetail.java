package com.dashboard.budget.DAO;

import org.openqa.selenium.By;

public class AccountDetail {
	private int code;
	private String url;
	private String usernameLocator;
	private String usernameValue;
	private String passwordLocator;
	private String passwordValue;
	private String loginLocator;
	private By logoutLocator;
	private By dueDateLocator;
	private By dueAmountLocator;
	private By allAccountsLinkLocator;
	private String transactionsPageUrl;
	private By detailsLinkLocator;
	private By periodSwitchLocator;
	private By periodSwitchSupLocator;
	private String transTableLocator;
	private String transTableSupLocator;
	private String transDateLocator;
	private Integer transDateFormat;
	private String transDescriptionLocator;
	private String transDescriptionSupLocator;
	private String transAmountLocator;
	private String transAmountSupLocator;

	public AccountDetail(int code, String url, String usernameLocator, String usernameValue, String passwordLocator,
			String passwordValue, String loginLocator, By logoutLocator) {
		super();
		this.code = code;
		this.url = url;
		this.usernameLocator = usernameLocator;
		this.usernameValue = usernameValue;
		this.passwordLocator = passwordLocator;
		this.passwordValue = passwordValue;
		this.loginLocator = loginLocator;
		this.logoutLocator = logoutLocator;
	}

	public AccountDetail(int code, String url, String usernameLocator, String usernameValue, String passwordLocator,
			String passwordValue, String loginLocator, By logoutLocator, By dueDateLocator, By dueAmountLocator, By allAccountsLinkLocator,
			String transactionsPageUrl, By detailsLinkLocator, By periodSwitchLocator, By periodSwitchSupLocator,
			String transTableLocator, String transTableSupLocator, String transDateLocator, Integer transDateFormat,
			String transDescriptionLocator, String transDescriptionSupLocator, String transAmountLocator,
			String transAmountSupLocator) {
		super();
		this.code = code;
		this.url = url;
		this.usernameLocator = usernameLocator;
		this.usernameValue = usernameValue;
		this.passwordLocator = passwordLocator;
		this.passwordValue = passwordValue;
		this.loginLocator = loginLocator;
		this.logoutLocator = logoutLocator;
		this.dueDateLocator = dueDateLocator;
		this.dueAmountLocator = dueAmountLocator;
		this.allAccountsLinkLocator = allAccountsLinkLocator;
		this.transactionsPageUrl = transactionsPageUrl;
		this.detailsLinkLocator = detailsLinkLocator;
		this.periodSwitchLocator = periodSwitchLocator;
		this.periodSwitchSupLocator = periodSwitchSupLocator;
		this.transTableLocator = transTableLocator;
		this.transTableSupLocator = transTableSupLocator;
		this.transDateLocator = transDateLocator;
		this.transDateFormat = transDateFormat;
		this.transDescriptionLocator = transDescriptionLocator;
		this.transDescriptionSupLocator = transDescriptionSupLocator;
		this.transAmountLocator = transAmountLocator;
		this.transAmountSupLocator = transAmountSupLocator;
	}

	public int getCode() {
		return code;
	}

	public String getUrl() {
		return url;
	}

	public String getUsernameLocator() {
		return usernameLocator;
	}

	public String getUsernameValue() {
		return usernameValue;
	}

	public String getPasswordLocator() {
		return passwordLocator;
	}

	public String getPasswordValue() {
		return passwordValue;
	}

	public String getLoginLocator() {
		return loginLocator;
	}

	public By getLogoutLocator() {
		return logoutLocator;
	}
	
	public By getDueDateLocator() {
		return dueDateLocator;
	}
	

	public By getDueAmountLocator() {
		return dueAmountLocator;
	}

	public By getAllAccountsLinkLocator() {
		return allAccountsLinkLocator;
	}

	public String getTransactionsPageUrl() {
		return transactionsPageUrl;
	}

	public By getDetailsLinkLocator() {
		return detailsLinkLocator;
	}

	public By getPeriodSwitchLocator() {
		return periodSwitchLocator;
	}

	public By getPeriodSwitchSupLocator() {
		return periodSwitchSupLocator;
	}

	public String getTransTableLocator() {
		return transTableLocator;
	}

	public String getTransTableSupLocator() {
		return transTableSupLocator;
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
}
