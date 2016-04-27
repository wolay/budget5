package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Transaction;

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
			else
				return null;
		}

		// reading balance
		String locator = "span.balanceValue.TL_NPI_L1";
		amount = webDriver.findElement(By.cssSelector(locator));
		return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
	}

	@Override
	public List<Transaction> getTransactions(Double diff, List<Transaction> prevTransactions) {

		List<Transaction> result = new ArrayList<Transaction>();
		/*
		 * // Store the current window handle String winHandleBefore =
		 * webDriver.getWindowHandle(); // Switch to new window opened for
		 * (String winHandle : webDriver.getWindowHandles()) { if
		 * (!winHandle.equals(winHandleBefore)) {
		 * webDriver.switchTo().window(winHandle); break; } }
		 * 
		 * //webDriver.switchTo().defaultContent(); WebElement details =
		 * webDriver.findElement(By.linkText("Transactions")); if (details !=
		 * null) details.click(); else return result;
		 */

		webDriver.get("https://services1.capitalone.com/accounts/transactions");
		webDriver.switchTo().defaultContent();

		List<WebElement> rows = webDriver.findElements(By.xpath("//div[@id='postedTransactionTable']/div"));
		for (WebElement row : rows) {
			WebElement amountStr = row.findElement(By.xpath("./div/span/span"));
			if (amount == null)
				return new ArrayList<Transaction>();
			Double amount = -convertStringAmountToDouble(amountStr.getText());
			String description = row.findElement(By.xpath("./div[2]/div/span")).getText().trim();
			Date date = Util.convertStringToDateType1(row.findElement(By.xpath("./div/span")).getText());
			result.add(new Transaction(account.getCode(), date, description, amount, ""));
			diff = Util.roundDouble(diff - amount);
			if (diff == 0.0)
				return result;
		}

		// Switch back to original browser (first window)
		// webDriver.switchTo().window(winHandleBefore);

		if (diff == 0.0)
			return result;
		else
			return new ArrayList<Transaction>();

	}

}
