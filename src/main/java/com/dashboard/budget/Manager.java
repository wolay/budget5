package com.dashboard.budget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.CreditScore;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class Manager {

	private static Logger logger = LoggerFactory.getLogger(Manager.class);
	private static DataHandler dataHandler = new DataHandler();
	private static WebDriverManager webDriverManager = new WebDriverManager(dataHandler);
	
	private static List<Total> totals;
	private static List<Transaction> newTransactions;
	private static List<Transaction> prevTransactions;
	private static List<CreditScore> creditScores;

	public static void main(String[] args){
		if(!webDriverManager.isOnline())
			return;			
		
		logger.info("Data collecting started");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		// Getting all balances
		Thread t1 = new Thread() {
			public void run() {
				Thread.currentThread().setName("Bank accounts");
				newTransactions= new ArrayList<Transaction>();
				// Previous balances & transactions
				Map<String, Double> prevTotals = Util.getPrevTotals();
				List<Transaction> prevTransactions = Util.getPrevTransactions();
						
				// New balances & transactions
				List<Account> accounts = dataHandler.getBankAccountsList();
				totals = webDriverManager.getNewTotals(accounts, newTransactions, prevTotals, prevTransactions);
				logger.info("automation total: {}", Util.roundDouble(DataHandler.getFullTotal(totals)));
				
				// Adding skipped accounts
				dataHandler.addSkippedAccounts(accounts, totals);
			}
		};

		// Getting credit scores
		Thread t2 = new Thread() {
			public void run() {
				creditScores = webDriverManager.getCreditScores();
			}
		};

		t1.start();
		t2.start();

		try {
			t1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		
		logger.info(stopWatch.toString());

		// Sending summary to email
		Util.sendEmailSummary(totals, newTransactions, creditScores, stopWatch.toString());
		
		// Saving to files
		Util.writeTotalsToFile(totals);
		newTransactions.addAll(prevTransactions);
		Util.writeTransactionsToFile(newTransactions);
	}
}
