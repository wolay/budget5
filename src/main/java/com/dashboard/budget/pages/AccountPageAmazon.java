package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Field;
import com.dashboard.budget.DAO.PageElementNotFoundException;
import com.dashboard.budget.DAO.TableRow;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;

public class AccountPageAmazon extends AccountPage {

	private Field balanceStrDol;
	private Field balanceStrCen;

	public AccountPageAmazon(Account account, DataHandler dataHandler) {
		super(account, dataHandler);

		balanceStrDol = new Field("amount dollars", accountTotalDetails.getBalanceDolLocator(), getWebdriver());
		balanceStrCen = new Field("amount cents", accountTotalDetails.getBalanceCenLocator(), getWebdriver());
	}


	public synchronized boolean login() {
		if (Util.checkIfSiteDown(webDriver))
			return false;
		try {
			fldUsername.setText(valUsername);
			fldPassword.setText(valPassword);
			btnLogin.click();
			return true;
		} catch (PageElementNotFoundException e) {
			return false;
		}
	}

	public Double getTotal() {
		try {
			return Util.wrapAmount(-convertStringAmountToDouble(
					balanceStrDol.getText() + balanceStrCen.getText().replace("·", ".").replace("*", "")));
		} catch (PageElementNotFoundException e) {
			return null;
		}
	}
	
	public List<Transaction> getTransactions(Total total, List<Transaction> prevTransactions)
			throws PageElementNotFoundException {

		List<Transaction> result = new ArrayList<Transaction>();

		// Navigating to the page with transactions
		btnTransactionsPage.click();

		Double difference = total.getDifference();

		// CURRENT PERIOD TRANSACTIONS
		List<WebElement> currentPeriodRows = webDriver.findElements(accountTransactionDetails.getTransTableLocator());
		if (currentPeriodRows == null)
			logger.info("No rows found in the current period table");
		else {
			logger.info("Rows in the current period table: {}", currentPeriodRows.size());
			for (WebElement row : currentPeriodRows) {
				logger.info("Row in the current period table: {}", row.getText());
				if (Util.isPending(row.getText()))
					continue;

				// Parsing row
				TableRow tr = new TableRow("transaction row", By.xpath(accountTransactionDetails.getTransDateLocator()),
						accountTransactionDetails.getTransDateFormat(),
						By.xpath(accountTransactionDetails.getTransAmountLocator()),
						By.xpath(accountTransactionDetails.getTransDescriptionLocator()),
						(accountTransactionDetails.getTransCategoryLocator() == null) ? null
								: By.xpath(accountTransactionDetails.getTransCategoryLocator()),
						row);

				if (!isTransactionExist(prevTransactions, tr.getDate(), -tr.getAmount())) {

					result.add(new Transaction(account, total, tr.getDate(), tr.getDescription(), -tr.getAmount(),
							tr.getCategory(), null));

					// Refreshing remaining difference
					difference = Util.roundDouble(difference + tr.getAmount());
					logger.info("Amount: {}, diff: {}", -tr.getAmount(), difference);
					if (difference == 0.0) {
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
						row);

				if (!isTransactionExist(prevTransactions, tr.getDate(), tr.getAmount())
						&& !isTransactionExist(result, tr.getDate(), tr.getAmount())) {

					result.add(new Transaction(account, total, tr.getDate(), tr.getDescription(), tr.getAmount(),
							tr.getCategory(), null));

					// Refreshing remaining difference
					difference = Util.roundDouble(difference - tr.getAmount());
					logger.info("Amount: {}, diff: {}", tr.getAmount(), difference);
					if (difference == 0.0) {
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
	
	public void quit() {
		try {
			btnLogout.click();
			logger.info("Account page {} was closed", account.getName());
		} catch (PageElementNotFoundException e) {
			logger.error("Account page {} was not closed properly", account.getName());
		}

		webDriver.quit();		
	}
}
