
package com.dashboard.budget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.CreditScore;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;
import com.dashboard.budget.pages.AccountPage;
import com.dashboard.budget.pages.AccountPageAmEx;
import com.dashboard.budget.pages.AccountPageAmazon;
import com.dashboard.budget.pages.AccountPageBestBuy;
import com.dashboard.budget.pages.AccountPageMP;
import com.dashboard.budget.pages.AccountPageCiti;
import com.dashboard.budget.pages.AccountPageCreditKarma;
import com.dashboard.budget.pages.AccountPageJCPenney;
import com.dashboard.budget.pages.AccountPageKohls;
import com.dashboard.budget.pages.AccountPageMacys;
import com.dashboard.budget.pages.AccountPageNordstorm;
import com.dashboard.budget.pages.AccountPagePayPal;
import com.dashboard.budget.pages.AccountPageSaks;
import com.dashboard.budget.pages.AccountPageTjMaxx;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 *
 * @author aanpilogov
 */
public class WebDriverManager implements Config {

	private static Logger logger = LoggerFactory.getLogger(WebDriverManager.class);
	private DataHandler dataHandler;
	private AccountPage accountPage = null;

	public WebDriverManager(DataHandler dataHandler) {
		this.dataHandler = dataHandler;
	}

	private AccountPage getAccountPage(Account account) {
		switch (account.getBank().getName()) {
		case "mp":
			return new AccountPageMP(account, dataHandler);
		case "citi":
			return new AccountPageCiti(account, dataHandler);
		case "amex":
			return new AccountPageAmEx(account, dataHandler);
		case "pp":
			return new AccountPagePayPal(account, dataHandler);
		case "amz":
			return new AccountPageAmazon(account, dataHandler);
		case "nrd":
			return new AccountPageNordstorm(account, dataHandler);
		case "sks":
			return new AccountPageSaks(account, dataHandler);
		case "tjm":
			return new AccountPageTjMaxx(account, dataHandler);
		case "mcs":
			return new AccountPageMacys(account, dataHandler);
		case "bb":
			return new AccountPageBestBuy(account, dataHandler);
		case "khl":
			return new AccountPageKohls(account, dataHandler);
		case "jcp":
			return new AccountPageJCPenney(account, dataHandler);

		default:
			return null;
		}
	}

	private Double getDifference(Account account, Double amount, List<Total> prevTotals) {
		if (amount == null)
			return 0.00;

		Double prevTotal = prevTotals.stream().filter(t -> t.getAccount() == account).findFirst().get().getAmount();
		if (prevTotal == null)
			return null;
		else
			return Util.roundDouble(amount - prevTotal);

	}

	private void addTransactionsForDifference(Total total, AccountPage accountPage, Double difference,
			List<Transaction> prevTransactions) {
		try {
			List<Transaction> newTransactions;
			newTransactions = accountPage.getTransactions(total, prevTransactions);
			if (newTransactions.isEmpty()) {
				logger.error("No transactions found for difference");
			} else {
				for (Transaction newTransaction : newTransactions) {
					logger.info("{}, transaction: {}, {}, {}, {} ({})", accountPage.getAccount().getName(),
							newTransaction.getDate(), newTransaction.getDecription(), newTransaction.getAmount(),
							newTransaction.getCategory(), newTransaction.getCategoryStr());
				}
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}

	}

	public List<Total> getNewTotals(List<Account> accountsIn, List<Total> prevTotals,
			List<Transaction> prevTransactions) {
		List<Account> accounts;

		if (!isRunningBankAccounts) {
			logger.info("Running bank accounts skipped");
			return new ArrayList<Total>();
		}
		if (!bankAccountsFilter.equals(""))
			accountsIn = Util.getAccountsByDriver(accountsIn, bankAccountsFilter);

		// skipping bank account having totals for today
		accounts = Util.skipUpdatedBankAccounts(accountsIn, prevTotals);

		List<Total> result = new ArrayList<Total>();

		logger.info("Number of accounts to run: {}", accounts.size());

		// Extract list of drivers
		List<List<String>> drivers = new ArrayList<List<String>>();

		// Group accounts list by banks
		accounts.stream().forEach(a -> {
			List<String> driver = new ArrayList<String>();
			driver.add(a.getBank().getName());
			driver.add(a.getOwner());
			if (!drivers.contains(driver))
				drivers.add(driver);
		});

		ExecutorService executor = Executors.newFixedThreadPool(nubmberOfThreads,
				new ThreadFactoryBuilder().setNameFormat("%d").build());

		for (List<String> driver : drivers) {
			executor.submit(() -> {
				accounts.stream().filter(a -> a.getBank().equals(driver.get(0)) && a.getOwner().equals(driver.get(1))
						&& a.getIsEnabled()).forEach(account -> {
					Thread.currentThread().setName("Bank accounts ("
							+ Util.getThreadNumber(Thread.currentThread().getName()) + "): " + account.getName());
					int attempt = 0;
					Double amount = null;
					Total total = null;
					Double difference = null;
					boolean isDownloaded = false;
					while (!isDownloaded && attempt < maxAttemptsToDownloadData) {
						logger.info("{}: attempt #{}", account.getName(), ++attempt);
						if (accountPage == null) {
							accountPage = getAccountPage(account);
							accountPage.gotoHomePage();
							if (!accountPage.login()) {
								logger.error("Unsuccessful login to: {}", account.getName());
								accountPage.quit();
								accountPage = null;
								continue;
							}
						} else
							accountPage.setAccount(account);

						amount = accountPage.getTotal();
						if (amount != null) {
							difference = getDifference(account, amount, prevTotals);
							total = new Total(account, amount, difference, DataRetrievalStatus.OK);
							logger.info("{}, total: {}", account.getName(), amount);
							if (difference != null && difference != 0.00) {
								logger.info("{}, difference: {}", account.getName(), difference);
								addTransactionsForDifference(total, accountPage, difference, prevTransactions);
								// if no transactions found for difference
								// better not to save it but to wait next day
								if (total.getTransactions() == null)
									total = null;
							}
						} else {
							logger.error("Error while getting total for: {}", account.getName());
							accountPage.quit();
							accountPage = null;
							continue;
						}

						isDownloaded = true;
					}
					if (total != null)
						result.add(total);
				});
				accountPage.quit();
				accountPage = null;
			});
		}

		Collections.sort(result);
		Util.executorShutdown(executor);
		return result;
	}

	public List<CreditScore> getCreditScores(List<Account> accountsIn) {

		if (!isRunningCreditScores) {
			logger.info("Running credit scores skipped");
			return new ArrayList<CreditScore>();
		}

		Thread.currentThread().setName("Credit scores");

		List<CreditScore> result = new ArrayList<CreditScore>();
		dataHandler.getCreditScores().stream().forEach(s -> {
			if (Util.isDateToday(s.getDate()))
				result.add(s);
			else {
				Account account = s.getAccount();
				AccountPage accountPage = new AccountPageCreditKarma(account, dataHandler);
				accountPage.gotoHomePage();
				accountPage.login();

				int score = accountPage.getScore();
				accountPage.quit();

				result.add(new CreditScore(account, account.getOwner(), score, score - s.getScore()));
				logger.info("New credit score {}: {}", account.getOwner(), score);
			}
		});

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
