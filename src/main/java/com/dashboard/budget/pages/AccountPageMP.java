package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.DataRetrievalStatus;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;
import com.dashboard.budget.UI.Button;
import com.dashboard.budget.UI.Field;
import com.dashboard.budget.UI.PageElementNotFoundException;
import com.dashboard.budget.UI.TableRow;

public class AccountPageMP extends AccountPage {

	private List<mpTotal> totalsList;
	private Field fldRefreshStatus;
	private Button btnToolsAndTransactions;
	private Button btnTransactions;
	private Button btnAccountSelect;

	public AccountPageMP(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}
	
	public void refreshLocators(){
		super.refreshLocators();
		
		fldRefreshStatus = new Field("refresh status", By.xpath("//a[@id='refresh']"), getWebdriver(), getWebdriver());
		btnToolsAndTransactions = new Button("tools and transactions", By.name("onh_tools_and_investing"), getWebdriver(), getWebdriver());
		btnTransactions = new Button("transactions", By.linkText("Transactions"), getWebdriver(), getWebdriver());
		btnAccountSelect = new Button("account select", By.id("dropdown_itemAccountId"), getWebdriver(), getWebdriver());
	}

	public DataRetrievalStatus login() {
		if (Util.checkIfSiteDown(webDriver))
			return DataRetrievalStatus.SERVICE_UNAVAILABLE;
		try {
			fldUsername.setText(valUsername);
			fldPassword.setText(valPassword);
			btnLogin.click();
			return DataRetrievalStatus.SUCCESS;
		} catch (PageElementNotFoundException e) {
			return DataRetrievalStatus.NAVIGATION_BROKEN;
		}
	}

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
				if (!gotoMyPortfolioPage()) {
					logger.error("Cannot navigate to 'My Portfolio' page");
					return null;
				}
			}

			totalsList = new ArrayList<mpTotal>();

			try{
			// Waiting for table to refresh
			if (fldRefreshStatus.getText().startsWith("Refreshing")) {
				logger.info("Waiting for refreshing accounts table...");
				while (fldRefreshStatus.getText().startsWith("Refreshing")) {
					Util.sleep(5000);
					fldRefreshStatus.setWebElement(null);
				}

				// Needs to come again on 'My Portfolio' page
				// otherwise updated totals not reflected in table
				webDriver.getWebDriver().navigate().back();
				Util.sleep(3000);
				if (!gotoMyPortfolioPage()) {
					logger.error("Cannot navigate to 'My Portfolio' page");
					return null;
				}
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
			
			} catch (PageElementNotFoundException e) {
				return null;
			}
		}

		mpTotal totalRow = totalsList.stream().filter(t -> t.getId().contains(account.getMyPortfolioId())).findFirst()
				.orElse(null);

		if (totalRow != null)
			return totalRow.getAmount();
		else {
			logger.error("Unable to find total for {} by id '{}'", account.getName(), account.getMyPortfolioId());
			webDriver.takeScreenshot();
			return null;
		}
	}
	
	public List<Transaction> getTransactions(Total total, List<Transaction> prevTransactions) throws PageElementNotFoundException {

		List<Transaction> result = new ArrayList<Transaction>();
		getWebdriver().getWebDriver().switchTo().defaultContent();

			if (!webDriver.getWebDriver().getTitle().contains("Transaction")) {
				btnToolsAndTransactions.clickAsAction();
				btnTransactions.click();
			}

			// select account from dropdown list
			try{
			btnAccountSelect.clickIfAvailable();
			}catch(Exception e){}

			List<WebElement> accounts = webDriver.findElements(By.className(" groupItem"));
			if(accounts == null){
				logger.error("Unable to fetch list of accounts");
				webDriver.takeScreenshot();
				return result;
			}
			WebElement weAccount = accounts.stream()
					.filter(a -> !a.getText().equals("") && a.getText().contains(account.getMyPortfolioId()))
					.findFirst().orElse(null);
			if (weAccount != null)
				webDriver.clickElementWithAction(weAccount);

			// select period for 1 month
			WebElement periodList = webDriver.findElement(By.id("dropdown_dateRangeId"));
			if (periodList != null) {
				if (!periodList.getText().equals("1 month")) {
					periodList.click();
					WebElement periodItem = webDriver.findElement(By.id("custom_multi_select_2_dateRangeId"));
					if (periodItem != null)
						periodItem.click();
				}
			}

		Double difference = total.getDifference();

		// CURRENT PERIOD TRANSACTIONS
		List<WebElement> currentPeriodRows = webDriver.findElements(accountTransactionDetails.getTransTableLocator());
		if (currentPeriodRows == null)
			logger.info("No rows found in the current period table");
		else {
			logger.info("Rows in the current period table: {}", currentPeriodRows.size());
			for (WebElement row : currentPeriodRows) {
				logger.info("Row in the current period table: {}", row.getText().replaceAll("\\n", "").replaceAll("\\r", ""));
				if (Util.isPending(row.getText()))
					continue;

				// Parsing row
				TableRow tr = new TableRow("transaction row", By.xpath(accountTransactionDetails.getTransDateLocator()),
						accountTransactionDetails.getTransDateFormat(),
						By.xpath(accountTransactionDetails.getTransAmountLocator()),
						By.xpath(accountTransactionDetails.getTransDescriptionLocator()),
						(accountTransactionDetails.getTransCategoryLocator() == null) ? null
								: By.xpath(accountTransactionDetails.getTransCategoryLocator()),
						row, getWebdriver());

				if (!isTransactionExist(prevTransactions, tr.getDate(), tr.getAmount())) {
					
					result.add(new Transaction(account, total, tr.getDate(), tr.getDescription(), tr.getAmount(),
							tr.getCategory(), null));

					// Refreshing remaining difference
					difference = Util.roundDouble(difference - tr.getAmount());
					logger.info("Amount: {}, diff: {}", tr.getAmount(), difference);
					if (difference == 0.0) {
						if (account.getIsMyProtfolio())
							webDriver.getWebDriver().navigate().back();

						result.stream().forEach(t -> total.addTransactions(t));
						return result;
					}
				}
			}
		}

		// PREVIOUS PERIOD TRANSACTIONS
		List<WebElement> previousPeriodRows;
		// lets see how it will go... not many accounts reach that point
		// for some accounts previous period transactions are not considered
		// (i.e. WF)
		if (accountNavigationDetails == null || accountNavigationDetails.getPeriodSwitchLocator() == null) {
			if (account.getIsMyProtfolio())
				webDriver.getWebDriver().navigate().back();
			return new ArrayList<Transaction>();
		} else {
			swtPeriod.perform();

			// Wait for previous transactions table to be loaded
			Util.sleep(5000);
			
			previousPeriodRows = webDriver.findElements(accountTransactionDetails.getTransTableLocator());
			logger.info("Rows in the previous period table: {}", previousPeriodRows.size());

			for (WebElement row : previousPeriodRows) {
				if (Util.isPending(row.getText()))
					continue;

				// Parsing row
				TableRow tr = new TableRow("transaction row", By.xpath(accountTransactionDetails.getTransDateLocator()),
						accountTransactionDetails.getTransDateFormat(),
						By.xpath(accountTransactionDetails.getTransAmountLocator()),
						By.xpath(accountTransactionDetails.getTransDescriptionLocator()),
						(accountTransactionDetails.getTransCategoryLocator() == null) ? null
								: By.xpath(accountTransactionDetails.getTransCategoryLocator()),
						row, getWebdriver());

				if (!isTransactionExist(prevTransactions, tr.getDate(), tr.getAmount())
						&& !isTransactionExist(result, tr.getDate(), tr.getAmount())) {
					
					result.add(new Transaction(account, total, tr.getDate(), tr.getDescription(), tr.getAmount(),
							tr.getCategory(), null));

					// Refreshing remaining difference
					difference = Util.roundDouble(difference - tr.getAmount());
					logger.info("Amount: {}, diff: {}", tr.getAmount(), difference);
					if (difference == 0.0) {
						if (account.getIsMyProtfolio())
							webDriver.getWebDriver().navigate().back();

						result.stream().forEach(t -> total.addTransactions(t));
						return result;
					}
				}
			}
		}

		if (difference == 0.0)
			return result;
		else
			return new ArrayList<Transaction>();
	}

	private boolean gotoMyPortfolioPage() {
		logger.info("Navigating to 'My Portfolio' page...");
		webDriver.switchTo().defaultContent();
		Actions action = new Actions(webDriver.getWebDriver());

		WebElement we = webDriver.findElement(By.name("onh_tools_and_investing"));
		if (we == null)
			return false;
		else
			action.moveToElement(we).build().perform();

		WebElement submit1 = webDriver.findElement(By.name("onh_tools_and_investing_my_portfolio"));
		if (submit1 == null)
			return false;
		else
			submit1.click();
		webDriver.waitFrameToBeAvailableAndSwitchToIt("htmlhelp");

		return true;
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

	public void quit() {
		try {
			btnLogout.click();
			logger.info("Account page {} was closed", account.getName());
		} catch (PageElementNotFoundException | WebDriverException e) {
			logger.error("Account page {} was not closed properly", account.getName());
		}

		webDriver.quit();
	}
}
