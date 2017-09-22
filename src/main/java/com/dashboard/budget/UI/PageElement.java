package com.dashboard.budget.UI;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.UberWebDriver;

public class PageElement {

	protected String name;
	protected By locator;
	protected SearchContext searchContext;
	protected WebElement webElement;
	protected UberWebDriver webdriver;
	
	public PageElement(String name, By locator, SearchContext searchContext, UberWebDriver webdriver) {
		this.name = name;
		this.locator = locator;
		this.searchContext = searchContext;
		this.webdriver = webdriver;
	}

	public String getName() {
		return name;
	}

	public By getLocator() {
		return locator;
	}

	public SearchContext getSearchContext() {
		return searchContext;
	}

	public void setSearchContext(SearchContext searchContext) {
		this.searchContext = searchContext;
	}

	public WebElement getWebElement() {
		return webElement;
	}

	public void setWebElement(WebElement webElement) {
		this.webElement = webElement;
	}

	public UberWebDriver getWebdriver() {
		return webdriver;
	}

	public void setWebdriver(UberWebDriver webdriver) {
		this.webdriver = webdriver;
	}
	
}
