package com.dashboard.budget.UI;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import com.dashboard.budget.Util;

public class Button extends PageElement {
	
	public Button(String name, By locator, SearchContext searchContext, WebDriver webdriver) {
		super(name, locator, searchContext, webdriver);
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
		if (webElement == null){
			Util.takeScreenshot(webdriver);
			throw new PageElementNotFoundException("Button '" + name + "' (" + locator + ") not found ");
		}
		
		try {
			webElement.click();
		} catch (StaleElementReferenceException ex) {
			webElement = searchContext.findElement(locator);
			if (webElement == null) {
				Util.takeScreenshot(webdriver);
				throw new PageElementNotFoundException("Field '" + name + "' (" + locator + ") not found ");
			}
			webElement.click();
		}		
	}
	
	public void clickAsAction() throws PageElementNotFoundException{
		if (webElement == null)
			webElement = searchContext.findElement(locator);
		if (webElement == null){
			Util.takeScreenshot(webdriver);
			throw new PageElementNotFoundException("Button '" + name + "' (" + locator + ") not found ");
		}
		
		try {
			Actions action = new Actions(webdriver);
			action.moveToElement(webElement).build().perform();
		} catch (StaleElementReferenceException ex) {
			webElement = searchContext.findElement(locator);
			if (webElement == null) {
				Util.takeScreenshot(webdriver);
				throw new PageElementNotFoundException("Field '" + name + "' (" + locator + ") not found ");
			}
			Actions action = new Actions(webdriver);
			action.moveToElement(webElement).build().perform();
		}	
	}
	
	public void clickIfAvailable() throws PageElementNotFoundException{
		if(locator==null)
			return;
		if (webElement == null)
			webElement = searchContext.findElement(locator);
		if (webElement == null)
			throw new PageElementNotFoundException("Button '" + name + "' (" + locator + ") not found ");
		
		try {
			webElement.click();
		} catch (StaleElementReferenceException ex) {
			webElement = searchContext.findElement(locator);
			if (webElement == null) {
				Util.takeScreenshot(webdriver);
				throw new PageElementNotFoundException("Field '" + name + "' (" + locator + ") not found ");
			}
			webElement.click();
		}		
	}

	@Override
	public String toString() {
		return "Button [name=" + name + ", locator=" + locator  + ", webElement=" + webElement + "]";
	}

}
