package com.dashboard.budget.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.UberWebDriver;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.AccountDetailsLogin;
import com.dashboard.budget.DAO.AccountDetailsNavigation;
import com.dashboard.budget.DAO.Button;
import com.dashboard.budget.DAO.Field;
import com.dashboard.budget.DAO.PageElementNotFoundException;
import com.dashboard.budget.DAO.SecretQuestion;

public class AccountPageLogin implements Page {

	protected static Logger logger = LoggerFactory.getLogger(AccountPageLogin.class);

	protected Account account;
	protected UberWebDriver webDriver;
	protected DataHandler dataHandler;

	// General set
	private Field fldUsername;
	private String valUsername;
	private Field fldPassword;
	private String valPassword;
	private Button btnLogin;
	private Button btnLogout;
	private Button btnPostLogout;

	// Advanced set (TJ Maxx, JC Penney, Credit Karma)
	private Button btnUserCheck;
	private Boolean advancedFlow;
	private Field fldSecretQuestion;
	private Field fldSecretAnswer;
	private Button btnSecretSubmit;
	private Button btnSecretPreSubmit;

	public AccountPageLogin(Account account, UberWebDriver webDriver, DataHandler dataHandler) {
		AccountDetailsLogin accountLoginDetails = account.getAccountDetailsLogin();
		AccountDetailsNavigation accountDetailsNavigation = account.getAccountDetailsNavigation();

		this.account = account;
		this.webDriver = webDriver;
		this.dataHandler = dataHandler;

		// General
		fldUsername = new Field("username", accountLoginDetails.getUsernameLocator(), this);
		valUsername = accountLoginDetails.getUsernameValue();
		fldPassword = new Field("password", accountLoginDetails.getPasswordLocator(), this);
		valPassword = accountLoginDetails.getPasswordValue();
		btnLogin = new Button("login", accountLoginDetails.getLoginLocator(), this);
		btnLogout = new Button("logout", accountLoginDetails.getLogoutLocator(), this);
		btnPostLogout = new Button("post logout", accountLoginDetails.getLogoutPostLocator(), this);

		// Advanced
		btnUserCheck = new Button("login", accountLoginDetails.getUserCheckLocator(), this);
		advancedFlow = accountLoginDetails.isAdvancedFlow();
		if (advancedFlow && accountDetailsNavigation != null) {
			fldSecretQuestion = new Field("secret question", accountDetailsNavigation.getSecretQuestionLocator(), this);
			fldSecretAnswer = new Field("secret answer", accountDetailsNavigation.getSecretAnswerLocator(), this);
			btnSecretSubmit = new Button("submit secret answer", accountDetailsNavigation.getSecretSubmitLocator(),
					this);
			btnSecretPreSubmit = new Button("pre submit secret answer",
					accountDetailsNavigation.getSecretSubmitSupLocator(), this);
		}
	}

	public synchronized boolean login() {
		if (Util.checkIfSiteDown(webDriver))
			return false;

		Util.sleep(3000); // for Best Buy card

		try {
			if (!advancedFlow) {
				fldUsername.setText(valUsername);
				fldPassword.setText(valPassword);
				btnLogin.click();
			} else {
				fldUsername.setText(valUsername);
				btnUserCheck.clickIfAvailable();
				// Account blocked
				if (Util.isProblemWithLogin(webDriver)) {
					logger.error("There is a problem with login: {}", account.getName());
					return false;
				}
				// Secret question
				if (Util.isSecretQuestionShown(webDriver))
					if (!answerSecretQuestion())
						return false;
				fldPassword.setText(valPassword);
				btnLogin.click();
			}
			return true;
		} catch (PageElementNotFoundException e) {
			return false;
		}
	}

	protected boolean answerSecretQuestion() {

		try {
			String secretQuestion = fldSecretQuestion.getText();
			if (secretQuestion == null)
				return true; // no secret question shown on the page

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
			else
				fldSecretAnswer.setText(secretAnswer.getAnswer());

			btnSecretPreSubmit.clickIfAvailable();
			btnSecretSubmit.click();
		} catch (PageElementNotFoundException e) {
			return false;
		}

		return true;
	}

	public boolean quit() {

		try {
			btnLogout.click();
			btnPostLogout.click();
		} catch (PageElementNotFoundException e) {
			logger.error("Account page {} was not closed properly", account.getName());
			return true;
		}

		webDriver.quit();
		logger.info("Account page {} was closed", account.getName());
		return true;
	}

	@Override
	public UberWebDriver getWebdriver() {
		// TODO Auto-generated method stub
		return webDriver;
	}

}
