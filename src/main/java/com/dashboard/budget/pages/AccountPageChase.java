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

public class AccountPageChase extends AccountPage {

	public AccountPageChase(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public Double getTotal() {
		String code = account.getCode();
		// IHG
		if (code.equals("131")) {
			String locator = "//form[@id='FORM1']/table[2]/tbody/tr/td/table[4]/tbody/tr/td[3]";
			amount = webDriver.findElement(By.xpath(locator));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Hyatt
		} else if (code.equals("132")) {
			String locator = "//form[@id='FORM1']/table[2]/tbody/tr/td/table[2]/tbody/tr/td[3]";
			amount = webDriver.findElement(By.xpath(locator));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
		}

		return null;
	}

	@Override
	public List<Transaction> getTransactions(Double diff, List<Transaction> prevTransactions) {
		String code = account.getCode();
		WebElement details = null;
		List<Transaction> result = new ArrayList<Transaction>();
		if ("131".equals(code))
			details = webDriver
					.findElement(By.xpath("//form[@id='FORM1']/table[2]/tbody/tr/td/table[4]/tbody/tr/td[5]/a"));
		else if ("132".equals(code))
			details = webDriver
					.findElement(By.xpath("//form[@id='FORM1']/table[2]/tbody/tr/td/table[2]/tbody/tr/td[5]/a"));

		if (details != null)
			details.click();
		else
			return result;

		List<WebElement> rows = webDriver.findElements(By.xpath("//div[@id='Posted']/table/tbody/tr"));
		double amount;
		for (WebElement row : rows) {
			if ("".equals(row.getText().trim()))
				continue;
			if (row.findElements(By.xpath("./td[7]")).size() == 0)
				continue;
			amount = -Util.convertStringAmountToDouble(row.findElement(By.xpath("./td[7]")).getText());
			Date date = Util.convertStringToDateType4(row.findElement(By.xpath("./td[2]")).getText());
			result.add(new Transaction(code, date, row.findElement(By.xpath("./td[5]/span")).getText().trim(), amount,
					""));

			diff = Util.roundDouble(diff - amount);
			if (diff == 0.0) {
				WebElement accounts = webDriver.findElement(By.id("megamenu-MyAccounts"));
				if (accounts != null)
					accounts.click();
				return result;
			}
		}

		new Select(webDriver.findElement(By.id("StatementPeriodQuick"))).selectByIndex(1);
		rows = webDriver.findElements(By.xpath("//div[@id='Posted']/table/tbody/tr"));
		for (WebElement row : rows) {
			if ("".equals(row.getText().trim()))
				continue;
			amount = -Util.convertStringAmountToDouble(row.findElement(By.xpath("./td[7]")).getText());
			Date date = Util.convertStringToDateType4(row.findElement(By.xpath("./td[2]")).getText().trim());
			result.add(new Transaction(code, date, row.findElement(By.xpath("./td[5]/span")).getText().trim(), amount,
					""));
			diff = Util.roundDouble(diff - amount);
			if (diff == 0.0) {
				WebElement accounts = webDriver.findElement(By.id("megamenu-MyAccounts"));
				if (accounts != null)
					accounts.click();
				return result;
			}
		}

		WebElement accounts = webDriver.findElement(By.id("megamenu-MyAccounts"));
		if (accounts != null)
			accounts.click();
		if (diff == 0.0)
			return result;
		else
			return new ArrayList<Transaction>();
	}
}
