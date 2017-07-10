package com.dashboard.budget.DAO;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.ui.Select;

public class Switch extends PageElement {
	
	private String action;

	public Switch(String name, By locator, String action, SearchContext searchContext) {
		super(name, locator, searchContext);
		this.action = action;
	}

	public void perform() throws PageElementNotFoundException {
		if (locator == null)
			return;
		if (webElement == null)
			webElement = searchContext.findElement(locator);
		if (webElement == null)
			throw new PageElementNotFoundException("Switch '" + name + "' (" + locator + ") not found ");
		
		if ("click".equals(action))
			webElement.click();
		else
			new Select(webElement).selectByIndex(1);
	}

	@Override
	public String toString() {
		return "Switch [name=" + name + ", locator=" + locator + ", webElement=" + webElement + "]";
	}

}
