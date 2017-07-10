package com.dashboard.budget.pages;

import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Label;
import com.dashboard.budget.DAO.PageElementNotFoundException;
import com.google.common.collect.Sets;

public class AccountPageCreditKarma extends AccountPage {

	private Label lblTransUnion;
	private Label lblEquifax;
	private Set<Integer> staticCreditScores = Sets.newHashSet(580, 640, 700, 750, 300, 850);

	public AccountPageCreditKarma(Account account, DataHandler dataHandler) {
		super(account, dataHandler);

		lblTransUnion = new Label("TransUnion score", By.xpath("//div[contains(@class, 'dashboard-score-dials')]/a"),
				getWebdriver());
		lblEquifax = new Label("TransUnion score", By.xpath("//div[contains(@class, 'dashboard-score-dials')]/a[2]"),
				getWebdriver());
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
		// TransUnion
		String rawTransUnion = lblTransUnion.getText();
		logger.info("Parsing 'TransUnion' credit score.. '{}' ", rawTransUnion);
		
		Integer csTransUnion = identifyScore(rawTransUnion);
		logger.info("Identified 'TransUnion' credit score = {} ", csTransUnion);
		
		// Equifax
		String rawEquifax = lblEquifax.getText(); 
		logger.info("Parsing 'Equifax' credit score.. '{}' ", rawEquifax);
		
		Integer csEquifax = identifyScore(rawEquifax);
		logger.info("Identified 'Equifax' credit score = {} ", csEquifax);
		
		// Average credit score
		return (csTransUnion + csEquifax) / 2;
	}

	private Integer identifyScore(String rawData) {
		Set<Integer> staticScores = new HashSet<Integer>(staticCreditScores);
		for (String csStr : rawData.split("\\n")) {
			if (!Util.isNumeric(csStr))
				continue;
			Integer csInt = Integer.valueOf(csStr);
			if (csInt < 300 || csInt > 850)
				continue;
			if (staticScores.contains(csInt)){
				staticScores.remove(csInt);
				continue;
			}
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
