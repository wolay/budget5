package com.dashboard.budget.UI;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;

import com.dashboard.budget.Util;

public class Label extends PageElement {

	public Label(String name, By locator, SearchContext searchContext, WebDriver webdriver) {
		super(name, locator, searchContext, webdriver);
	}

	public String getText() throws PageElementNotFoundException {
		if (locator == null)
			return null;
		if (webElement == null)
			webElement = searchContext.findElement(locator);
		if (webElement == null){
			Util.takeScreenshot(webdriver);
			throw new PageElementNotFoundException("Field '" + name + "' (" + locator + ") not found ");
		}
		
		try {
			return webElement.getText().trim();
		} catch (StaleElementReferenceException ex) {
			webElement = searchContext.findElement(locator);
			if (webElement == null) {
				Util.takeScreenshot(webdriver);
				throw new PageElementNotFoundException("Field '" + name + "' (" + locator + ") not found ");
			}
			return webElement.getText().trim();
		}				
	}

	@Override
	public String toString() {
		return "Label [name=" + name + ", locator=" + locator + ", webElement=" + webElement + "]";
	}

}
