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

public class AccountPageAmazon extends AccountPage {

	public AccountPageAmazon(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public Double getTotal() {
		String balanceStrDol = webDriver.findElement(By.cssSelector("span.dollarAmt")).getText();
		String balanceStrCen = webDriver.findElement(By.cssSelector("span.cent")).getText().replace("·", ".")
				.replace("*", "");
		return Util.wrapAmount(-convertStringAmountToDouble(balanceStrDol + balanceStrCen));
	}

	@Override
	public synchronized List<Transaction> getTransactions(Double diff, List<Transaction> prevTransactions) {

		List<Transaction> result = new ArrayList<Transaction>();
		String code = account.getCode();

		WebElement activity = webDriver.findElement(By.linkText("View Activity"));
		if (activity != null)
			activity.click();
		else
			return result;

		List<WebElement> rows = webDriver.findElements(By.xpath("//div[@id='completedBillingActivityDiv']/ul/li"));
		for (WebElement row : rows) {
			double amount = -Util.convertStringAmountToDouble(row.findElement(By.xpath("./div[4]/p/span")).getText());
			Date date = Util.convertStringToDateType5(row.findElement(By.xpath("./div[2]/p")).getText());
			result.add(
					new Transaction(code, date, row.findElement(By.xpath("./div[3]/h3")).getText().trim(), amount, ""));
			diff = Util.roundDouble(diff - amount);
			if (diff == 0.0) {
				return result;
			}
		}

		if (diff == 0.0)
			return result;
		else
			return new ArrayList<Transaction>();
	}

}
