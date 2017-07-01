package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageTjMaxx extends AccountPage {

	public AccountPageTjMaxx(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	@Override
	public Double getTotal() {
		int code = account.getId();
		// Andrei
		if (code==207) {
			amount = webDriver.findElement(By.id("currentBalance"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Irina
		} else if (code==208) {
			WebElement next = webDriver.findElement(By.cssSelector("a.RegisterNextBtnleft.wal_dualno > span"));
			if (next != null)
				next.click();
			else
				return null;
			amount = webDriver.findElement(By.id("currentBalance"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
		}

		return null;
	}
}
