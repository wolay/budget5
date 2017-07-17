package com.dashboard.budget.pages;

import java.util.Date;
import java.util.List;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.Config;
import com.dashboard.budget.DataHandler;
import com.dashboard.budget.UberWebDriver;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.AccountDetailsLogin;
import com.dashboard.budget.DAO.AccountDetailsNavigation;
import com.dashboard.budget.DAO.AccountDetailsTotal;
import com.dashboard.budget.DAO.AccountDetailsTransaction;
import com.dashboard.budget.DAO.DataRetrievalStatus;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;
import com.dashboard.budget.UI.Button;
import com.dashboard.budget.UI.Field;
import com.dashboard.budget.UI.PageElementNotFoundException;
import com.dashboard.budget.UI.Switch;
import com.dashboard.budget.UI.TableRow;

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
		
		refreshLocators();
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
		
		refreshLocators();
	}
	
	public void refreshLocators(){		
		// Login
		fldUsername = new Field("username", accountLoginDetails.getUsernameLocator(), getWebdriver(), getWebdriver().getWebDriver());
		valUsername = accountLoginDetails.getUsernameValue();
		fldPassword = new Field("password", accountLoginDetails.getPasswordLocator(), getWebdriver(), getWebdriver().getWebDriver());
		valPassword = accountLoginDetails.getPasswordValue();
		btnLogin = new Button("login", accountLoginDetails.getLoginLocator(), getWebdriver(), getWebdriver().getWebDriver());
		btnLogout = new Button("logout", accountLoginDetails.getLogoutLocator(), getWebdriver(), getWebdriver().getWebDriver());
		btnPostLogout = new Button("post logout", accountLoginDetails.getLogoutPostLocator(), getWebdriver(), getWebdriver().getWebDriver());

		// Total
		if (accountTotalDetails != null) {
			fldBalance = new Field("balance", accountTotalDetails.getBalanceLocator(), getWebdriver(), getWebdriver().getWebDriver());
		}

		// Transactions
		if (accountNavigationDetails != null) {
			btnTransactionsPage = new Button("transactions page link", accountNavigationDetails.getDetailsLinkLocator(),
					getWebdriver(), getWebdriver().getWebDriver());
			swtPeriod = new Switch("period switch", accountNavigationDetails.getPeriodSwitchLocator(),
					accountNavigationDetails.getActionToSwitchPeriod(), getWebdriver(), getWebdriver().getWebDriver());
		}		
	}

	public void gotoHomePage() {
		webDriver.get(account.getUrl());
	}

	public abstract DataRetrievalStatus login();

	public abstract Double getTotal();

	public abstract List<Transaction> getTransactions(Total total, List<Transaction> prevTransactions) throws PageElementNotFoundException;

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
