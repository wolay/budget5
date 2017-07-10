package com.dashboard.budget.DAO;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class Field extends PageElement {

	public Field(String name, By locator, SearchContext searchContext) {
		super(name, locator, searchContext);
	}

	public String getText() throws PageElementNotFoundException {
		if (locator == null)
			return null;
		if (webElement == null)
			webElement = searchContext.findElement(locator);
		if (webElement == null)
			throw new PageElementNotFoundException("Field '" + name + "' (" + locator + ") not found ");
		return webElement.getText().trim();
	}

	public void setText(String text) throws PageElementNotFoundException {
		if (locator == null)
			return;
		if (webElement == null)
			webElement = searchContext.findElement(locator);
		if (webElement == null)
			throw new PageElementNotFoundException("Field '" + name + "' (" + locator + ") not found ");
		webElement.sendKeys(text);
	}

	@Override
	public String toString() {
		return "Field [name=" + name + ", locator=" + locator + ", webElement=" + webElement + "]";
	}

}
