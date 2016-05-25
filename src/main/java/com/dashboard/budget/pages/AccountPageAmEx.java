package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageAmEx extends AccountPage {

	public AccountPageAmEx(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public Double getTotal() {
		int code = account.getCode();
		WebElement nav = null;
		// Blue Cash
		if (code == 121) {
			amount = webDriver.findElement(By.id("ah-outstanding-balance-value"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Costco
		} else if (code==122) {
			Util.sleep(5000);
			nav = webDriver.findElement(By.id("iNavCSImg1"));
			if (nav != null)
				nav.click();
			else
				return null;
			amount = webDriver.findElement(By.id("ah-outstanding-balance-value"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Hilton
		} else if (code==123) {
			Util.sleep(5000);
			nav = webDriver.findElement(By.id("iNavCSImg2"));
			if (nav != null)
				nav.click();
			else
				return null;
			amount = webDriver.findElement(By.id("ah-outstanding-balance-value"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Gold
		} else if (code==124) {
			amount = webDriver.findElement(By.id("ah-outstanding-balance-value"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
		}

		return null;
	}
}
