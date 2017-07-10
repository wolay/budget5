package com.dashboard.budget.pages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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
import com.dashboard.budget.DAO.Button;
import com.dashboard.budget.DAO.Field;
import com.dashboard.budget.DAO.PageElementNotFoundException;
import com.dashboard.budget.DAO.Switch;
import com.dashboard.budget.DAO.TableRow;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;

public abstract class AccountPage implements Config, Page {

	protected static Logger logger = LoggerFactory.getLogger(AccountPage.class);

	private AccountPageSecretQuestions pageQuestions;

	protected Account account;
	protected AccountDetailsLogin accountLoginDetails;
	protected AccountDetailsNavigation accountNavigationDetails;
	protected AccountDetailsTotal accountTotalDetails;
	protected AccountDetailsTransaction accountTransactionDetails;
	protected DataHandler dataHandler;

	protected UberWebDriver webDriver;
	protected WebDriverWait wait;

	// Login
	protected Field fldUsername;
	protected String valUsername;
	protected Field fldPassword;
	protected String valPassword;
	protected Button btnLogin;
	protected Button btnLogout;
	protected Button btnPostLogout;

	// Total
	protected Field fldBalance;

	// Transactions
	protected Button btnTransactionsPage;
	protected TableRow trTransaction;
	protected Switch swtPeriod;

	public AccountPage(Account account, DataHandler dataHandler) {
		this.account = account;
		this.dataHandler = dataHandler;
		this.accountLoginDetails = account.getAccountDetailsLogin();
		this.accountNavigationDetails = account.getAccountDetailsNavigation();
		this.accountTotalDetails = account.getAccountDetailsTotal();
		this.accountTransactionDetails = account.getAccountDetailsTransaction();

		this.webDriver = new UberWebDriver();
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);

		pageQuestions = new AccountPageSecretQuestions(account, webDriver, dataHandler);

		// Login
		fldUsername = new Field("username", accountLoginDetails.getUsernameLocator(), getWebdriver());
		valUsername = accountLoginDetails.getUsernameValue();
		fldPassword = new Field("password", accountLoginDetails.getPasswordLocator(), getWebdriver());
		valPassword = accountLoginDetails.getPasswordValue();
		btnLogin = new Button("login", accountLoginDetails.getLoginLocator(), getWebdriver());
		btnLogout = new Button("logout", accountLoginDetails.getLogoutLocator(), getWebdriver());
		btnPostLogout = new Button("post logout", accountLoginDetails.getLogoutPostLocator(), getWebdriver());

		// Total
		if (accountTotalDetails != null) {
			fldBalance = new Field("balance", accountTotalDetails.getBalanceLocator(), getWebdriver());
		}

		// Transactions
		if (accountNavigationDetails != null) {
			btnTransactionsPage = new Button("transactions page link", accountNavigationDetails.getDetailsLinkLocator(),
					getWebdriver());
			swtPeriod = new Switch("period switch", accountNavigationDetails.getPeriodSwitchLocator(),
					accountNavigationDetails.getActionToSwitchPeriod(), getWebdriver());

		}

	}

	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account account) {
		logger.info("Setting account.. {}", account.getName());
		this.account = account;
		this.accountLoginDetails = account.getAccountDetailsLogin();
		this.accountNavigationDetails = account.getAccountDetailsNavigation();
		this.accountTotalDetails = account.getAccountDetailsTotal();
		this.accountTransactionDetails = account.getAccountDetailsTransaction();
	}

	public void gotoHomePage() {
		webDriver.get(account.getUrl());
	}

	public abstract boolean login();

	public abstract Double getTotal();

	public List<Transaction> getTransactions(Total total, List<Transaction> prevTransactions) throws Exception {
		// if(1==1)
		// return null;
		// check if getting transactions is enabled for account
		if (accountTransactionDetails == null) {
			logger.info("Getting transactions is not enabled for account '{}'", total.getAccount().getName());
			return new ArrayList<Transaction>();
		}

		List<Transaction> result = new ArrayList<Transaction>();

		if (account.getIsMyProtfolio()) {
			if (!webDriver.getWebDriver().getTitle().contains("Transaction")) {
				Actions action = new Actions(webDriver.getWebDriver());
				WebElement we = webDriver.findElement(By.name("onh_tools_and_investing"));
				if (we != null)
					action.moveToElement(we).build().perform();
				WebElement submit1 = webDriver.findElement(By.linkText("Transactions"));
				if (submit1 != null)
					submit1.click();
			}

			// select account from dropdown list
			WebElement accountsLink = webDriver.findElement(By.id("dropdown_itemAccountId"));
			if (accountsLink != null)
				accountsLink.click();

			List<WebElement> accounts = webDriver.findElements(By.className(" groupItem"));
			WebElement weAccount = accounts.stream()
					.filter(a -> !a.getText().equals("") && a.getText().contains(account.getMyPortfolioId()))
					.findFirst().orElse(null);
			if (weAccount != null)
				webDriver.clickElementWithAction(weAccount);

			// select period for 1 month
			WebElement periodList = webDriver.findElement(By.id("dropdown_dateRangeId"));
			if (periodList != null) {
				if (!periodList.getText().equals("1 month")) {
					periodList.click();
					WebElement periodItem = webDriver.findElement(By.id("custom_multi_select_2_dateRangeId"));
					if (periodItem != null)
						periodItem.click();
				}
			}

		} else if (accountNavigationDetails != null && accountNavigationDetails.getDetailsLinkLocator() != null) {
			WebElement weDetails = webDriver.lookupElement(accountNavigationDetails.getDetailsLinkLocator());
			if (weDetails != null) {
				webDriver.waitToBeClickable(accountNavigationDetails.getDetailsLinkLocator());
				try {
					weDetails.click();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				logger.error("Details (transaction) locator '{}' not found on the page",
						accountNavigationDetails.getDetailsLinkLocator());
				return result;
			}
		}

		Double difference = total.getDifference();
		By byDate = By.xpath(accountTransactionDetails.getTransDateLocator());
		By byAmount = By.xpath(accountTransactionDetails.getTransAmountLocator());
		By byDescription = By.xpath(accountTransactionDetails.getTransDescriptionLocator());
		By byCategory = (accountTransactionDetails.getTransCategoryLocator() == null) ? null
				: By.xpath(accountTransactionDetails.getTransCategoryLocator());
		Integer dateFormat = accountTransactionDetails.getTransDateFormat();

		// CURRENT PERIOD TRANSACTIONS
		List<WebElement> currentPeriodRows = webDriver.findElements(accountTransactionDetails.getTransTableLocator());
		if (currentPeriodRows == null)
			logger.info("No rows found in the current period table");
		else {
			logger.info("Rows in the current period table: {}", currentPeriodRows.size());
			for (WebElement row : currentPeriodRows) {
				logger.info("Row in the current period table: {}", row.getText());
				if (Util.isPending(row.getText()))
					continue;
				// special case for Chase - transactions within one day are
				// grouped
				// so only 1st shown transaction has date in the row
				Date date;
				if (Util.isPending(row.findElement(byDate).getText()))
					continue;
				date = Util.convertStringToDateByType(row.findElement(byDate).getText(), dateFormat);

				double amount;
				WebElement weByAmount = row.findElement(byAmount);
				// Amount consideration got complicated due PayPal
				if (account.getIsMyProtfolio())
					amount = Util.convertStringAmountToDouble(weByAmount.getText());
				else
					amount = -Util.convertStringAmountToDouble(weByAmount.getText());

				String description = row.findElement(byDescription).getText().trim().replace("\n", "-");

				List<Transaction> matchTransactions = prevTransactions.stream()
						.filter(t -> t.getDate().equals(date) && t.getAmount() == amount && t.getAccount() == account)
						.collect(Collectors.toList());
				if (matchTransactions.isEmpty()) {
					// trying to get Category
					String categoryStr = null;
					if (byCategory != null) {
						if (accountTransactionDetails.getTransCategoryLocator().startsWith(".")) {
							WebElement weCategory = webDriver.findElementInRow(row, byCategory);
							if (weCategory != null)
								categoryStr = weCategory.getText();
						} else // absolute path
							categoryStr = webDriver.findElement(byCategory).getText();
					} else {
						categoryStr = "";
					}
					Transaction newTransaction = new Transaction(account, total, date, description, amount, categoryStr,
							null);
					logger.info("Category in current row: {}", categoryStr);
					dataHandler.recognizeCategoryInTransaction(newTransaction);
					logger.info("Recognized category: {}", newTransaction.getCategory());
					result.add(newTransaction);
					difference = Util.roundDouble(difference - amount);
					logger.info("Amount: {}, diff: {}", amount, difference);
					if (difference == 0.0) {
						// return to the main page (from where Transactions will
						// be opened)
						if (account.getIsMyProtfolio())
							webDriver.getWebDriver().navigate().back();
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
		if (accountNavigationDetails == null || accountNavigationDetails.getPeriodSwitchLocator() == null) {
			if (account.getIsMyProtfolio())
				webDriver.getWebDriver().navigate().back();
			return new ArrayList<Transaction>();
		} else {
			// AmEx case: before select prev period a button should be clicked
			if (accountNavigationDetails.getPeriodSwitchPreLocator() != null) {
				WebElement periods = webDriver.findElement(accountNavigationDetails.getPeriodSwitchPreLocator());
				if (periods != null)
					periods.click();
				else
					return new ArrayList<Transaction>();
			}

			// Util.sleep(3000);
			WebElement period = webDriver.findElement(accountNavigationDetails.getPeriodSwitchLocator());
			if (period != null) {
				if ("click".equals(accountNavigationDetails.getActionToSwitchPeriod()))
					period.click();
				else
					new Select(period).selectByIndex(1);
			} else
				return new ArrayList<Transaction>();

			// Wait for previous transactions table to be loaded
			Util.sleep(5000);
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
				List<Transaction> matchPrevTransactions = prevTransactions.stream()
						.filter(t -> t.getDate().equals(date) && t.getAmount() == amount).collect(Collectors.toList());
				List<Transaction> matchCurrentTransactions = result.stream()
						.filter(t -> t.getDate().equals(date) && t.getAmount() == amount).collect(Collectors.toList());
				if (matchPrevTransactions.isEmpty() && matchCurrentTransactions.isEmpty()) {
					// trying to get Category
					String categoryStr = null;
					if (byCategory != null) {
						if (accountTransactionDetails.getTransCategoryLocator().startsWith(".")) {
							WebElement weCategory = webDriver.findElementInRow(row, byCategory);
							if (weCategory != null)
								categoryStr = weCategory.getText();
						} else // absolute path
							categoryStr = webDriver.findElement(byCategory).getText();
					} else {
						categoryStr = "";
					}
					Transaction newTransaction = new Transaction(account, total, date, description, amount, categoryStr,
							null);
					logger.info("Category in current row: {}", categoryStr);
					dataHandler.recognizeCategoryInTransaction(newTransaction);
					logger.info("Recognized category: {}", newTransaction.getCategory());
					result.add(newTransaction);
					difference = Util.roundDouble(difference - amount);
					logger.info("Amount: {}, diff: {}", amount, difference);
					if (difference == 0.0) {
						// return to the main page (from where Transactions will
						// be opened)
						if (account.getIsMyProtfolio())
							webDriver.getWebDriver().navigate().back();
						result.stream().forEach(t -> total.addTransactions(t));
						return result;
					}
				}
			}
		}

		if (difference == 0.0)
			return result;
		else
			return new ArrayList<Transaction>();
	}

	protected boolean isTransactionExist(List<Transaction> prevTransactions, Date trDate, Double trAmount) {
		return prevTransactions.stream()
				.filter(t -> t.getDate().equals(trDate) && t.getAmount() == trAmount && t.getAccount() == account)
				.count() > 0;
	}

	public int getScore() throws PageElementNotFoundException {
		return 0;
	}

	protected boolean answerSecretQuestion() {
		return pageQuestions.answerSecretQuestion();
	}

	@Override
	public UberWebDriver getWebdriver() {
		return webDriver;
	}

	public abstract void quit();
}
