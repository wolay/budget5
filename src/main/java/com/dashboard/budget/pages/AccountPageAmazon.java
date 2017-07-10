package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Field;
import com.dashboard.budget.DAO.PageElementNotFoundException;

public class AccountPageAmazon extends AccountPage {

	private Field balanceStrDol;
	private Field balanceStrCen;

	public AccountPageAmazon(Account account, DataHandler dataHandler) {
		super(account, dataHandler);

		balanceStrDol = new Field("amount dollars", accountTotalDetails.getBalanceDolLocator(), getWebdriver());
		balanceStrCen = new Field("amount cents", accountTotalDetails.getBalanceCenLocator(), getWebdriver());
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
			return Util.wrapAmount(-convertStringAmountToDouble(
					balanceStrDol.getText() + balanceStrCen.getText().replace("·", ".").replace("*", "")));
		} catch (PageElementNotFoundException e) {
			return null;
		}
	}
	
	public void quit() {
		try {
			btnLogout.click();
			logger.info("Account page {} was closed", account.getName());
		} catch (PageElementNotFoundException e) {
			logger.error("Account page {} was not closed properly", account.getName());
		}

		webDriver.quit();		
	}
}
