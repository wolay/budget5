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
@Table(name = "accounts_details_login")
public class AccountDetailsLogin {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@OneToOne(mappedBy = "accountDetailsLogin")
	private Account account;
	private String usernameLocator;
	private String usernameValue;
	private String userCheckLocator;
	private Boolean advancedFlow;
	private String passwordLocator;
	private String passwordValue;
	private String loginLocator;
	private String logoutLocator;
	private String logoutPostLocator;

	public AccountDetailsLogin() {
	}

	public AccountDetailsLogin(Account account, String usernameLocator, String usernameValue, String userCheckLocator,
			Boolean advancedFlow, String passwordLocator, String passwordValue, String loginLocator,
			String logoutLocator) {
		super();
		this.account = account;
		this.usernameLocator = usernameLocator;
		this.usernameValue = usernameValue;
		this.userCheckLocator = userCheckLocator;
		this.advancedFlow = advancedFlow;
		this.passwordLocator = passwordLocator;
		this.passwordValue = passwordValue;
		this.loginLocator = loginLocator;
		this.logoutLocator = logoutLocator;

		this.account.setAccountDetailsLogin(this);
	}

	public int getId() {
		return id;
	}

	public Account getAccount() {
		return account;
	}

	public By getUsernameLocator() {
		return Util.getByLocator(usernameLocator);
	}

	public String getUsernameValue() {
		return usernameValue;
	}

	public By getUserCheckLocator() {
		return Util.getByLocator(userCheckLocator);
	}

	public Boolean isAdvancedFlow() {
		return (advancedFlow == null) ? false : advancedFlow;
	}

	public By getPasswordLocator() {
		return Util.getByLocator(passwordLocator);
	}

	public String getPasswordValue() {
		return passwordValue;
	}

	public By getLoginLocator() {
		return Util.getByLocator(loginLocator);
	}

	public By getLogoutLocator() {
		return Util.getByLocator(logoutLocator);
	}

	public By getLogoutPostLocator() {
		return Util.getByLocator(logoutPostLocator);
	}

}
