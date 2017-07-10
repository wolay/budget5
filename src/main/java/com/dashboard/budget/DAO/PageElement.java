package com.dashboard.budget.DAO;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class PageElement {

	protected String name;
	protected By locator;
	protected SearchContext searchContext;
	protected WebElement webElement;
	
	public PageElement(String name, By locator, SearchContext searchContext) {
		this.name = name;
		this.locator = locator;
		this.searchContext = searchContext;
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
	
}
