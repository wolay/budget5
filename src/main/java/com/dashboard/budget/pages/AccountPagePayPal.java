package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPagePayPal extends AccountPage {

	public AccountPagePayPal(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public Double getTotal() {
		// skip advertisement
		WebElement ad = webDriver.lookupElement(By.linkText("Proceed to Account Overview"));
		if (ad != null)
			ad.click();

		String balanceStr = webDriver.findElement(By.cssSelector("span.vx_h2.enforceLtr  ")).getText();
		return Util.wrapAmount(convertStringAmountToDouble(balanceStr));
	}

}
