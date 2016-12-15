package com.dashboard.budget.pages;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.DAO.Account;

public class AccountPageCreditKarma extends AccountPage {

	public AccountPageCreditKarma(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	public int getScore() {
		int scoreTransUnion = Integer.valueOf(webDriver
				.findElement(By.xpath("//div[@class='rich-medias-container']/div[1]//a/div"))
				.getText().split("\\n")[6]);
		int scoreEquifax = Integer.valueOf(webDriver
				.findElement(By.xpath("//div[@class='rich-medias-container']/div[2]//a/div"))
				.getText().split("\\n")[6]);
		return (scoreTransUnion + scoreEquifax) / 2;
	}

	@Override
	public Double getTotal() {
		return null;
	}

	@Override
	public void quit() {
		webDriver.get("https://www.creditkarma.com/auth/logout/lockdown");
		webDriver.quit();
	}

}
