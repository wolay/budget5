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

		// first check if table of totals already captured
		if (totalsList == null) {
			logger.info("Capturing all totals on 'My Portfolio' page...");
			// secret question
			if (Util.isSecretQuestionShown(webDriver))
				if (!super.answerSecretQuestion())
					return null;

			// navigate to MyPortfolio page if it's not there yet
			if (!webDriver.getWebDriver().getTitle().contains("My Portfolio")) {
				gotoMyPortfolioPage();
			}

			totalsList = new ArrayList<mpTotal>();

			// Waiting for table to refresh
			WebElement refreshStatus = webDriver.findElement(By.xpath("//a[@id='refresh']"));
			if (refreshStatus == null) {
				logger.error("Cannot find refresh status locator");
				return null;
			}

			if (refreshStatus.getText().startsWith("Refreshing")) {
				logger.info("Waiting for refreshing accounts table...");
				while (refreshStatus.getText().startsWith("Refreshing")) {
					Util.sleep(5000);
					refreshStatus = webDriver.findElement(By.xpath("//a[@id='refresh']"));
				}
				
				// Needs to come again on 'My Portfolio' page
				// otherwise updated totals not reflected in table				
				webDriver.getWebDriver().navigate().back();
				Util.sleep(3000);
				gotoMyPortfolioPage();
				logger.info("All My Portfolio accounts are up to date");
			}

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

			logger.info("Accounts total (parsed on page):");
			totalsList.stream().forEach(t -> logger.info(t.toString()));

			// return to the main page (from where Transactions will be opened)
			webDriver.getWebDriver().navigate().back();
		}

		mpTotal totalRow = totalsList.stream().filter(t -> t.getId().contains(account.getMyPortfolioId())).findFirst().orElse(null);

		if (totalRow != null)
			return totalRow.getAmount();
		else {
			logger.error("Unable to find total for {} by locator {}", account.getName(), account.getIsMyProtfolio());
			return null;
		}
	}

	private void gotoMyPortfolioPage() {
		logger.info("Navigating to 'My Portfolio' page...");
		webDriver.switchTo().defaultContent();
		Actions action = new Actions(webDriver.getWebDriver());
		WebElement we = webDriver.findElement(By.name("onh_tools_and_investing"));
		if (we != null)
			action.moveToElement(we).build().perform();
		WebElement submit1 = webDriver.findElement(By.name("onh_tools_and_investing_my_portfolio"));
		if (submit1 != null)
			submit1.click();
		webDriver.waitFrameToBeAvailableAndSwitchToIt("htmlhelp");
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
