package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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
		
		// secret question
		WebElement securityLabel = webDriver.findElement(By.xpath("//*[contains(text(),'Challenge Question')]"));
		if (securityLabel != null) {
			String question = webDriver.findElement(By.xpath("//tr[5]/td[2]")).getText().trim();
			if (question.equals("In what city were you married? (Enter full name of city)")) {
				webDriver.findElement(By.id("challengeAnswer1")).clear();
				webDriver.findElement(By.id("challengeAnswer1")).sendKeys("Moscow");
			} else if (question.equals("What city were you in on New Year's Eve, 1999?")) {
				webDriver.findElement(By.id("challengeAnswer1")).clear();
				webDriver.findElement(By.id("challengeAnswer1")).sendKeys("Saransk");
			} else if (question.equals("What was the name of your first pet?")) {
				webDriver.findElement(By.id("challengeAnswer1")).clear();
				webDriver.findElement(By.id("challengeAnswer1")).sendKeys("Murzik");
			}
			WebElement submit = webDriver.findElement(By.id("submitChallengeAnswers"));
			if (submit != null)
				submit.click();
			else
				return false;
		}		
		
		webDriver.findElement(fldPassword).sendKeys(accountDetails.getPasswordValue());		
		webDriver.findElement(btnLogin).click();
		return true;
		
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
