
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
import com.dashboard.budget.pages.AccountPageBoA;
import com.dashboard.budget.pages.AccountPageCapOne;
import com.dashboard.budget.pages.AccountPageChase;
import com.dashboard.budget.pages.AccountPageCiti;
import com.dashboard.budget.pages.AccountPageCreditKarma;
import com.dashboard.budget.pages.AccountPageJCPenney;
import com.dashboard.budget.pages.AccountPageKohls;
import com.dashboard.budget.pages.AccountPageMacys;
import com.dashboard.budget.pages.AccountPageNordstorm;
import com.dashboard.budget.pages.AccountPagePayPal;
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
		switch (account.getId()) {
		case 101:
			return new AccountPageWF(account, dataHandler);
		case 102:
			return new AccountPageWF(account, dataHandler);
		case 103:
			return new AccountPageWF(account, dataHandler);
		case 111:
			return new AccountPageCiti(account, dataHandler);
		case 112:
			return new AccountPageCiti(account, dataHandler);
		case 113:
			return new AccountPageCiti(account, dataHandler);
		case 121:
			return new AccountPageAmEx(account, dataHandler);
		case 122:
			return new AccountPageAmEx(account, dataHandler);
		case 124:
			return new AccountPageAmEx(account, dataHandler);
		case 131:
			return new AccountPageChase(account, dataHandler);
		case 132:
			return new AccountPageChase(account, dataHandler);
		case 141:
			return new AccountPageBoA(account, dataHandler);
		case 181:
			return new AccountPagePayPal(account, dataHandler);
		case 191:
			return new AccountPageCapOne(account, dataHandler);
		case 201:
			return new AccountPageAmazon(account, dataHandler);
		case 203:
			return new AccountPageNordstorm(account, dataHandler);
		case 205:
			return new AccountPageSaks(account, dataHandler);
		case 207:
			return new AccountPageTjMaxx(account, dataHandler);
		case 208:
			return new AccountPageTjMaxx(account, dataHandler);
		case 209:
			return new AccountPageMacys(account, dataHandler);
		case 211:
			return new AccountPageBestBuy(account, dataHandler);
		case 213:
			return new AccountPageKohls(account, dataHandler);
		case 215:
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

	private void addTransactionsForDifference(Total total, List<Transaction> transactions, AccountPage accountPage,
			Double difference, List<Transaction> prevTransactions) {
		List<Transaction> newTransactions = accountPage.getTransactions(total, prevTransactions);
		if (newTransactions.isEmpty()) {
			logger.error("No transactions found for difference");
		} else {
			for (Transaction newTransaction : newTransactions) {
				logger.info("{}, transaction: {}, {}, {}, {} ({})", accountPage.getAccount().getName(),
						newTransaction.getDate(), newTransaction.getDecription(), newTransaction.getAmount(),
						newTransaction.getCategory(), newTransaction.getCategoryStr());
			}
			transactions.addAll(newTransactions);
		}
	}

	public List<Total> getNewTotals(List<Account> accountsIn, List<Transaction> transactions, List<Total> prevTotals,
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

		// Sort accounts list by priority
		Collections.sort(accounts, (a1, a2) -> a1.priority.compareTo(a2.priority));
		for (Account account : accounts) {
			List<String> driver = new ArrayList<String>();
			driver.add(account.getBank());
			driver.add(account.getOwner());
			if (!drivers.contains(driver))
				drivers.add(driver);
		}

		ExecutorService executor = Executors.newFixedThreadPool(nubmberOfThreads,
				new ThreadFactoryBuilder().setNameFormat("%d").build());

		for (List<String> driver : drivers) {
			executor.submit(() -> {
				AccountPage accountPage = null;
				for (Account account : Util.getAccountsByDriver(accounts, driver.get(0), driver.get(1))) {
					Thread.currentThread().setPriority(account.getPriority());
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
								addTransactionsForDifference(total, transactions, accountPage, difference,
										prevTransactions);
							}
						} else {
							logger.error("Error while getting total for: {}", account.getName());
							accountPage.quit();
							accountPage = null;
							continue;
						}

						isDownloaded = true;
					}
					result.add(total);
				}
				accountPage.quit();
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
