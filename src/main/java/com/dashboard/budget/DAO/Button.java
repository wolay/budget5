package com.dashboard.budget.DAO;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class Button extends PageElement {
	
	public Button(String name, By locator, SearchContext searchContext) {
		super(name, locator, searchContext);
	}
	
	/*
	// More complicated scenario for click
	WebElement weDetails = webDriver.lookupElement(accountNavigationDetails.getDetailsLinkLocator());
	if (weDetails != null) {
		webDriver.waitToBeClickable(accountNavigationDetails.getDetailsLinkLocator());
		try {
			weDetails.click();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	
	public void click() throws PageElementNotFoundException{
		if (webElement == null)
			webElement = searchContext.findElement(locator);
		if (webElement == null)
			throw new PageElementNotFoundException("Button '" + name + "' (" + locator + ") not found ");
		webElement.click();
	}
	
	public void clickIfAvailable() throws PageElementNotFoundException{
		if(locator==null)
			return;
		if (webElement == null)
			webElement = searchContext.findElement(locator);
		if (webElement == null)
			throw new PageElementNotFoundException("Button '" + name + "' (" + locator + ") not found ");
		webElement.click();
	}

	@Override
	public String toString() {
		return "Button [name=" + name + ", locator=" + locator  + ", webElement=" + webElement + "]";
	}

}
