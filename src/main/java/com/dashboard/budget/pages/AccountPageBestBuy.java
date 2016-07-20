package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageBestBuy extends AccountPage {

	public AccountPageBestBuy(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	@Override
	public Double getTotal() {
		amount = webDriver.findElement(By.xpath("//div[@id='skip_target']/section[2]/section/article/div/dl[2]/dd"));
		WebElement signoff = webDriver.findElement(By.linkText("Sign Off"));
		if (signoff != null)
			signoff.click();
		else
			return null;		
		return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
	}

}
