package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageCapOne extends AccountPage {

	public AccountPageCapOne(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	@Override
	public void gotoHomePage() {
		super.gotoHomePage();
		webDriver.switchTo().frame(1);
	}

	@Override
	public Double getTotal() {
		webDriver.switchTo().defaultContent();
		amount = webDriver
				.findElement(By.xpath("//article[@id='acct0_bricklet']//li[@class='cf cur_bal']/div[@class='amount']"));
		return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
	}
}
