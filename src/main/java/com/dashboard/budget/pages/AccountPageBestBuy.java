package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.PageElementNotFoundException;

public class AccountPageBestBuy extends AccountPage {

	public AccountPageBestBuy(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	public boolean login() {
		if (Util.checkIfSiteDown(webDriver))
			return false;

		Util.sleep(3000);

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
			Double amount = Util.wrapAmount(-convertStringAmountToDouble(fldBalance.getText()));
			btnPostLogout.click();
			return amount;
		} catch (PageElementNotFoundException e) {
			return null;
		}
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
