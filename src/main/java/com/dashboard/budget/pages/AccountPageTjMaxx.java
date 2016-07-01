package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageTjMaxx extends AccountPage {

	private By btnLoginConfirmation = By.cssSelector("a.RegisterNextBtnleft > span");

	public AccountPageTjMaxx(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public boolean login() {
		fldUsername = By.name(accountDetails.getUsernameLocator());
		if(fldUsername==null)
			return false;
		fldPassword = By.name(accountDetails.getPasswordLocator());
		if(fldPassword==null)
			return false;		
		btnLogin = accountDetails.getLoginLocator();
		if(btnLogin==null)
			return false;

		webDriver.findElement(fldUsername).sendKeys(accountDetails.getUsernameValue());
		webDriver.findElement(btnLogin).click();

		// secret question
		WebElement securityLabel = webDriver.lookupElement(By.xpath("//*[contains(text(),'Challenge Question')]"));
		if (securityLabel != null) {
			String question = webDriver.findElement(By.xpath("//tr[4]/td[2]")).getText().trim();
			if ("Andrei".equals(account.getOwner())) {
				if (question.equals("In what city were you married? (Enter full name of city)")) {
					webDriver.findElement(By.name("challengeAnswer1")).clear();
					webDriver.findElement(By.name("challengeAnswer1")).sendKeys("Moscow");
				} else if (question.equals("What was the name of your first pet?")) {
					webDriver.findElement(By.name("challengeAnswer1")).clear();
					webDriver.findElement(By.id("challengeAnswer1")).sendKeys("Murzik");
				}
			} else {
				if (question.equals("In what city were you married? (Enter full name of city)")) {
					webDriver.findElement(By.name("challengeAnswer1")).clear();
					webDriver.findElement(By.name("challengeAnswer1")).sendKeys("Moscow");
				} else if (question.equals("What was the name of your first pet?")) {
					webDriver.findElement(By.name("challengeAnswer1")).clear();
					webDriver.findElement(By.name("challengeAnswer1")).sendKeys("Jessy");
				}
			}
			WebElement submit = webDriver.findElement(By.id("submitChallengeAnswers"));
			if (submit != null)
				submit.click();
			else
				return false;
		}

		webDriver.findElement(fldPassword).sendKeys(accountDetails.getPasswordValue());
		webDriver.findElement(btnLoginConfirmation).click();
		return true;
	}

	@Override
	public Double getTotal() {
		int code = account.getId();
		// Andrei
		if (code==207) {
			amount = webDriver.findElement(By.id("currentBalance"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Irina
		} else if (code==208) {
			WebElement next = webDriver.findElement(By.cssSelector("a.RegisterNextBtnleft.wal_dualno > span"));
			if (next != null)
				next.click();
			else
				return null;
			amount = webDriver.findElement(By.id("currentBalance"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
		}

		return null;
	}
}
