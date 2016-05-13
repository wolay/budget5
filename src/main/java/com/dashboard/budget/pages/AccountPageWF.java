package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

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
}
