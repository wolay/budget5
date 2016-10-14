package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageKohls extends AccountPage {

	public AccountPageKohls(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	@Override
	public Double getTotal() {
		// secret question
		if(Util.isSecretQuestionShown(webDriver))
			if (!super.answerSecretQuestion())
				return null;	

		// secret question could be asked one more time
		if(Util.isSecretQuestionShown(webDriver))
			if (!super.answerSecretQuestion())
				return null;	

		// reading total
		amount = webDriver.findElement(By.xpath("//form/table[2]/tbody/tr/td/table/tbody/tr/td[2]"));
		return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
	}
}
