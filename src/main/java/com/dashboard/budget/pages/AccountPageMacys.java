package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Button;
import com.dashboard.budget.DAO.PageElementNotFoundException;

public class AccountPageMacys extends AccountPage {

	private Button btnCreditSummary = new Button("credit summary", By.linkText("Credit Summary"), getWebdriver());

	public AccountPageMacys(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	public synchronized boolean login() {
		if (Util.checkIfSiteDown(webDriver))
			return false;
		try {
			fldUsername.setText(valUsername);
			fldPassword.setText(valPassword);
			btnLogin.click();
			return true;
		} catch (PageElementNotFoundException e) {
			return false;
		}
	}

	public Double getTotal() {

		try {
			btnCreditSummary.click();
		} catch (PageElementNotFoundException e) {
			return null;
		}

		// Store the current window handle
		String winHandleBefore = webDriver.getWindowHandle();
		// Switch to new window opened
		for (String winHandle : webDriver.getWindowHandles()) {
			if (!winHandle.equals(winHandleBefore)) {
				webDriver.switchTo().window(winHandle);
				break;
			}
		}

		Double amount;
		try {
			amount = Util.wrapAmount(-convertStringAmountToDouble(fldBalance.getText()));
		} catch (PageElementNotFoundException e) {
			return null;
		}

		// Switch back to original browser (first window)
		webDriver.switchTo().window(winHandleBefore);
		// Switch to new window opened
		for (String winHandle : webDriver.getWindowHandles()) {
			if (!winHandle.equals(winHandleBefore)) {
				webDriver.switchTo().window(winHandle);
				break;
			}
		}

		return amount;
	}
	
	public void quit() {
		try {
			btnLogout.click();
			btnPostLogout.clickIfAvailable();
			logger.info("Account page {} was closed", account.getName());
		} catch (PageElementNotFoundException e) {
			logger.error("Account page {} was not closed properly", account.getName());
		}

		webDriver.quit();		
	}
}
