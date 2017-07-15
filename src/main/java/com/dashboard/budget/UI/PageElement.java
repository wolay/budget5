package com.dashboard.budget.UI;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PageElement {

	protected String name;
	protected By locator;
	protected SearchContext searchContext;
	protected WebElement webElement;
	protected WebDriver webdriver;
	
	public PageElement(String name, By locator, SearchContext searchContext, WebDriver webdriver) {
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

	public WebDriver getWebdriver() {
		return webdriver;
	}

	public void setWebdriver(WebDriver webdriver) {
		this.webdriver = webdriver;
	}
	
}
