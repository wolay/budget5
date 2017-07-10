package com.dashboard.budget.pages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.UberWebDriver;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.AccountDetailsNavigation;
import com.dashboard.budget.DAO.Button;
import com.dashboard.budget.DAO.Field;
import com.dashboard.budget.DAO.PageElementNotFoundException;
import com.dashboard.budget.DAO.SecretQuestion;

public class AccountPageSecretQuestions implements Page {

	protected static Logger logger = LoggerFactory.getLogger(AccountPageSecretQuestions.class);

	protected Account account;
	protected UberWebDriver webDriver;
	protected DataHandler dataHandler;

	private Field fldSecretQuestion;
	private Field fldSecretAnswer;
	private Button btnSecretSubmit;
	private Button btnSecretPreSubmit;

	public AccountPageSecretQuestions(Account account, UberWebDriver webDriver, DataHandler dataHandler) {
		AccountDetailsNavigation accountDetailsNavigation = account.getAccountDetailsNavigation();

		this.account = account;
		this.webDriver = webDriver;
		this.dataHandler = dataHandler;

		if(accountDetailsNavigation!=null){
			fldSecretQuestion = new Field("secret question", accountDetailsNavigation.getSecretQuestionLocator(), getWebdriver());
			fldSecretAnswer = new Field("secret answer", accountDetailsNavigation.getSecretAnswerLocator(), getWebdriver());
			btnSecretSubmit = new Button("submit secret answer", accountDetailsNavigation.getSecretSubmitLocator(),
					getWebdriver());
			btnSecretPreSubmit = new Button("pre submit secret answer",
					accountDetailsNavigation.getSecretSubmitSupLocator(), getWebdriver());
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

	@Override
	public UberWebDriver getWebdriver() {
		// TODO Auto-generated method stub
		return webDriver;
	}

	
}
