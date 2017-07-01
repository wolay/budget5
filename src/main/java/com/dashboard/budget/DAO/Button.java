package com.dashboard.budget.DAO;

import org.openqa.selenium.By;

import com.dashboard.budget.pages.Page;

public class Button extends PageElement {
	
	public Button(String name, By locator, Page accountPage) {
		super(name, locator, accountPage);
	}
	
	public void click() throws PageElementNotFoundException{
		if (webElement == null)
			webElement = accountPage.getWebdriver().findElement(locator);
		if (webElement == null)
			throw new PageElementNotFoundException("Button '" + name + "' (" + locator + ") not found ");
		webElement.click();
	}
	
	public void clickIfAvailable() throws PageElementNotFoundException{
		if(locator==null)
			return;
		if (webElement == null)
			webElement = accountPage.getWebdriver().findElement(locator);
		if (webElement == null)
			throw new PageElementNotFoundException("Button '" + name + "' (" + locator + ") not found ");
		webElement.click();
	}

	@Override
	public String toString() {
		return "Button [name=" + name + ", locator=" + locator + ", accountPage=" + accountPage + ", webElement="
				+ webElement + "]";
	}

}
