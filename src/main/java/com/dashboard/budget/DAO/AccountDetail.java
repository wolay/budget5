package com.dashboard.budget.DAO;

import org.openqa.selenium.By;

public class AccountDetail {
	private String code;
	private String url;
	private String usernameLocator;
	private String usernameValue;
	private String passwordLocator;
	private String passwordValue;
	private String loginLocator;
	private By logoutLocator;

	public AccountDetail(String code, String url, String usernameLocator, String usernameValue, String passwordLocator,
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsernameLocator() {
		return usernameLocator;
	}

	public void setUsernameLocator(String usernameLocator) {
		this.usernameLocator = usernameLocator;
	}

	public String getUsernameValue() {
		return usernameValue;
	}

	public void setUsernameValue(String usernameValue) {
		this.usernameValue = usernameValue;
	}

	public String getPasswordLocator() {
		return passwordLocator;
	}

	public void setPasswordLocator(String passwordLocator) {
		this.passwordLocator = passwordLocator;
	}

	public String getPasswordValue() {
		return passwordValue;
	}

	public void setPasswordValue(String passwordValue) {
		this.passwordValue = passwordValue;
	}

	public String getLoginLocator() {
		return loginLocator;
	}

	public void setLoginLocator(String loginLocator) {
		this.loginLocator = loginLocator;
	}
	
	public By getLogoutLocator() {
		return logoutLocator;
	}

	public void setLogoutLocator(By logoutLocator) {
		this.logoutLocator = logoutLocator;
	}
}
