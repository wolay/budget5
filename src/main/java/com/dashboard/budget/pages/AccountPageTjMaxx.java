package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import java.util.List;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Button;
import com.dashboard.budget.DAO.DataRetrievalStatus;
import com.dashboard.budget.DAO.PageElementNotFoundException;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;

public class AccountPageTjMaxx extends AccountPage {

	private Button btnPreBalance = new Button("user check button", accountTotalDetails.getPreBalanceLocator(), getWebdriver());

	public AccountPageTjMaxx(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	public DataRetrievalStatus login() {
		if (Util.checkIfSiteDown(webDriver))
			return DataRetrievalStatus.SERVICE_UNAVAILABLE;

		try {
			fldUsername.setText(valUsername);
			btnPreBalance.click();
			// Account blocked
			if (Util.isProblemWithLogin(webDriver)) {
				logger.error("There is a problem with login: {}", account.getName());
				return DataRetrievalStatus.SERVICE_UNAVAILABLE;
			}
			// Secret question
			if (Util.isSecretQuestionShown(webDriver))
				if (!answerSecretQuestion())
					return DataRetrievalStatus.SERVICE_UNAVAILABLE;
			fldPassword.setText(valPassword);
			btnLogin.click();
			return DataRetrievalStatus.SUCCESS;
		} catch (PageElementNotFoundException e) {
			return DataRetrievalStatus.NAVIGATION_BROKEN;
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

	@Override
	public List<Transaction> getTransactions(Total total, List<Transaction> prevTransactions)
			throws PageElementNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
}
