package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageNordstorm extends AccountPage {

	public AccountPageNordstorm(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public Double getTotal() {
		// secret question
		String question = webDriver.findElement(By.cssSelector("h4.ng-binding")).getText();
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
		WebElement submit = webDriver.findElement(By.xpath("//button"));
		if (submit != null)
			submit.click();
		else
			return null;

		// reading balance
		String locator = "//ul[@class='accountslist ng-scope']/li[3]//strong";
		amount = webDriver.findElement(By.xpath(locator));
		return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
	}
}
