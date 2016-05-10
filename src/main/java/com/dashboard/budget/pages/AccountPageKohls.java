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

public class AccountPageKohls extends AccountPage {

	public AccountPageKohls(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public Double getTotal() {
		// secret question
		WebElement securityLabel = webDriver.findElement(By.xpath("//*[contains(text(),'Security Verification')]"));
		if (securityLabel != null) {
			String question = webDriver.findElement(By.xpath("//form/table/tbody/tr/td[2]")).getText().trim();
			if (question.equals("What was the name of the organization where you conducted your first internship?")) {
				webDriver.findElement(By.id("singleanswer")).clear();
				webDriver.findElement(By.id("singleanswer")).sendKeys("Proteus");
			} else if (question.equals("What city were you in on New Year's Eve, 1999?")) {
				webDriver.findElement(By.id("singleanswer")).clear();
				webDriver.findElement(By.id("singleanswer")).sendKeys("Saransk");
			} else if (question.equals("What was the name of your first pet?")) {
				webDriver.findElement(By.id("singleanswer")).clear();
				webDriver.findElement(By.id("singleanswer")).sendKeys("Murzik");
			}
			WebElement submit = webDriver.findElement(By.id("submitChallengeAnswers"));
			if (submit != null)
				submit.click();
			else
				return null;
		}

		// could be asked one more time
		if (securityLabel != null) {
			securityLabel = webDriver.findElement(By.xpath("//*[contains(text(),'Security Verification')]"));
			if (securityLabel != null) {
				String question = webDriver.findElement(By.xpath("//form/table/tbody/tr/td[2]")).getText().trim();
				if (question
						.equals("What was the name of the organization where you conducted your first internship?")) {
					webDriver.findElement(By.id("singleanswer")).clear();
					webDriver.findElement(By.id("singleanswer")).sendKeys("Proteus");
				} else if (question.equals("What city were you in on New Year's Eve, 1999?")) {
					webDriver.findElement(By.id("singleanswer")).clear();
					webDriver.findElement(By.id("singleanswer")).sendKeys("Saransk");
				} else if (question.equals("What was the name of your first pet?")) {
					webDriver.findElement(By.id("singleanswer")).clear();
					webDriver.findElement(By.id("singleanswer")).sendKeys("Murzik");
				}
				WebElement submit = webDriver.findElement(By.id("submitChallengeAnswers"));
				if (submit != null)
					submit.click();
				else
					return null;
			}
		}

		// reading total
		amount = webDriver.findElement(By.xpath("//form/table[2]/tbody/tr/td/table/tbody/tr/td[2]"));
		return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
	}

	@Override
	public List<Transaction> getTransactions(Double diff, List<Transaction> prevTransactions) {
		// not implemented yet
		return new ArrayList<Transaction>();
	}

}
