package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageMacys extends AccountPage {

	public AccountPageMacys(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public Double getTotal() {
		WebElement summary = webDriver.findElement(By.linkText("credit summary"));
		if (summary != null)
			summary.click();
		else
			return null;
		WebElement cont = webDriver.findElement(By.id("speedBumpContinueBtn"));
		if (cont != null)
			cont.click();
		else
			return null;
		// Store the current window handle
		String winHandleBefore = webDriver.getWindowHandle();
		// Switch to new window opened
		for (String winHandle : webDriver.getWindowHandles()) {
			if (!winHandle.equals(winHandleBefore)) {
				webDriver.switchTo().window(winHandle);
				break;
			}
		}
		amount = webDriver.findElement(By.xpath("//div[@id='skip_target']/section[2]/section/article/div/dl[2]/dd"));
		if (amount == null) 
			return null;
		else{
			// Switch back to original browser (first window)
			webDriver.switchTo().window(winHandleBefore);
			WebElement signoff = webDriver.findElement(By.cssSelector("a.sign_off.ui-link"));
			if (signoff != null)
				signoff.click();		
			// Switch to new window opened
			for (String winHandle : webDriver.getWindowHandles()) {
				if (!winHandle.equals(winHandleBefore)) {
					webDriver.switchTo().window(winHandle);
					break;
				}
			}
			return Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
		}		
	}
}
