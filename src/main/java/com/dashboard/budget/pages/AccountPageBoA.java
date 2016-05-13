package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageBoA extends AccountPage {

	public AccountPageBoA(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public Double getTotal() {
		// secret question
		WebElement question = webDriver.findElement(By.cssSelector("label"));
		if (question != null) {
			if (question.getText().equals("What was the name of your first pet?")) {
				webDriver.findElement(By.id("tlpvt-challenge-answer")).clear();
				webDriver.findElement(By.id("tlpvt-challenge-answer")).sendKeys("Jessy");
			} else if (question.getText().equals("What is your mother's middle name?")) {
				webDriver.findElement(By.id("tlpvt-challenge-answer")).clear();
				webDriver.findElement(By.id("tlpvt-challenge-answer")).sendKeys("Nikolaevna");
			} else if (question.getText().equals("What city were you in on New Year's Eve, 1999?")) {
				webDriver.findElement(By.id("tlpvt-challenge-answer")).clear();
				webDriver.findElement(By.id("tlpvt-challenge-answer")).sendKeys("Krasnodar");
			}
			WebElement submit = webDriver.findElement(By.id("yes-recognize"));
			if (submit != null)
				submit.click();
			
			WebElement cont = webDriver.findElement(By.cssSelector("#verify-cq-submit > span"));
			if (cont != null)
				cont.click();
			else
				return null;
		}

		// reading balance
		String locator = "span.balanceValue.TL_NPI_L1";
		amount = webDriver.findElement(By.cssSelector(locator));
		return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
	}

}
