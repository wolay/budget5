package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageChase extends AccountPage {

	public AccountPageChase(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	@Override
	public Double getTotal() {
		int code = account.getId();
		// IHG
		if (code == 131) {
			Util.sleep(5000);
			WebElement nav = webDriver.lookupElement(By.xpath("//div[@id='creditcardGroupaccounts']/div[2]/div[2]/div"));
			if (nav != null) {
				nav.click();
				Util.sleep(5000);
			} else
				return null;
			amount = webDriver.lookupElement(By.cssSelector("td.dataValue.HEADERNUMSTR > span"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Hyatt
		} else if (code == 132) {
			amount = webDriver.lookupElement(By.cssSelector("td.dataValue.HEADERNUMSTR > span"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
		}

		return null;
	}
}