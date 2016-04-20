package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Transaction;

public class AccountPageBestBuy extends AccountPage {

	public AccountPageBestBuy(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public boolean login() {
		btnLogin = By.cssSelector(accountDetails.getLoginLocator());
		super.login();
		return true;
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

	@Override
	public List<Transaction> getTransactions(Double diff, List<Transaction> prevTransactions) {
		// not implemented yet
		return new ArrayList<Transaction>();
	}

}
