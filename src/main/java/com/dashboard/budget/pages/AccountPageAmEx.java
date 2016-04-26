package com.dashboard.budget.pages;

import static com.dashboard.budget.Util.convertStringAmountToDouble;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.dashboard.budget.DataHandler;
import com.dashboard.budget.Util;
import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Transaction;

public class AccountPageAmEx extends AccountPage {

	public AccountPageAmEx(Account account, DataHandler dataHandler) {
		super(account);
	}

	@Override
	public Double getTotal() {
		String code = account.getCode();
		WebElement nav = null;
		// Blue Cash
		if (code.equals("121")) {
			amount = webDriver.findElement(By.id("ah-outstanding-balance"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Costco
		} else if (code.equals("122")) {
			Util.sleep(5000);
			nav = webDriver.findElement(By.id("iNavCSImg1"));
			if (nav != null)
				nav.click();
			else
				return null;
			amount = webDriver.findElement(By.id("ah-outstanding-balance"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Hilton
		} else if (code.equals("123")) {
			Util.sleep(5000);
			nav = webDriver.findElement(By.id("iNavCSImg2"));
			if (nav != null)
				nav.click();
			else
				return null;
			amount = webDriver.findElement(By.id("ah-outstanding-balance"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
			// Gold
		} else if (code.equals("124")) {
			amount = webDriver.findElement(By.id("ah-outstanding-balance"));
			return amount == null ? null : Util.wrapAmount(-convertStringAmountToDouble(amount.getText()));
		}

		return null;
	}

	@Override
	public List<Transaction> getTransactions(Double diff, List<Transaction> prevTransactions) {

		List<Transaction> result = new ArrayList<Transaction>();

		WebElement details = webDriver.findElement(By.xpath("//ul[@id='iNavMenu']/li[2]/a"));
		if (details != null)
			details.click();
		else
			return result;

		// waitForElement(wait,
		// By.xpath("//table[@id='listData']/tbody/tr/td"));
		List<WebElement> rows = webDriver.findElements(By.xpath("//table[@id='listData']/tbody/tr"));
		double amount;

		// make it different for each account so later could be converted into
		// common logic with config data
		String code = account.getCode();
		if ("121".equals(code)) // Blue Cash
		{
			for (WebElement row : rows) {
				if ("".equals(row.getText().trim()) || row.getText().trim().startsWith("There is no recent activity"))
					continue;
				amount = -Util.convertStringAmountToDouble(row.findElement(By.xpath("./td[7]/div")).getText());
				Date date = Util.convertStringToDateType3(row.findElement(By.xpath("./td[3]/div")).getText());
				result.add(new Transaction(code, date, row.findElement(By.xpath("./td[4]/div")).getText().trim(),
						amount, ""));
				diff = Util.roundDouble(diff - amount);
				if (diff == 0.0) {
					WebElement accounts = webDriver.findElement(By.id("MYCA_PC_Account_Summary2"));
					if (accounts != null)
						accounts.click();
					return result;
				}
			}
		} else if ("122".equals(code))// Costco
		{
			for (WebElement row : rows) {
				if ("".equals(row.getText().trim()) || row.getText().trim().startsWith("There is no recent activity"))
					continue;
				amount = -Util.convertStringAmountToDouble(row.findElement(By.xpath("./td[7]/div")).getText());
				Date date = Util.convertStringToDateType3(row.findElement(By.xpath("./td[3]/div")).getText());
				result.add(new Transaction(code, date, row.findElement(By.xpath("./td[4]/div")).getText().trim(),
						amount, ""));
				diff = Util.roundDouble(diff - amount);
				if (diff == 0.0) {
					WebElement accounts = webDriver.findElement(By.id("MYCA_PC_Account_Summary2"));
					if (accounts != null)
						accounts.click();
					return result;
				}
			}
		} else if ("123".equals(code))// Hilton
		{
			for (WebElement row : rows) {
				if ("".equals(row.getText().trim()) || row.getText().trim().startsWith("There is no recent activity"))
					continue;
				amount = -Util.convertStringAmountToDouble(row.findElement(By.xpath("./td[6]/div")).getText());
				Date date = Util.convertStringToDateType3(row.findElement(By.xpath("./td[3]/div")).getText());
				result.add(new Transaction(code, date, row.findElement(By.xpath("./td[4]/div")).getText().trim(),
						amount, ""));
				diff = Util.roundDouble(diff - amount);
				if (diff == 0.0) {
					WebElement accounts = webDriver.findElement(By.id("MYCA_PC_Account_Summary2"));
					if (accounts != null)
						accounts.click();
					return result;
				}
			}
		} else if ("124".equals(code)) // Gold
		{
			for (WebElement row : rows) {
				if ("".equals(row.getText().trim()) || row.getText().trim().startsWith("There is no recent activity"))
					continue;
				amount = -Util.convertStringAmountToDouble(row.findElement(By.xpath("./td[8]/div")).getText());
				Date date = Util.convertStringToDateType3(row.findElement(By.xpath("./td[4]/div")).getText());
				result.add(new Transaction(code, date, row.findElement(By.xpath("./td[5]/div")).getText().trim(),
						amount, ""));
				diff = Util.roundDouble(diff - amount);
				if (diff == 0.0) {
					WebElement accounts = webDriver.findElement(By.id("MYCA_PC_Account_Summary2"));
					if (accounts != null)
						accounts.click();
					return result;
				}
			}
		}

		WebElement periods = webDriver.findElement(By.id("periodSelect"));
		if (periods != null)
			periods.click();
		else
			return result;

		WebElement period = webDriver.findElement(By.xpath("//div[@id='tPeriodMenuContainer']/ul/li[2]/span[2]"));
		if (period != null)
			period.click();
		else
			return result;

		rows = webDriver.findElements(By.xpath("//table[@id='listData']/tbody/tr"));

		// make it different for each account so later could be converted into
		// common logic with config data
		if ("121".equals(code)) // Blue Cash
		{
			for (WebElement row : rows) {
				if ("".equals(row.getText().trim()))
					continue;
				amount = -Util.convertStringAmountToDouble(row.findElement(By.xpath("./td[7]/div")).getText());
				Date date = Util.convertStringToDateType3(row.findElement(By.xpath("./td[3]/div")).getText());
				result.add(new Transaction(code, date, row.findElement(By.xpath("./td[4]/div")).getText().trim(),
						amount, ""));
				diff = Util.roundDouble(diff - amount);
				if (diff == 0.0) {
					WebElement accounts = webDriver.findElement(By.id("MYCA_PC_Account_Summary2"));
					if (accounts != null)
						accounts.click();
					return result;
				}
			}
		} else if ("122".equals(code))// Costco
		{
			for (WebElement row : rows) {
				if ("".equals(row.getText().trim()))
					continue;
				amount = -Util.convertStringAmountToDouble(row.findElement(By.xpath("./td[7]/div")).getText());
				Date date = Util.convertStringToDateType3(row.findElement(By.xpath("./td[3]/div")).getText());
				result.add(new Transaction(code, date, row.findElement(By.xpath("./td[4]/div")).getText().trim(),
						amount, ""));
				diff = Util.roundDouble(diff - amount);
				if (diff == 0.0) {
					WebElement accounts = webDriver.findElement(By.id("MYCA_PC_Account_Summary2"));
					if (accounts != null)
						accounts.click();
					return result;
				}
			}
		} else if ("123".equals(code))// Hilton
		{
			for (WebElement row : rows) {
				if ("".equals(row.getText().trim()))
					continue;
				amount = -Util.convertStringAmountToDouble(row.findElement(By.xpath("./td[6]/div")).getText());
				Date date = Util.convertStringToDateType3(row.findElement(By.xpath("./td[3]/div")).getText());
				result.add(new Transaction(code, date, row.findElement(By.xpath("./td[4]/div")).getText().trim(),
						amount, ""));
				diff = Util.roundDouble(diff - amount);
				if (diff == 0.0) {
					WebElement accounts = webDriver.findElement(By.id("MYCA_PC_Account_Summary2"));
					if (accounts != null)
						accounts.click();
					return result;
				}
			}
		} else if ("124".equals(code)) // Gold
		{
			for (WebElement row : rows) {
				if ("".equals(row.getText().trim()))
					continue;
				amount = -Util.convertStringAmountToDouble(row.findElement(By.xpath("./td[8]/div")).getText());
				Date date = Util.convertStringToDateType3(row.findElement(By.xpath("./td[4]/div")).getText());
				result.add(new Transaction(code, date, row.findElement(By.xpath("./td[5]/div")).getText().trim(),
						amount, ""));
				diff = Util.roundDouble(diff - amount);
				if (diff == 0.0) {
					WebElement accounts = webDriver.findElement(By.id("MYCA_PC_Account_Summary2"));
					if (accounts != null)
						accounts.click();
					return result;
				}
			}
		}

		WebElement accounts = webDriver.findElement(By.id("MYCA_PC_Account_Summary2"));
		if (accounts != null)
			accounts.click();
		if (diff == 0.0)
			return result;
		else
			return new ArrayList<Transaction>();
	}

}
