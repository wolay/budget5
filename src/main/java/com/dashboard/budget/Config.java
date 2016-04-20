package com.dashboard.budget;

public interface Config {
	
	public int maxAttemptsToDownloadData = 3;
	public String dirOutputTotals = "/Users/andreianpilogov/Dropbox/Java/budget5/budget/src/output/totals/";
	public String dirOutputTransactions = "/Users/andreianpilogov/Dropbox/Java/budget5/budget/src/output/transactions/";
	public String summaryReceiver = "anpilogov.andrei@gmail.com";
	
	public int timeout = 30;
	public int nubmberOfThreads = 3;
	
	//runtime
	public boolean isRunningCreditScores = false;
	public boolean isRunningBankAccounts = true;
	public String bankAccountsFilter = "wf";
	

}
