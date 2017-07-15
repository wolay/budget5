package com.dashboard.budget;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Credential;
import com.dashboard.budget.DAO.CreditScore;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;

public class Manager {

	private static Logger logger = LoggerFactory.getLogger(Manager.class);
	private static DataHandler dataHandler = new DataHandler();
	private static WebDriverManager webDriverManager = new WebDriverManager(dataHandler);
	
	private static List<Account> accounts;
	private static List<Total> prevTotals;
	private static List<Total> newTotals;
	private static List<Transaction> prevTransactions;
	private static List<CreditScore> creditScores;

	public static void main(String[] args){
		if(!webDriverManager.isOnline())
			System.exit(0);			
		
		logger.info("Data collecting started...");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		// Getting all balances
		Thread t1 = new Thread() {
			public void run() {
				Thread.currentThread().setName("Bank accounts");				
				// Previous balances & transactions
				accounts = dataHandler.getBankAccountsList();
				prevTotals = dataHandler.getPrevTotals();
				prevTransactions = dataHandler.getPrevTransactions();
						
				// New balances & transactions
				newTotals = webDriverManager.getNewTotals(accounts, prevTotals, prevTransactions);
			}
		};
		t1.start();

		// Getting credit scores
		Thread t2 = new Thread() {
			public void run() {
				creditScores = webDriverManager.getCreditScores(dataHandler.getCreditScoreList());
			}
		};
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
		
		// Saving
		logger.info("Saving new data to DB...");
		dataHandler.saveNewTotalsWithTransactionsToDb(newTotals);
		dataHandler.saveCreditScoresToDb(creditScores);
		dataHandler.calculateBudgetSummary();
		dataHandler.saveAnnualTarget();
		
		// Sending summary to email
		Credential mailCredentials = dataHandler.getCredentials().stream().filter(c -> c.getName().equals("mailru")).findFirst().get();
		new Reporter(dataHandler).sendEmailSummary(stopWatch.toString(), mailCredentials);

		System.exit(0);
		
	}
}
