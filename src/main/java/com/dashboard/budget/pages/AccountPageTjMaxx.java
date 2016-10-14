package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageTjMaxx extends AccountPage {

	private By btnLoginConfirmation = By.cssSelector("a.RegisterNextBtnleft > span");

	public AccountPageTjMaxx(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	@Override
	public boolean login() {
		WebElement username = webDriver.findElement(fldUsername);
		if (username == null)
			return false;
		username.sendKeys(accountLoginDetails.getUsernameValue());
		WebElement login = webDriver.findElement(btnLogin);
		if (login == null)
			return false;	
		login.click();

		// Account blocked
		if(Util.isProblemWithLogin(webDriver)){
			logger.error("There is a problem with login: {}", account.getName());
			return false;
		}
		// secret question
		if(Util.isSecretQuestionShown(webDriver))
			if (!super.answerSecretQuestion())
				return false;

		WebElement password = webDriver.findElement(fldPassword);
		if (password == null)
			return false;		
		password.sendKeys(accountLoginDetails.getPasswordValue());
		webDriver.findElement(btnLoginConfirmation).click();
		return true;
	}

	@Override
	public Double getTotal() {
		int code = account.getId();
		// Andrei
		if (code==207) {
			amount = webDriver.findElement(By.id("currentBalance"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Irina
		} else if (code==208) {
			WebElement next = webDriver.findElement(By.cssSelector("a.RegisterNextBtnleft.wal_dualno > span"));
			if (next != null)
				next.click();
			else
				return null;
			amount = webDriver.findElement(By.id("currentBalance"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
		}

		return null;
	}
}
