package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageMP extends AccountPage {

	private List<mpTotal> totalsList;

	public AccountPageMP(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

	@Override
	public Double getTotal() {

		// DONT FORGET ABOUT RERESHING STATUS

		// first check if table of totals already captured
		if (totalsList == null) {
			// navigate to MyPortfolio page if it's not there yet
			if (!webDriver.getWebDriver().getTitle().contains("My Portfolio")) {
				Actions action = new Actions(webDriver.getWebDriver());
				WebElement we = webDriver.findElement(By.name("onh_tools_and_investing"));
				if (we != null)
					action.moveToElement(we).build().perform();
				WebElement submit1 = webDriver.findElement(By.name("onh_tools_and_investing_my_portfolio"));
				if (submit1 != null)
					submit1.click();
			}
			webDriver.waitFrameToBeAvailableAndSwitchToIt("htmlhelp");

			totalsList = new ArrayList<mpTotal>();

			// debit accounts
			List<WebElement> debitAccounts = webDriver
					.findElements(By.xpath("//div[@id='main-table']/div/table/tbody/tr"));
			if (debitAccounts == null) {
				logger.error("Unable to find table with debit accounts");
				return null;
			}
			for (WebElement row : debitAccounts) {
				if (row.getText().contains("TOTAL:"))
					break;

				WebElement weId = webDriver.findElementInRow(row, By.xpath("./td/div/div/div[2]/span"));
				if (weId == null) {
					logger.error("Unable to find 'my portfolio id' in a table with debit accounts");
					return null;
				}

				WebElement weAmount = webDriver.findElementInRow(row, By.xpath("./td[2]/span/span"));
				if (weAmount == null) {
					logger.error("Unable to find 'amount' in a table with debit accounts");
					return null;
				}

				String totalLocator = Util.getLocatorForWebElement(row);

				totalsList.add(
						new mpTotal(weId.getText(), totalLocator, convertStringAmountToDouble(weAmount.getText())));
			}

			// credit accounts
			List<WebElement> creditAccounts = webDriver
					.findElements(By.xpath("//div[@id='main-table']/div[2]/table/tbody/tr"));
			if (creditAccounts == null) {
				logger.error("Unable to find table with credit accounts");
				return null;
			}
			for (WebElement row : creditAccounts) {
				if (row.getText().contains("TOTAL:"))
					break;

				WebElement weId = webDriver.findElementInRow(row, By.xpath("./td/div/div/div[2]/span"));
				if (weId == null) {
					logger.error("Unable to find 'my portfolio id' in a table with debit accounts");
					return null;
				}

				WebElement weAmount = webDriver.findElementInRow(row, By.xpath("./td[2]/span/span"));
				if (weAmount == null) {
					logger.error("Unable to find 'amount' in a table with debit accounts");
					return null;
				}

				String totalLocator = Util.getLocatorForWebElement(row);

				totalsList.add(
						new mpTotal(weId.getText(), totalLocator, -convertStringAmountToDouble(weAmount.getText())));
			}

			totalsList.stream().forEach(t -> logger.info(t.toString()));
			logger.info("все!");
		}

		// secret question
		WebElement securityLabel = webDriver.lookupElement(By.xpath("//*[contains(text(),'Secret')]"));
		if (securityLabel != null) {
			WebElement question = webDriver.lookupElement(By.cssSelector("label"));
			if (question != null) {
				if (question.getText().equals("What was the name of your first pet?")) {
					webDriver.findElement(By.id("tlpvt-challenge-answer")).clear();
					webDriver.findElement(By.id("tlpvt-challenge-answer")).sendKeys("Jessy");
				} else if (question.getText().equals("What is your mother's middle name?")) {
					webDriver.findElement(By.id("tlpvt-challenge-answer")).clear();
					webDriver.findElement(By.id("tlpvt-challenge-answer")).sendKeys("Nikolaevna");
				} else if (question.getText().equals("What city were you in on New Year's Eve, 1999?")) {
					webDriver.findElement(By.id("tlpvt-challenge-answer")).clear();
					webDriver.findElement(By.id("tlpvt-challenge-answer")).sendKeys("Krasnodar");
				} else {
					logger.error(question.getText());
					logger.error("Unable to recognize secret question");
					return null;
				}
				WebElement submit = webDriver.findElement(By.id("yes-recognize"));
				if (submit != null)
					submit.click();

				WebElement cont = webDriver.findElement(By.cssSelector("#verify-cq-submit > span"));
				if (cont != null)
					cont.click();
				else
					return null;
			}
		}

		mpTotal totalRow = totalsList.stream().filter(t -> t.getId().equals(account.getMyPortfolioId())).findFirst()
				.get();

		if (totalRow != null)
			return totalRow.getAmount();
		else {
			logger.error("Unable to fint total for {} by locator {}", account.getName(), account.getIsMyProtfolio());
			return null;
		}
	}

	class mpTotal {
		private String id;
		private String locator;
		private Double amount;

		public mpTotal(String id, String locator, Double amount) {
			this.id = id;
			this.locator = locator;
			this.amount = amount;
		}

		public String getId() {
			return id;
		}

		public String getLocator() {
			return locator;
		}

		public Double getAmount() {
			return amount;
		}

		@Override
		public String toString() {
			return "mpTotal [id=" + id + ", locator=" + locator + ", amount=" + amount + "]";
		}
	}

}
