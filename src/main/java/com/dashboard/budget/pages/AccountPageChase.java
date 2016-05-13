package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

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
}