package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageNordstorm extends AccountPage {

	public AccountPageNordstorm(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	@Override
	public Double getTotal() {
		// secret question
		if(Util.isSecretQuestionShown(webDriver))
			if (!super.answerSecretQuestion())
				return null;	

		// reading balance
		String locator = "//ul[@class='accountslist ng-scope']/li[3]//strong";
		amount = webDriver.findElement(By.xpath(locator));
		return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
	}
}
