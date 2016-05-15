package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPagePayPal extends AccountPage {

	public AccountPagePayPal(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public Double getTotal() {
		String balanceStr = webDriver.findElement(By.cssSelector("span.vx_h2.enforceLtr  ")).getText();
		return Util.wrapAmount(convertStringAmountToDouble(balanceStr));
	}


}
