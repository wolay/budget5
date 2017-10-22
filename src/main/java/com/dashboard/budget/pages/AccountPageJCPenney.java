package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import java.util.List;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.DataRetrievalStatus;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;
import com.dashboard.budget.UI.PageElementNotFoundException;

public class AccountPageJCPenney extends AccountPage {

	public AccountPageJCPenney(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	public DataRetrievalStatus login() {
		if (Util.checkIfSiteDown(webDriver))
			return DataRetrievalStatus.SERVICE_UNAVAILABLE;
		try {
			fldUsername.setText(valUsername);
			fldPassword.setText(valPassword);
			btnLogin.click();
			return DataRetrievalStatus.SUCCESS;
		} catch (PageElementNotFoundException e) {
			return DataRetrievalStatus.NAVIGATION_BROKEN;
		}
	}

	public Double getTotal() throws PageElementNotFoundException {
		return Util.wrapAmount(-convertStringAmountToDouble(fldBalance.getText()));
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

	@Override
	public List<Transaction> getTransactions(Total total, List<Transaction> prevTransactions)
			throws PageElementNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
}
