package com.dashboard.budget.DAO;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.pages.Page;

public class PageElement {

	protected String name;
	protected By locator;
	protected Page accountPage;
	protected WebElement webElement;
	
	public PageElement(String name, By locator, Page accountPage) {
		this.name = name;
		this.locator = locator;
		this.accountPage = accountPage;
	}

	public String getName() {
		return name;
	}

	public By getLocator() {
		return locator;
	}

	public Page getAccountPage() {
		return accountPage;
	}

	public WebElement getWebElement() {
		return webElement;
	}

	public void setWebElement(WebElement webElement) {
		this.webElement = webElement;
	}
	
}
