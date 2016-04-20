
package com.dashboard.budget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.CreditScore;
import com.dashboard.budget.DAO.DataRetrievalStatus;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;
import com.dashboard.budget.pages.AccountPage;
import com.dashboard.budget.pages.AccountPageAmEx;
import com.dashboard.budget.pages.AccountPageAmazon;
import com.dashboard.budget.pages.AccountPageBestBuy;
import com.dashboard.budget.pages.AccountPageCapOne;
import com.dashboard.budget.pages.AccountPageChase;
import com.dashboard.budget.pages.AccountPageCiti;
import com.dashboard.budget.pages.AccountPageCreditKarma;
import com.dashboard.budget.pages.AccountPageJCPenney;
import com.dashboard.budget.pages.AccountPageKohls;
import com.dashboard.budget.pages.AccountPageMacys;
import com.dashboard.budget.pages.AccountPageNordstorm;
import com.dashboard.budget.pages.AccountPageSaks;
import com.dashboard.budget.pages.AccountPageTjMaxx;
import com.dashboard.budget.pages.AccountPageWF;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 *
 * @author aanpilogov
 */
public class WebDriverManager implements Config {

	private static Logger logger = LoggerFactory.getLogger(WebDriverManager.class);
	private DataHandler dataHandler;

	public WebDriverManager(DataHandler dataHandler) {
		this.dataHandler = dataHandler;
	}

	private AccountPage getAccountPage(Account account) {
		switch (account.getCode()) {
		case "101":
			return new AccountPageWF(account, dataHandler);
		case "102":
			return new AccountPageWF(account, dataHandler);
		case "103":
			return new AccountPageWF(account, dataHandler);
		case "111":
			return new AccountPageCiti(account, dataHandler);
		case "112":
			return new AccountPageCiti(account, dataHandler);
		case "113":
			return new AccountPageCiti(account, dataHandler);
		case "121":
			return new AccountPageAmEx(account, dataHandler);
		case "122":
			return new AccountPageAmEx(account, dataHandler);
		case "124":
			return new AccountPageAmEx(account, dataHandler);
		case "131":
			return new AccountPageChase(account, dataHandler);
		case "132":
			return new AccountPageChase(account, dataHandler);
		case "191":
			return new AccountPageCapOne(account, dataHandler);
		case "201":
			return new AccountPageAmazon(account, dataHandler);
		case "203":
			return new AccountPageNordstorm(account, dataHandler);
		case "205":
			return new AccountPageSaks(account, dataHandler);
		case "207":
			return new AccountPageTjMaxx(account, dataHandler);
		case "208":
			return new AccountPageTjMaxx(account, dataHandler);
		case "209":
			return new AccountPageMacys(account, dataHandler);
		case "211":
			return new AccountPageBestBuy(account, dataHandler);
		case "213":
			return new AccountPageKohls(account, dataHandler);
		case "215":
			return new AccountPageJCPenney(account, dataHandler);

		default:
			return null;
		}
	}

	private Double getDifference(Account account, Double amount, Map<String, Double> prevTotals) {
		if (amount == null)
			return 0.00;

		Double prevTotal = prevTotals.get(account.getCode());
		if (prevTotal == null)
			return null;
		else
			return Util.roundDouble(amount - prevTotal);

	}

	private void addTransactionsForDifference(List<Transaction> transactions, AccountPage accountPage,
			Double difference, List<Transaction> prevTransactions) {
		List<Transaction> newTransactions = accountPage.getTransactions(difference, prevTransactions);
		if (newTransactions.isEmpty()) {
			logger.error("No transactions found for difference");
		}else{
			for (Transaction newTransaction : newTransactions) {
				logger.info("{}, transaction: {}, {}, {}", accountPage.getAccount().getName(), newTransaction.getDate(),
						newTransaction.getDecription(), newTransaction.getAmount());
			}
			transactions.addAll(newTransactions);
		}
	}

	public List<Total> getNewTotals(List<Account> accountsIn, List<Transaction> transactions,
			Map<String, Double> prevTotals, List<Transaction> prevTransactions) {
		List<Account> accounts;
		
		if(!isRunningBankAccounts){
			logger.info("Running bank accounts skipped");
			return new ArrayList<Total>();
		}
		if(bankAccountsFilter.equals(""))
			accounts = accountsIn;
		else 
			accounts = Util.getAccountsByDriver(accountsIn, bankAccountsFilter);

		List<Total> result = new ArrayList<Total>();
		ExecutorService executor = Executors.newFixedThreadPool(1);

		logger.info("Number of accounts to run: {}", accounts.size());

		// Extract list of drivers
		List<List<String>> drivers = new ArrayList<List<String>>();

		for (Account account : accounts) {
			//if (account.getPriority() != 2)
			//	continue;
			List<String> driver = new ArrayList<String>();
			driver.add(account.getBank());
			driver.add(account.getOwner());
			if (!drivers.contains(driver))
				drivers.add(driver);
		}

		executor = Executors.newFixedThreadPool(nubmberOfThreads, new ThreadFactoryBuilder().setNameFormat("Bank accounts (thread %d)").build());

		for (List<String> driver : drivers) {
			executor.submit(() -> {
				AccountPage accountPage = null;
				for (Account account : Util.getAccountsByDriver(accounts, driver.get(0), driver.get(1))) {
					Thread.currentThread().setName("Bank accounts: " + account.getName());
					int attempt = 0;
					Double amount = null;
					Double difference = null;
					boolean isDownloaded = false;
					while (!isDownloaded && attempt < maxAttemptsToDownloadData) {
						logger.info("{}: attempt #{}", account.getName(), ++attempt);
						if (accountPage == null) {
							accountPage = getAccountPage(account);
							accountPage.gotoHomePage();
							if(!accountPage.login()){
								logger.error("Unsuccessful login to: {}", account.getName());
								accountPage.quit();
								accountPage = null;
								continue;
							}
						} else
							accountPage.setAccount(account);

						amount = accountPage.getTotal();
						if(amount!=null)
							logger.info("{}, total: {}", account.getName(), amount);
						else{
							logger.error("Error while getting total for: {}", account.getName());
							accountPage.quit();
							accountPage = null;
							continue;
						}

						difference = getDifference(account, amount, prevTotals);
						if (difference != null && difference != 0.00) {
							logger.info("{}, difference: {}", account.getName(), difference);
							addTransactionsForDifference(transactions, accountPage, difference, prevTransactions);
						}
						isDownloaded = true;
					}
					result.add(new Total(account, amount, difference,
							(amount != null) ? DataRetrievalStatus.OK : DataRetrievalStatus.FAILED));
				}
				accountPage.quit();
			});
		}

		Collections.sort(result);
		Util.executorShutdown(executor);
		return result;
	}

	public List<CreditScore> getCreditScores() {
		
		if(!isRunningCreditScores){
			logger.info("Running credit scores skipped");
			return new ArrayList<CreditScore>();
		}		
		
		Thread.currentThread().setName("Credit scores");

		List<CreditScore> result = new ArrayList<CreditScore>();

		for (Account account : dataHandler.getCreditScoreList()) {
			AccountPage accountPage = new AccountPageCreditKarma(account, dataHandler);
			accountPage.gotoHomePage();
			accountPage.login();

			int score = accountPage.getScore();
			result.add(new CreditScore(account.getOwner(), score));
			logger.info("Credit score {}: {}", account.getOwner(), score);

			accountPage.quit();
		}

		return result;
	}

	public boolean isOnline() {
		logger.info("Network check...");
		WebDriver webDriver = new HtmlUnitDriver();
		webDriver.get("https://www.google.com");
		try {
			if (webDriver.findElements(By.cssSelector("#hplogo")).size() > 0) {
				webDriver.quit();
				logger.info("Network check - OK!");
				return true;
			}
		} catch (Exception e) {
		}
		webDriver.quit();
		logger.info("Network check - FAIL!");
		return false;
	}
}
