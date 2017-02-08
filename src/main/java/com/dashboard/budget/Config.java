package com.dashboard.budget;

public interface Config {
	
	public int maxAttemptsToDownloadData = 3;
	public String summaryReceiver = "anpilogov.andrei@gmail.com";
	
	public int timeout = 30;
	public int nubmberOfThreads = 1;
	
	//runtime
	public boolean isRunningCreditScores = true;
	public boolean isRunningBankAccounts = true;
	public String bankAccountsFilter = "";
	
}
