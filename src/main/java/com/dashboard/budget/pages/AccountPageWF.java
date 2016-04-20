package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Transaction;

public class AccountPageWF extends AccountPage {

	public AccountPageWF(Account account, DataHandler dataHandler) {
		super(account);
	}

	// @Override
	public synchronized Double getTotal() {
		String code = account.getCode();
		Double result = null;
		WebElement details = null;
		// Checking
		if (code.equals("101")) {
			details = webDriver.findElement(By.xpath("//th[@id='cashAccount1']/a"));
			if (details != null)
				details.click();
			else
				return result;
			webDriver.waitForTextToBePresent(By.name("selectedAccountUID"), "CHECKING XXXXXX6763");
			amount = webDriver.findElement(By.className("availableBalanceTotalAmount"));
			return amount == null ? null : Util.wrapAmount(convertStringAmountToDouble(amount.getText()));
			// Saving
		} else if (code.equals("102")) {
			new Select(webDriver.findElement(By.id("accountDropdown"))).selectByIndex(1);
			details = webDriver.findElement(By.name("accountselection"));
			if (details != null)
				details.click();
			else
				return result;
			webDriver.waitForTextToBePresent(By.name("selectedAccountUID"), "WAY2SAVE® SAVINGS XXXXXX3119");
			amount = webDriver.findElement(By.className("availableBalanceTotalAmount"));
			return amount == null ? null : Util.wrapAmount(convertStringAmountToDouble(amount.getText()));
			// Credit card
		} else if (code.equals("103")) {
			new Select(webDriver.findElement(By.id("accountDropdown"))).selectByIndex(2);
			details = webDriver.findElement(By.name("accountselection"));
			if (details != null)
				details.click();
			else
				return result;
			webDriver.waitForTextToBePresent(By.name("selectedAccountUID"), "PLATINUM CARD XXXX-XXXX-XXXX-4116");
			amount = webDriver.findElement(By.xpath("//table[@id='balancedetailstable']/tbody/tr[2]/td"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
		}

		return Util.wrapAmount(result);
	}

	@Override
	public synchronized List<Transaction> getTransactions(Double diff, List<Transaction> prevTransactions) {

		List<Transaction> result = new ArrayList<Transaction>();
		List<WebElement> rows = webDriver.findElements(By.xpath("//table[@id='DDATransactionTable']/tbody/tr"));
		for (WebElement row : rows) {
			if (row.findElements(By.xpath("./td[2]/span")).size() > 0) {
				String amountIn = row.findElement(By.xpath("./td[2]/span")).getText();
				String amountOut = row.findElement(By.xpath("./td[3]/span")).getText();
				double amount = (" ".equals(amountIn)) ? -Util.convertStringAmountToDouble(amountOut)
						: Util.convertStringAmountToDouble(amountIn);
				String description = row.findElement(By.xpath("./td/span")).getText().trim();
				if ("".equals(description) || amountIn.equals(description))
					description = row.findElement(By.xpath("./td/div/span")).getText().trim();
				Date date = Util.convertStringToDateType1(row.findElement(By.xpath("./th")).getText());
				List<Transaction> matchTransactions = prevTransactions.stream()
						.filter(t -> t.getDate().equals(date) && t.getAmount() == amount).collect(Collectors.toList());
				if (matchTransactions.isEmpty()) {
					result.add(new Transaction(account.getCode(), date, description, amount, ""));
					diff = Util.roundDouble(diff - amount);
				}
			}
			if (diff == 0.0)
				return result;
		}

		if (diff == 0.0)
			return result;
		else
			return new ArrayList<Transaction>();
	}

}
