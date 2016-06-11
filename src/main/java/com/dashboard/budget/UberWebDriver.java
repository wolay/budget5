package com.dashboard.budget;

import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.pages.AccountPage;

public class UberWebDriver implements Config {

	protected static Logger logger = LoggerFactory.getLogger(AccountPage.class);

	private WebDriver webDriver;
	private WebDriverWait wait;

	public UberWebDriver() {
		System.setProperty("webdriver.chrome.driver", "chromedriver");
		this.webDriver = new ChromeDriver();
		this.wait = new WebDriverWait(this.webDriver, timeout);
	}

	public void get(String url) {
		webDriver.get(url);
	}

	public void quit() {
		webDriver.quit();
	}

	public TargetLocator switchTo() {
		return webDriver.switchTo();
	}

	public String getWindowHandle() {
		return webDriver.getWindowHandle();
	}

	public Set<String> getWindowHandles() {
		return webDriver.getWindowHandles();
	}

	public String getPageSource() {
		return webDriver.getPageSource();
	}

	public WebElement lookupElement(By by) {
		int i = 0;
		while (webDriver.findElements(by).size() == 0 && i < 20) {
			Util.sleep(500);
			i++;
		}
		List<WebElement> elements = webDriver.findElements(by);
		if (elements.size() > 0)
			return elements.get(0);
		else
			return null;
	}

	public WebElement findElement(By by) {
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			return webDriver.findElement(by);
		} catch (Exception e) {
			logger.error("Unable to find locator: {}", by);
			return null;
		}
	}

	public List<WebElement> findElements(By by) {
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			return webDriver.findElements(by);
		} catch (Exception e) {
			logger.error("Unable to find locator: {}", by);
			return null;
		}
	}

	public void waitForTextToBePresent(By by, String text) {
		try {
			wait.until(ExpectedConditions.textToBePresentInElement(findElement(by), text));
		} catch (Exception e) {
			logger.error("Unable to find locator: {}", by);
		}
	}

	public void waitToBeClickable(By by) {
		wait.until(ExpectedConditions.elementToBeClickable(by));
	}
}
