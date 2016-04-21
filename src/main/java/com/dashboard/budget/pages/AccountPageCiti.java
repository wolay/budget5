package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Transaction;

public class AccountPageCiti extends AccountPage {

	public AccountPageCiti(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public void gotoHomePage() {
		super.gotoHomePage();
		// Store the current window handle
		String winHandleBefore = webDriver.getWindowHandle();
		// Switch to new window opened
		for (String winHandle : webDriver.getWindowHandles()) {
			if (!winHandle.equals(winHandleBefore)) {
				webDriver.switchTo().window(winHandle);
				break;
			}
		}
	}

	@Override
	public Double getTotal() {
		String code = account.getCode();
		// Expedia
		if (code.equals("111")) {
			amount = webDriver
					.findElement(By.cssSelector("div.cA-spf-firstBalanceElementValue.cA-spf-accPanlBalElmtPos > span"));
			if (amount == null)
				return null;
			else if (amount.getText().substring(0, 1).equals("("))
				return Util.wrapAmount(convertStringAmountToDouble(amount.getText().replace("(", "").replace(")", "")));
			else
				return Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Thank you
		} else if (code.equals("112")) {
			amount = webDriver.findElement(By.xpath("//div[2]/div[2]/div/div[2]/span"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Hilton
		} else if (code.equals("113")) {
			amount = webDriver.findElement(By.xpath("//div[3]/div[2]/div/div[2]/span"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
		}

		return null;
	}

	@Override
	public List<Transaction> getTransactions(Double diff, List<Transaction> prevTransactions) {

		String code = account.getCode();
		List<Transaction> result = new ArrayList<Transaction>();
		WebElement details = null;
		// Expedia
		if (code.equals("111"))
			details = webDriver.findElement(By.xpath("//div/div/div[4]/a"));
		// Thank you
		else if (code.equals("112"))
			details = webDriver.findElement(By.xpath("//div[2]/div[2]/div/div[4]/a"));
		// Hilton
		else if (code.equals("113"))
			details = webDriver.findElement(By.xpath("//div[3]/div[2]/div/div[4]/a"));

		if (details != null)
			details.click();
		else
			return result;

		// Util.sleep(10000);
		// Tricky moment - pending transactions populate in /table/tboby
		// and posted transaction populate in /table/tbody[2]
		// but.. if there is no pending transactions
		// then posted transactions populate in /table/tbody
		List<WebElement> rows = null;
		if (webDriver.findElement(By.xpath("//div[@id='postedTansactionTable']/table/tbody[2]/tr")) == null)
			rows = webDriver.findElements(By.xpath("//div[@id='postedTansactionTable']/table/tbody/tr"));
		else
			rows = webDriver.findElements(By.xpath("//div[@id='postedTansactionTable']/table/tbody[2]/tr"));

		if (rows == null)
			return new ArrayList<Transaction>();
		double amount;
		for (WebElement row : rows) {
			if ("".equals(row.getText().trim()) || row.getText().trim().startsWith("There is no recent activity"))
				continue;
			String amountStr = row.findElement(By.xpath("./td[3]/span")).getText();
			if (amountStr.substring(0, 1).equals("("))
				amount = convertStringAmountToDouble(amountStr.replace("(", "").replace(")", ""));
			else
				amount = -convertStringAmountToDouble(amountStr);
			Date date = Util.convertStringToDateType2(row.findElement(By.xpath("./td[1]/span")).getText());
			result.add(new Transaction(code, date, row.findElement(By.xpath("./td[2]/span")).getText().trim(), amount,
					""));
			diff = Util.roundDouble(diff - amount);
			if (diff == 0.0) {
				WebElement accounts = webDriver.findElement(By.linkText("Accounts"));
				if (accounts != null)
					accounts.click();
				Util.sleep(3000);
				return result;
			}
		}

		new Select(webDriver.findElement(By.id("filterDropDown"))).selectByIndex(1);
		rows = webDriver.findElements(By.xpath("//div[@id='postedTansactionTable']/table/tbody/tr"));
		if (rows == null)
			return new ArrayList<Transaction>();
		for (WebElement row : rows) {
			if ("".equals(row.getText().trim()) || row.getText().trim().startsWith("There is no recent activity"))
				continue;
			WebElement rowAmount = row.findElement(By.xpath("./td[3]/span"));
			if (rowAmount == null)
				return new ArrayList<Transaction>();
			String amountStr = rowAmount.getText();
			if (amountStr.substring(0, 1).equals("("))
				amount = convertStringAmountToDouble(amountStr.replace("(", "").replace(")", ""));
			else
				amount = -convertStringAmountToDouble(amountStr);
			Date date = Util.convertStringToDateType2(row.findElement(By.xpath("./td[1]/span")).getText());
			result.add(new Transaction(code, date, row.findElement(By.xpath("./td[2]/span")).getText().trim(), amount,
					""));
			diff = Util.roundDouble(diff - amount);
			if (diff == 0.0) {
				WebElement accounts = webDriver.findElement(By.linkText("Accounts"));
				if (accounts != null) {
					accounts.click();
					Util.sleep(3000);
				}
				return result;
			}
		}

		WebElement accounts = webDriver.findElement(By.linkText("Accounts"));
		if (accounts != null) {
			accounts.click();
			Util.sleep(3000);
		}
		if (diff == 0.0)
			return result;
		else
			return new ArrayList<Transaction>();
	}
}