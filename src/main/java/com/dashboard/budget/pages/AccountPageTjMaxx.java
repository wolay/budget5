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

public class AccountPageTjMaxx extends AccountPage {

	private By btnLoginConfirmation = By.cssSelector("a.RegisterNextBtnleft > span");

	public AccountPageTjMaxx(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public boolean login() {
		fldUsername = By.name(accountDetails.getUsernameLocator());
		fldPassword = By.name(accountDetails.getPasswordLocator());
		btnLogin = By.cssSelector(accountDetails.getLoginLocator());

		webDriver.findElement(fldUsername).sendKeys(accountDetails.getUsernameValue());
		webDriver.findElement(btnLogin).click();

		// here could be secret question
		if(webDriver.getPageSource().contains("Challenge Question")){
			logger.error("{}: Here is a secret question", account.getName());
			return false;
			/*String question = webDriver.findElement(By.cssSelector("h4.ng-binding")).getText();
			if (question.equals("What was the name of your first pet?")) {
				webDriver.findElement(By.id("answer1")).clear();
				webDriver.findElement(By.id("answer1")).sendKeys("Jessy");
			} else if (question.equals("What is your mother's middle name?")) {
				webDriver.findElement(By.id("answer1")).clear();
				webDriver.findElement(By.id("answer1")).sendKeys("Nikolaevna");
			} else if (question.equals("What city were you in on New Year's Eve, 1999?")) {
				webDriver.findElement(By.id("answer1")).clear();
				webDriver.findElement(By.id("answer1")).sendKeys("Krasnodar");
			}
			waitAndClick(webDriver, wait, By.xpath("//button"));*/
		}

		webDriver.findElement(fldPassword).sendKeys(accountDetails.getPasswordValue());
		webDriver.findElement(btnLoginConfirmation).click();
		return true;
	}

	@Override
	public Double getTotal() {
		String code = account.getCode();
		// Andrei
		if (code.equals("207")) {
			amount = webDriver.findElement(By.id("currentBalance"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Irina
		} else if (code.equals("208")) {
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

	@Override
	public List<Transaction> getTransactions(Double diff, List<Transaction> prevTransactions) {
		// not implemented yet
		return new ArrayList<Transaction>();
	}

}
