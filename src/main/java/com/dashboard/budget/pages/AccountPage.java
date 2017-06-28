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
import com.dashboard.budget.DAO.SecretQuestion;
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
	protected By btnPostLogout;
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
		btnPostLogout = accountLoginDetails.getLogoutPostLocator();

		this.webDriver = new UberWebDriver();
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);
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

	public synchronized boolean login() {
		if (Util.checkIfSiteDown(webDriver))
			return false;

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

	public List<Transaction> getTransactions(Total total, List<Transaction> prevTransactions) throws Exception {
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
		} else if (accountNavigationDetails != null && accountNavigationDetails.getTransactionsPageUrl() != null) {
			webDriver.get(accountNavigationDetails.getTransactionsPageUrl());
			webDriver.switchTo().defaultContent();
		}

		Double difference = total.getDifference();
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
		By byCategorySup = (accountTransactionDetails.getTransCategorySupLocator() == null) ? null
				: By.xpath(accountTransactionDetails.getTransCategorySupLocator());
		Integer dateFormat = accountTransactionDetails.getTransDateFormat();

		// CURRENT PERIOD TRANSACTIONS
		List<WebElement> currentPeriodRows;
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
				// special case for Chase - transactions within one day are
				// grouped
				// so only 1st shown transaction has date in the row
				Date date;
				if (Util.isPending(row.findElement(byDate).getText()))
					continue;
				date = Util.convertStringToDateByType(row.findElement(byDate).getText(), dateFormat);

				double amount;
				WebElement weByAmount = row.findElement(byAmount);
				WebElement weByAmountSup = (byAmountSup == null) ? null : row.findElement(byAmountSup);
				// Amount consideration got complicated due PayPal
				if (byAmountSup == null)
					if (account.getIsMyProtfolio())
						amount = Util.convertStringAmountToDouble(weByAmount.getText());
					else
						amount = -Util.convertStringAmountToDouble(weByAmount.getText());
				else {
					if ("negative".equals(weByAmountSup.getText()))
						amount = -Util.convertStringAmountToDouble(weByAmount.getText());
					else {
						if (weByAmount == null)
							amount = -Util.convertStringAmountToDouble(weByAmountSup.getText());
						else
							amount = ("".equals(weByAmountSup.getText()))
									? -Util.convertStringAmountToDouble(weByAmount.getText())
									: Util.convertStringAmountToDouble(weByAmountSup.getText());
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
						.filter(t -> t.getDate().equals(date) && t.getAmount() == amount && t.getAccount() == account)
						.collect(Collectors.toList());
				if (matchTransactions.isEmpty()) {
					// trying to get Category
					String categoryStr = null;
					if (byCategoryNav != null) {
						WebElement weCategoryNav = webDriver.findElementInRow(row, byCategoryNav);
						if (weCategoryNav == null)
							logger.error("Cannot find category navigation locatore");
						else {
							webDriver.clickElementWithAction(weCategoryNav);
							if (accountTransactionDetails.getTransCategoryLocator().startsWith(".")) {
								WebElement weCategory = webDriver.findElementInRow(row, byCategory);
								if (weCategory != null)
									categoryStr = weCategory.getText();
								else
									categoryStr = row.findElement(byCategorySup).getText();
							} else // absolute path
								categoryStr = webDriver.findElement(byCategory).getText();
							// weCategoryNav.click();
						}
					} else if (byCategory != null) {
						if (accountTransactionDetails.getTransCategoryLocator().startsWith(".")) {
							WebElement weCategory = webDriver.findElementInRow(row, byCategory);
							if (weCategory != null)
								categoryStr = weCategory.getText();
							else
								categoryStr = row.findElement(byCategorySup).getText();
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
						else if (accountNavigationDetails != null
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

			//
			if (accountNavigationDetails.getPeriodSwitchPostLocator() != null) {
				WebElement periods = webDriver.findElement(accountNavigationDetails.getPeriodSwitchPostLocator());
				if (periods != null)
					periods.click();
				else
					return new ArrayList<Transaction>();
			}

			// Wait for previous transactions table to be loaded
			Util.sleep(5000);
			previousPeriodRows = webDriver.findElements(accountTransactionDetails.getTransTableLocator());
			if (previousPeriodRows == null)
				previousPeriodRows = webDriver.findElements(accountTransactionDetails.getTransTableSupLocator());

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
					if (byCategoryNav != null && row.findElements(byCategoryNav).size() > 0) {
						row.findElement(byCategoryNav).click();
						Util.sleep(2000);
						if (accountTransactionDetails.getTransCategoryLocator().startsWith("."))
							categoryStr = row.findElement(byCategory).getText();
						else // absolute path
							categoryStr = webDriver.findElement(byCategory).getText();
					} else
						categoryStr = "";
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
						else if (accountNavigationDetails.getAllAccountsLinkLocator() != null) {
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

	protected boolean answerSecretQuestion() {
		By secretQuestionLocator = account.getAccountDetailsNavigation().getSecretQuestionLocator();
		By secretAnswerLocator = account.getAccountDetailsNavigation().getSecretAnswerLocator();
		By secretSubmitLocator = account.getAccountDetailsNavigation().getSecretSubmitLocator();
		By secretSubmitSupLocator = account.getAccountDetailsNavigation().getSecretSubmitSupLocator();

		WebElement question = webDriver.lookupElement(secretQuestionLocator);
		if (question == null)
			return false;
		else {
			String secretQuestion = question.getText().trim();
			// first trying to find answer by account
			SecretQuestion secretAnswer = dataHandler.getSecretQuestions().stream()
					.filter(sq -> sq.getAccount() == account && sq.getQuestion().equals(secretQuestion)).findFirst()
					.orElse(null);
			// if answer not found by account trying to find answer by bank
			if (secretAnswer == null)
				secretAnswer = dataHandler.getSecretQuestions().stream()
						.filter(sq -> sq.getBank() == account.getBank() && sq.getQuestion().equals(secretQuestion))
						.findFirst().orElse(null);
			if (secretAnswer == null)
				logger.error("Cannot find answer for question {}", secretQuestion);
			else {
				WebElement answer = webDriver.lookupElement(secretAnswerLocator);
				answer.clear();
				answer.sendKeys(secretAnswer.getAnswer());
			}

			if (secretSubmitSupLocator != null) {
				WebElement submitSup = webDriver.findElement(secretSubmitSupLocator);
				if (submitSup != null)
					submitSup.click();
			}

			WebElement submit = webDriver.findElement(secretSubmitLocator);
			if (submit != null)
				submit.click();

			/*
			 * WebElement cont = webDriver.findElement(By.cssSelector(
			 * "#verify-cq-submit > span")); if (cont != null) cont.click();
			 * else return false;
			 */

			return true;
		}

	}

	public UberWebDriver getWebDriver() {
		return webDriver;
	}

	public void quit() {
		WebElement logout = webDriver.findElement(btnLogout);
		if (logout != null) {
			logout.click();

			// For Macys: there is log out confirmation window
			if (btnPostLogout != null) {
				WebElement postLogout = webDriver.findElement(btnPostLogout);
				if (postLogout != null)
					postLogout.click();
			}

			logger.info("Account page {} was closed", account.getName());
		} else
			logger.error("Account page {} was not closed properly", account.getName());
		webDriver.quit();
	}
}
