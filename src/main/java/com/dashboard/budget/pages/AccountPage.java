package com.dashboard.budget.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.Config;
import com.dashboard.budget.DataHandler;
import com.dashboard.budget.UberWebDriver;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.AccountDetail;
import com.dashboard.budget.DAO.Transaction;

public abstract class AccountPage implements Config {

	protected static Logger logger = LoggerFactory.getLogger(AccountPage.class);

	protected Account account;
	protected AccountDetail accountDetails;

	protected UberWebDriver webDriver;
	protected WebDriverWait wait;

	protected By fldUsername;
	protected By fldPassword;
	protected By btnLogin;
	protected By btnLogout;
	protected WebElement amount;

	public AccountPage(Account account) {
		this.account = account;
		this.accountDetails = DataHandler.getAccountsDetailsByAccount(account);
		fldUsername = By.id(accountDetails.getUsernameLocator());
		fldPassword = By.id(accountDetails.getPasswordLocator());
		btnLogin = By.id(accountDetails.getLoginLocator());
		btnLogout = accountDetails.getLogoutLocator();

		this.webDriver = new UberWebDriver();
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);
	}

	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public synchronized boolean login() {
		WebElement username = webDriver.findElement(fldUsername);
		if(username == null)
			return false;
		username.sendKeys(accountDetails.getUsernameValue());
		WebElement password = webDriver.findElement(fldPassword);
		if(password == null)
			return false;		
		password.sendKeys(accountDetails.getPasswordValue());
		WebElement login = webDriver.findElement(btnLogin);
		if(login == null)
			return false;		
		login.click();		
		return true;
	}

	public void gotoHomePage() {
		webDriver.get(accountDetails.getUrl());
	}

	public abstract Double getTotal();

	public abstract List<Transaction> getTransactions(Double difference, List<Transaction> prevTransactions);

	public int getScore() {
		return 0;
	}

	public void quit() {
		WebElement logout = webDriver.findElement(btnLogout);
		if (logout != null) {
			logout.click();
			logger.info("Account page {} was closed", account.getName());
		} else
			logger.error("Account page {} was not closed properly", account.getName());
		webDriver.quit();
	}
}
