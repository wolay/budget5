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
import com.dashboard.budget.DAO.AccountDetailsLogin;
import com.dashboard.budget.DAO.AccountDetailsNavigation;
import com.dashboard.budget.DAO.AccountDetailsTotal;
import com.dashboard.budget.DAO.AccountDetailsTransaction;
import com.dashboard.budget.DAO.Category;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;

public abstract class AccountPage implements Config {

	protected static Logger logger = LoggerFactory.getLogger(AccountPage.class);

	protected Account account;
	protected AccountDetailsLogin accountLoginDetails;
	protected AccountDetailsNavigation accountNavigationDetails;
	protected AccountDetailsTotal accountTotalDetails;
	protected AccountDetailsTransaction accountTransactionDetails;
	protected DataHandler dataHandler;

	protected UberWebDriver webDriver;
	protected WebDriverWait wait;

	protected By fldUsername;
	protected By fldPassword;
	protected By btnLogin;
	protected By btnLogout;
	protected WebElement amount;

	public AccountPage(Account account, DataHandler dataHandler) {
		this.account = account;
		this.dataHandler = dataHandler;
		this.accountLoginDetails = account.getAccountDetailsLogin();
		this.accountNavigationDetails = account.getAccountDetailsNavigation();
		this.accountTotalDetails = account.getAccountDetailsTotal();
		this.accountTransactionDetails = account.getAccountDetailsTransaction();
		fldUsername = accountLoginDetails.getUsernameLocator();
		fldPassword = accountLoginDetails.getPasswordLocator();
		btnLogin = accountLoginDetails.getLoginLocator();
		btnLogout = accountLoginDetails.getLogoutLocator();

		this.webDriver = new UberWebDriver();
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);
	}

	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account account) {
		this.account = account;
		this.accountLoginDetails = account.getAccountDetailsLogin();
		this.accountNavigationDetails = account.getAccountDetailsNavigation();
		this.accountTotalDetails = account.getAccountDetailsTotal();
		this.accountTransactionDetails = account.getAccountDetailsTransaction();
	}

	public synchronized boolean login() {
		Util.sleep(3000); // for Best Buy card
		WebElement username = webDriver.findElement(fldUsername);
		if (username == null)
			return false;
		username.sendKeys(accountLoginDetails.getUsernameValue());
		WebElement password = webDriver.findElement(fldPassword);
		if (password == null)
			return false;
		password.sendKeys(accountLoginDetails.getPasswordValue());

		webDriver.findElement(btnLogin).click();
		return true;
	}

	public void gotoHomePage() {
		webDriver.get(account.getUrl());
	}

	public abstract Double getTotal();

	public List<Transaction> getTransactions(Total total, List<Transaction> prevTransactions) {
		// check if getting transactions is enabled for account
		if (accountTransactionDetails == null)
			return new ArrayList<Transaction>();

		List<Transaction> result = new ArrayList<Transaction>();

		// details link does not exist in WF case
		if (accountNavigationDetails != null && accountNavigationDetails.getDetailsLinkLocator() != null) {
			WebElement weDetails = webDriver.lookupElement(accountNavigationDetails.getDetailsLinkLocator());
			if (weDetails != null) {
				webDriver.waitToBeClickable(accountNavigationDetails.getDetailsLinkLocator());
				weDetails.click();
			} else
				return result;
		} else if (accountNavigationDetails != null && accountNavigationDetails.getTransactionsPageUrl() != null) {
			webDriver.get(accountNavigationDetails.getTransactionsPageUrl());
			webDriver.switchTo().defaultContent();
		}

		Double difference = 0.0;
		By byDate = By.xpath(accountTransactionDetails.getTransDateLocator());
		By byAmount = By.xpath(accountTransactionDetails.getTransAmountLocator());
		By byAmountSup = (accountTransactionDetails.getTransAmountSupLocator() == null) ? null
				: By.xpath(accountTransactionDetails.getTransAmountSupLocator());
		By byDescription = By.xpath(accountTransactionDetails.getTransDescriptionLocator());
		By byDescriptionSup = (accountTransactionDetails.getTransDescriptionSupLocator() == null) ? null
				: By.xpath(accountTransactionDetails.getTransDescriptionSupLocator());
		By byCategoryNav = (accountTransactionDetails.getTransCategoryNavLocator() == null) ? null
				: By.xpath(accountTransactionDetails.getTransCategoryNavLocator());
		By byCategory = (accountTransactionDetails.getTransCategoryLocator() == null) ? null
				: By.xpath(accountTransactionDetails.getTransCategoryLocator());
		Integer dateFormat = accountTransactionDetails.getTransDateFormat();

		// CURRENT PERIOD TRANSACTIONS
		List<WebElement> currentPeriodRows;
		// Little trick for Citi - pending transactions populate in /table/tboby
		// and posted transaction populate in /table/tbody[2]
		// but.. if there is no pending transactions then posted transactions
		// populate in /table/tbody
		if (webDriver.lookupElement(accountTransactionDetails.getTransTableLocator()) == null)
			if (accountTransactionDetails.getTransTableSupLocator() == null)
				currentPeriodRows = null;
			else
				currentPeriodRows = webDriver.findElements(accountTransactionDetails.getTransTableSupLocator());
		else
			currentPeriodRows = webDriver.findElements(accountTransactionDetails.getTransTableLocator());
		if (currentPeriodRows == null)
			logger.info("No rows found in the current period table");
		else {
			logger.info("Rows in the current period table: {}", currentPeriodRows.size());
			for (WebElement row : currentPeriodRows) {
				logger.info("Row in the current period table: {}", row.getText());
				if (Util.isPending(row.getText()))
					continue;
				if (Util.isPending(row.findElement(byDate).getText()))
					continue;
				Date date = Util.convertStringToDateByType(row.findElement(byDate).getText(), dateFormat);
				double amount;
				// Amount consideration got complicated due PayPal
				if (byAmountSup == null)
					amount = -Util.convertStringAmountToDouble(row.findElement(byAmount).getText());
				else {
					if ("negative".equals(row.findElement(byAmountSup).getText()))
						amount = -Util.convertStringAmountToDouble(row.findElement(byAmount).getText());
					else {
						if (row.findElements(byAmount).isEmpty())
							amount = -Util.convertStringAmountToDouble(row.findElement(byAmountSup).getText());
						else
							amount = (" ".equals(row.findElement(byAmount).getText()))
									? -Util.convertStringAmountToDouble(row.findElement(byAmountSup).getText())
									: Util.convertStringAmountToDouble(row.findElement(byAmount).getText());
					}

				}

				String description = "";
				if (byDescriptionSup == null || row.findElements(byDescriptionSup).size() == 0)
					description = row.findElement(byDescription).getText().trim().replace("\n", "-");
				else {
					description = row.findElement(byDescriptionSup).getText().trim().replace("\n", "-");
					if ("".equals(description))
						description = row.findElement(byDescription).getText().trim().replace("\n", "-");
				}

				List<Transaction> matchTransactions = prevTransactions.stream()
						.filter(t -> t.getDate().equals(date) && t.getAmount() == amount).collect(Collectors.toList());
				if (matchTransactions.isEmpty()) {
					// trying to get Category
					String categoryStr = null;
					Category category = null;
					if (byCategoryNav != null && row.findElements(byCategoryNav).size() > 0) {
						row.findElement(byCategoryNav).click();
						if (accountTransactionDetails.getTransCategoryLocator().startsWith("."))
							categoryStr = row.findElement(byCategory).getText();
						else // absolute path
							categoryStr = webDriver.findElement(byCategory).getText();
						category = dataHandler.recognizeCategory(categoryStr);
					}
					result.add(new Transaction(account, total, date, description, amount, categoryStr, category));
					difference = Util.roundDouble(difference - amount);
					logger.info("Amount: {}, diff: {}", amount, difference);
					if (difference == 0.0) {
						if (accountNavigationDetails != null
								&& accountNavigationDetails.getAllAccountsLinkLocator() != null) {
							WebElement accounts = webDriver
									.findElement(accountNavigationDetails.getAllAccountsLinkLocator());
							if (accounts != null)
								accounts.click();
							Util.sleep(3000);
						}
						result.stream().forEach(t -> total.addTransactions(t));
						return result;
					}
				}
			}
		}

		// PREVIOUS PERIOD TRANSACTIONS
		List<WebElement> previousPeriodRows;
		// lets see how it will go... not many accounts reach that point
		// for some accounts previous period transactions are not considered
		// (i.e. WF)
		if (accountNavigationDetails == null || accountNavigationDetails.getPeriodSwitchLocator() == null)
			return new ArrayList<Transaction>();
		else {
			// AmEx case: before select prev period a button should be clicked
			if (accountNavigationDetails.getPeriodSwitchSupLocator() != null) {
				WebElement periods = webDriver.findElement(accountNavigationDetails.getPeriodSwitchSupLocator());
				if (periods != null)
					periods.click();
				else
					return new ArrayList<Transaction>();
			}

			WebElement period = webDriver.findElement(accountNavigationDetails.getPeriodSwitchLocator());
			if (period != null) {
				if ("click".equals(accountNavigationDetails.getActionToSwitchPeriod()))
					period.click();
				else
					new Select(period).selectByIndex(1);
			} else
				return new ArrayList<Transaction>();

			// Wait for previous transactions table to be loaded
			Util.sleep(3000);
			if (webDriver.lookupElement(accountTransactionDetails.getTransTableLocator()) == null)
				previousPeriodRows = webDriver.findElements(accountTransactionDetails.getTransTableSupLocator());
			else
				previousPeriodRows = webDriver.findElements(accountTransactionDetails.getTransTableLocator());
			logger.info("Rows in the previous period table: {}", previousPeriodRows.size());
			for (WebElement row : previousPeriodRows) {
				// logger.info("Row in the previous period table: {}",
				// row.getText());
				if (Util.isPending(row.getText()))
					continue;
				double amount = -Util.convertStringAmountToDouble(row.findElement(byAmount).getText());
				Date date = Util.convertStringToDateByType(row.findElement(byDate).getText().trim(), dateFormat);
				String description = row.findElement(byDescription).getText().trim().replace("\n", "-");
				List<Transaction> matchTransactions = prevTransactions.stream()
						.filter(t -> t.getDate().equals(date) && t.getAmount() == amount).collect(Collectors.toList());
				if (matchTransactions.isEmpty()) {
					// trying to get Category
					String categoryStr = null;
					if (byCategoryNav != null) {
						row.findElement(byCategoryNav).click();
						Util.sleep(2000);
						categoryStr = row.findElement(byCategory).getText();
					}
					result.add(new Transaction(account, total, date, description, amount, categoryStr, null));
					difference = Util.roundDouble(difference - amount);
					logger.info("Amount: {}, diff: {}", amount, difference);
					if (difference == 0.0) {
						if (accountNavigationDetails.getAllAccountsLinkLocator() != null) {
							WebElement accounts = webDriver
									.findElement(accountNavigationDetails.getAllAccountsLinkLocator());
							if (accounts != null)
								accounts.click();
							Util.sleep(3000);
						}
						result.stream().forEach(t -> total.addTransactions(t));
						return result;
					}
				}
			}
		}

		if (accountNavigationDetails.getAllAccountsLinkLocator() != null) {
			WebElement accounts = webDriver.findElement(accountNavigationDetails.getAllAccountsLinkLocator());
			if (accounts != null)
				accounts.click();
			Util.sleep(3000);
		}
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
