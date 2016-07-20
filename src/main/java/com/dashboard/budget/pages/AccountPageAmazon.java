package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageAmazon extends AccountPage {

	public AccountPageAmazon(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	@Override
	public Double getTotal() {
		String balanceStrDol = webDriver.findElement(By.cssSelector("span.dollarAmt")).getText();
		String balanceStrCen = webDriver.findElement(By.cssSelector("span.cent")).getText().replace("·", ".")
				.replace("*", "");
		return Util.wrapAmount(-convertStringAmountToDouble(balanceStrDol + balanceStrCen));
	}


}
