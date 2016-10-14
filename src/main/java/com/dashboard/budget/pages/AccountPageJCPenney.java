package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageJCPenney extends AccountPage {

	public AccountPageJCPenney(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}
	
	@Override
	public boolean login() {
		fldUsername = accountLoginDetails.getUsernameLocator();
		fldPassword = accountLoginDetails.getPasswordLocator();
		
		webDriver.findElement(fldUsername).sendKeys(accountLoginDetails.getUsernameValue());
		webDriver.findElement(By.name("button")).click();
		
		// Account blocked
		if(Util.isProblemWithLogin(webDriver)){
			logger.error("There is a problem with login: {}", account.getName());
			return false;
		}
		// secret question
		if(Util.isSecretQuestionShown(webDriver))
			if (!super.answerSecretQuestion())
				return false;	
		
		webDriver.findElement(fldPassword).sendKeys(accountLoginDetails.getPasswordValue());		
		webDriver.findElement(btnLogin).click();
		return true;
		
	}

	@Override
	public Double getTotal() {
		amount = webDriver.findElement(By.id("currentBalance"));
		return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
	}
}
