package com.dashboard.budget.pages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.Config;
import com.dashboard.budget.DataHandler;
import com.dashboard.budget.UberWebDriver;
import com.dashboard.budget.Util;
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
		this.accountDetails = DataHandler.getAccountsDetailsByAccount(account);
	}

	public synchronized boolean login() {
		WebElement username = webDriver.findElement(fldUsername);
		if (username == null)
			return false;
		username.sendKeys(accountDetails.getUsernameValue());
		WebElement password = webDriver.findElement(fldPassword);
		if (password == null)
			return false;
		password.sendKeys(accountDetails.getPasswordValue());
		WebElement login = webDriver.findElement(btnLogin);
		if (login == null)
			return false;
		login.click();
		return true;
	}

	public void gotoHomePage() {
		webDriver.get(accountDetails.getUrl());
	}

	public abstract Double getTotal();

	public List<Transaction> getTransactions(Double difference, List<Transaction> prevTransactions) {
		// check if getting transactions is enabled for account
		if (accountDetails.getTransTableLocator() == null)
			return new ArrayList<Transaction>();

		String code = account.getCode();
		List<Transaction> result = new ArrayList<Transaction>();

		// details link does not exist in WF case
		if (accountDetails.getDetailsLinkLocator() != null) {
			WebElement weDetails = webDriver.findElement(accountDetails.getDetailsLinkLocator());
			if (weDetails != null) {
				// wait for Amazon (without wait button visible but not
				// clickable)
				Util.sleep(1000);
				weDetails.click();
			} else
				return result;
		} else if (accountDetails.getTransactionsPageUrl() != null) {
			webDriver.get(accountDetails.getTransactionsPageUrl());
	        webDriver.switchTo().defaultContent();
		} else
			return new ArrayList<Transaction>();

		By byDate = By.xpath(accountDetails.getTransDateLocator());
		By byAmount = By.xpath(accountDetails.getTransAmountLocator());
		By byAmountSup = (accountDetails.getTransAmountSupLocator() == null) ? null
				: By.xpath(accountDetails.getTransAmountSupLocator());
		By byDescription = By.xpath(accountDetails.getTransDescriptionLocator());
		By byDescriptionSup = (accountDetails.getTransDescriptionSupLocator() == null) ? null
				: By.xpath(accountDetails.getTransDescriptionSupLocator());
		Integer dateFormat = accountDetails.getTransDateFormat();

		// current period transactions
		List<WebElement> rows;
		// Little trick for Citi - pending transactions populate in /table/tboby
		// and posted transaction populate in /table/tbody[2]
		// but.. if there is no pending transactions then posted transactions
		// populate in /table/tbody
		if (webDriver.findElement(By.xpath(accountDetails.getTransTableLocator())) == null)
			rows = webDriver.findElements(By.xpath(accountDetails.getTransTableSupLocator()));
		else
			rows = webDriver.findElements(By.xpath(accountDetails.getTransTableLocator()));
		for (WebElement row : rows) {
			if (!Util.isValidTransactionRow(row.getText()))
				continue;
			double amount;
			if (byAmountSup == null)
				amount = -Util.convertStringAmountToDouble(row.findElement(byAmount).getText());
			else
				amount = (" ".equals(row.findElement(byAmount).getText()))
						? -Util.convertStringAmountToDouble(row.findElement(byAmountSup).getText())
						: Util.convertStringAmountToDouble(row.findElement(byAmount).getText());

			Date date = Util.convertStringToDateByType(row.findElement(byDate).getText(), dateFormat);
			String description = row.findElement(byDescription).getText().trim().replace("\n", "-");
			if ("".equals(description))
				description = row.findElement(byDescriptionSup).getText().trim().replace("\n", "-");
			List<Transaction> matchTransactions = prevTransactions.stream()
					.filter(t -> t.getDate().equals(date) && t.getAmount() == amount).collect(Collectors.toList());
			if (matchTransactions.isEmpty()) {
				result.add(new Transaction(code, date, description, amount, ""));
				difference = Util.roundDouble(difference - amount);
				if (difference == 0.0) {
					if (accountDetails.getAllAccountsLinkLocator() != null) {
						WebElement accounts = webDriver.findElement(accountDetails.getAllAccountsLinkLocator());
						if (accounts != null)
							accounts.click();
						Util.sleep(3000);
					}
					return result;
				}
			}
		}

		// for some accounts previous period transactions are not considered
		// (i.e. WF)
		if (accountDetails.getPeriodSwitchLocator() != null) {
			// previous period transactions
			new Select(webDriver.findElement(accountDetails.getPeriodSwitchLocator())).selectByIndex(1);
			rows = webDriver.findElements(By.xpath(accountDetails.getTransTableLocator()));
			for (WebElement row : rows) {
				if (!Util.isValidTransactionRow(row.getText()))
					continue;
				double amount = -Util.convertStringAmountToDouble(row.findElement(byAmount).getText());
				Date date = Util.convertStringToDateByType(row.findElement(byDate).getText().trim(), dateFormat);
				String description = row.findElement(byDescription).getText().trim().replace("\n", "-");
				result.add(new Transaction(code, date, description, amount, ""));
				difference = Util.roundDouble(difference - amount);
				if (difference == 0.0) {
					if (accountDetails.getAllAccountsLinkLocator() != null) {
						WebElement accounts = webDriver.findElement(accountDetails.getAllAccountsLinkLocator());
						if (accounts != null)
							accounts.click();
						Util.sleep(3000);
					}
					return result;
				}
			}
		}

		WebElement accounts = webDriver.findElement(accountDetails.getAllAccountsLinkLocator());
		if (accounts != null)
			accounts.click();
		if (difference == 0.0)
			return result;
		else
			return new ArrayList<Transaction>();
	}

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
