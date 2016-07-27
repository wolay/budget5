package com.dashboard.budget;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.CreditScore;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;

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
			System.exit(0);			
		
		logger.info("Data collecting started");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		// Getting all balances
		Thread t1 = new Thread() {
			public void run() {
				Thread.currentThread().setName("Bank accounts");				
				newTransactions= new ArrayList<Transaction>();
				// Previous balances & transactions
				List<Account> accounts = dataHandler.getBankAccountsList();
				//List<Total> prevTotals = Util.getPrevTotals(accounts);
				List<Total> prevTotals = dataHandler.getPrevTotals();
				//prevTransactions = dataHandler.get Util.getPrevTransactions(dataHandler.getBankAccountsList());
				prevTransactions = dataHandler.getPrevTransactions();
						
				// New balances & transactions
				totals = webDriverManager.getNewTotals(accounts, newTransactions, prevTotals, prevTransactions);
				logger.info("automation total: {}", Util.roundDouble(DataHandler.getFullTotal(totals)));

			}
		};
		t1.start();

		// Getting credit scores
		Thread t2 = new Thread() {
			public void run() {
				creditScores = webDriverManager.getCreditScores();
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

		// Sending summary to email
		Util.sendEmailSummary(totals, creditScores, stopWatch.toString());
		
		// Saving to files
		//Util.writeTotalsToFile(totals);
		dataHandler.saveNewTotalsToDb(totals);
		dataHandler.saveNewTransactionsToDb(newTransactions);
		//newTransactions.addAll(prevTransactions);
		//Util.writeTransactionsToFile(newTransactions);
		System.exit(0);
		
	}
}
