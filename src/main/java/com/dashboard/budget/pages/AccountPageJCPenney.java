package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Transaction;

public class AccountPageJCPenney extends AccountPage {

	public AccountPageJCPenney(Account account, DataHandler dataHandler) {
		super(account);
	}
	
	@Override
	public boolean login() {
		fldUsername = By.name(accountDetails.getUsernameLocator());
		fldPassword = By.name(accountDetails.getPasswordLocator());
		
		webDriver.findElement(fldUsername).sendKeys(accountDetails.getUsernameValue());
		webDriver.findElement(By.name("button")).click();
		webDriver.findElement(fldPassword).sendKeys(accountDetails.getPasswordValue());		
		webDriver.findElement(btnLogin).click();
		return true;
		
		/*waitForElement(wait, By.name(accountDetails.getUsernameLocator()));
		fldUsername = webDriver.findElement(By.name(accountDetails.getUsernameLocator()));
		username = accountDetails.getUsernameValue();
		fldUsername.clear();
		fldUsername.sendKeys(username);
		
		webDriver.findElement(By.name("button")).click();
		
		fldPassword = webDriver.findElement(By.name(accountDetails.getPasswordLocator()));
		password = accountDetails.getPasswordValue();
		fldPassword.clear();
		fldPassword.sendKeys(password);
		
		btnLogin = webDriver.findElement(By.id(accountDetails.getLoginLocator()));
		btnLogin.click();	*/	
	}

	@Override
	public Double getTotal() {
		amount = webDriver.findElement(By.id("currentBalance"));
		return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
	}
	
	@Override
	public List<Transaction> getTransactions(Double diff, List<Transaction> prevTransactions) {
		// not implemented yet
		return new ArrayList<Transaction>();
	}

}
