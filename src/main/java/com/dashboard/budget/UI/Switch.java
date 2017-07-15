package com.dashboard.budget.UI;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

import com.dashboard.budget.Util;

public class Switch extends PageElement {
	
	private String action;

	public Switch(String name, By locator, String action, SearchContext searchContext, WebDriver webdriver) {
		super(name, locator, searchContext, webdriver);
		this.action = action;
	}

	public void perform() throws PageElementNotFoundException {
		if (locator == null)
			return;
		if (webElement == null)
			webElement = searchContext.findElement(locator);
		if (webElement == null){
			Util.takeScreenshot(webdriver);
			throw new PageElementNotFoundException("Switch '" + name + "' (" + locator + ") not found ");
		}
		
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
