package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPagePayPal extends AccountPage {

	public AccountPagePayPal(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	@Override
	public Double getTotal() {
		// skip advertisement
		WebElement ad = webDriver.lookupElement(By.linkText("Proceed to Account Overview"));
		if (ad != null)
			ad.click();

		WebElement balPage = webDriver.lookupElement(By.id("js_balanceModule"));
		if (balPage != null)
			balPage.click();

		String balanceStr = null;
		WebElement bal = webDriver.lookupElement(By.cssSelector("dd.total.vx_h2   > span.enforceLtr"));
		if (bal != null)
			balanceStr = bal.getText();

		WebElement exit = webDriver
				.lookupElement(By.cssSelector("div.col-xs-12 > a[name='close'] > span.icon.icon-close-small"));
		if (exit != null) {
			webDriver.waitToBeClickable(By.cssSelector("div.col-xs-12 > a[name='close'] > span.icon.icon-close-small"));
			exit.click();
		}

		return Util.wrapAmount(convertStringAmountToDouble(balanceStr));
	}

}
