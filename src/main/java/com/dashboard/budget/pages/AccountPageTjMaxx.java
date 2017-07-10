package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Button;
import com.dashboard.budget.DAO.PageElementNotFoundException;

public class AccountPageTjMaxx extends AccountPage {

	private Button btnPreBalance = new Button("user check button", accountTotalDetails.getPreBalanceLocator(), getWebdriver());

	public AccountPageTjMaxx(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	public synchronized boolean login() {
		if (Util.checkIfSiteDown(webDriver))
			return false;

		try {
			fldUsername.setText(valUsername);
			btnPreBalance.click();
			// Account blocked
			if (Util.isProblemWithLogin(webDriver)) {
				logger.error("There is a problem with login: {}", account.getName());
				return false;
			}
			// Secret question
			if (Util.isSecretQuestionShown(webDriver))
				if (!answerSecretQuestion())
					return false;
			fldPassword.setText(valPassword);
			btnLogin.click();
			return true;
		} catch (PageElementNotFoundException e) {
			return false;
		}
	}

	public Double getTotal() {
		try {
			btnPreBalance.click();
			return Util.wrapAmount(-convertStringAmountToDouble(fldBalance.getText()));
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
