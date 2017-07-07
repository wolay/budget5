package com.dashboard.budget.pages;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Label;
import com.dashboard.budget.DAO.PageElementNotFoundException;

public class AccountPageCreditKarma extends AccountPage {

	private Label lblTransUnion;
	private Label lblEquifax;
	private final List<Integer> staticCreditScores = Arrays.asList(new Integer[] { 580, 640, 700, 750, 300, 850 });

	public AccountPageCreditKarma(Account account, DataHandler dataHandler) {
		super(account, dataHandler);

		lblTransUnion = new Label("TransUnion score", By.xpath("//div[contains(@class, 'dashboard-score-dials')]/a"),
				this);
		lblEquifax = new Label("TransUnion score", By.xpath("//div[contains(@class, 'dashboard-score-dials')]/a[2]"),
				this);
	}

	public boolean login() {
		if (Util.checkIfSiteDown(webDriver))
			return false;
		try {
			fldUsername.setText(valUsername);
			fldPassword.setText(valPassword);
			btnLogin.click();
			return true;
		} catch (PageElementNotFoundException e) {
			return false;
		}
	}

	public int getScore() throws PageElementNotFoundException {
		return (identifyScore(lblTransUnion.getText()) + identifyScore(lblEquifax.getText())) / 2;
	}

	private Integer identifyScore(String rawData) {
		for (String csStr : rawData.split("\\n")) {
			if (!Util.isNumeric(csStr))
				continue;
			Integer csInt = Integer.valueOf(csStr);
			if (csInt < 300 || csInt > 850)
				continue;
			if (staticCreditScores.contains(csInt))
				continue;
			return csInt;
		}
		return null;
	}

	public Double getTotal() {
		return null;
	}

	public void quit() {
		webDriver.get("https://www.creditkarma.com/auth/logout/lockdown");
		webDriver.quit();
	}

}
