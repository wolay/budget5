package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageChase extends AccountPage {

	public AccountPageChase(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public Double getTotal() {
		int code = account.getCode();
		// IHG
		if (code==131) {
			//Util.sleep(5000);
			WebElement nav = webDriver.findElement(By.xpath("//div[@id='creditcardGroupaccounts']/div[2]/div[2]"));
			if (nav != null)
				nav.click();
			else
				return null;			
			String locator = "td.dataValue.HEADERNUMSTR > span";
			amount = webDriver.findElement(By.cssSelector(locator));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Hyatt
		} else if (code==132) {
			String locator = "td.dataValue.HEADERNUMSTR > span";
			amount = webDriver.findElement(By.cssSelector(locator));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
		}

		return null;
	}
}