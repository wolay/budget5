package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import org.openqa.selenium.By;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;

public class AccountPageJCPenney extends AccountPage {

	public AccountPageJCPenney(Account account, DataHandler dataHandler) {
		super(account, dataHandler);
	}

}
