package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageCiti extends AccountPage {

	public AccountPageCiti(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public void gotoHomePage() {
		super.gotoHomePage();
		// Store the current window handle
		String winHandleBefore = webDriver.getWindowHandle();
		// Switch to new window opened
		for (String winHandle : webDriver.getWindowHandles()) {
			if (!winHandle.equals(winHandleBefore)) {
				webDriver.switchTo().window(winHandle);
				break;
			}
		}
	}

	@Override
	public Double getTotal() {
		int code = account.getId();
		// Expedia
		if (code==111) {
			amount = webDriver
					.findElement(By.cssSelector("div.cA-spf-firstBalanceElementValue.cA-spf-accPanlBalElmtPos > span"));
			if (amount == null)
				return null;
			else if (amount.getText().substring(0, 1).equals("("))
				return Util.wrapAmount(convertStringAmountToDouble(amount.getText().replace("(", "").replace(")", "")));
			else
				return Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Thank you
		} else if (code==112) {
			amount = webDriver.findElement(By.xpath("//div[2]/div[2]/div/div[2]/span"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Hilton
		} else if (code==113) {
			// weird but.. locator changes sometimes
			amount = webDriver.findElement(By.xpath("//div[3]/div[2]/div/div[2]/span"));
			if (amount == null)
				amount = webDriver.findElement(By.xpath("//div[3]/div/div[2]/span"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Costco
		} else if (code==114) {
			amount = webDriver.findElement(By.xpath("//div[4]/div[2]/div/div[2]/span"));
			//amount = webDriver.findElement(By.xpath("//div[3]/div/div[2]/span"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));			
		}

		return null;
	}
}