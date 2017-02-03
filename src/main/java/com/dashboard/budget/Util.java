/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dashboard.budget;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Category;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;

/**
 *
 * @author aanpilogov
 */
public class Util implements Config {

	private static Logger logger = LoggerFactory.getLogger(Util.class);

	@SuppressWarnings("deprecation")
	public static Date convertStringToDateByType(String string, int type) {
		string = string.replace("\n", " ");
		try {
			switch (type) {
			case 0:
				return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(string.trim());
			case 1:
				return new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH).parse(string.trim());
			case 2:
				return new SimpleDateFormat("MMM. dd, yyyy", Locale.ENGLISH).parse(string.trim());
			case 3:
				return new SimpleDateFormat("MMMdd yyyy", Locale.ENGLISH)
						.parse(string.trim() + " " + Calendar.getInstance().get(Calendar.YEAR));
			case 4:
				return new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).parse(string.trim());
			case 5:
				return new SimpleDateFormat("MMMMM dd, yyyy", Locale.ENGLISH).parse(string.trim());
			case 6:
				return new SimpleDateFormat("MMMMM dd yyyy", Locale.ENGLISH).parse(string.trim());
			case 7:
				return new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).parse(string.trim());
			case 8:
				return new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
						.parse(string.trim() + ", " + new Date().getYear());
			}
		} catch (ParseException ex) {
			logger.error(ex.getMessage());
		}
		return null;
	}

	public static String convertDateToStringType1(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}

	public static String convertDateToStringType2(Date date) {
		return new SimpleDateFormat("MM/dd").format(date);
	}

	public static String convertDateToStringType3(Date date) {
		return new SimpleDateFormat("yyyy-MM").format(date);
	}

	public static java.sql.Timestamp getTimestamp() {
		return new java.sql.Timestamp(System.currentTimeMillis());
	}

	public static double convertStringAmountToDouble(String stringAmount) {
		String out = stringAmount.replace("+", "");
		out = out.replace("–", "-");
		out = out.replace("$", "");
		out = out.replace(" ", "");
		out = out.replace(",", "");
		out = out.replace(" dollars and", "");
		out = out.replace(" Cents", "");
		out = out.replace("(pending)", "");
		out = out.replace("(", "");
		out = out.replace(")", "");

		return Double.parseDouble(out);
	}

	public static Double wrapAmount(Double input) {
		if (input == -0.00)
			return 0.00;
		else
			return input;
	}

	public static boolean isProblemWithLogin(UberWebDriver webDriver) {
		if (webDriver.lookupElement(By.xpath("//*[contains(text(),'Enter Account Number')]")) != null)
			return true;

		return false;
	}

	public static boolean isSecretQuestionShown(UberWebDriver webDriver) {
		WebElement secretQuestion = webDriver.lookupElement(By.xpath("//*[contains(text(),'Secret')]"));
		if (secretQuestion != null && secretQuestion.isDisplayed())
			return true;

		secretQuestion = webDriver.lookupElement(By.xpath("//*[contains(text(),'Verify Your Identity')]"), 0);
		if (secretQuestion != null && secretQuestion.isDisplayed())
			return true;

		secretQuestion = webDriver.lookupElement(By.xpath("//*[contains(text(),'Challenge Question')]"), 0);
		if (secretQuestion != null && secretQuestion.isDisplayed())
			return true;

		secretQuestion = webDriver.lookupElement(By.xpath("//*[contains(text(),'Security Verification')]"), 0);
		if (secretQuestion != null && secretQuestion.isDisplayed())
			return true;

		secretQuestion = webDriver.lookupElement(By.xpath("//*[contains(text(),'Security question')]"), 0);
		if (secretQuestion != null && secretQuestion.isDisplayed())
			return true;

		return false;
	}

	public static boolean checkIfSiteDown(UberWebDriver webDriver) {
		WebElement text = webDriver.lookupElement(By.xpath("//*[contains(text(),'Our site is down')]"));
		if (text != null && text.isDisplayed())
			return true;

		text = webDriver.lookupElement(By.xpath("//*[contains(text(),'Our site is down')]"), 0);
		if (text != null && text.isDisplayed())
			return true;

		return false;
	}

	public static List<Total> getTotalsFromFile(List<Account> accounts) {
		// open latest file to compare results
		File filePrevSummary = getLastFileModified(dirOutputTotals);

		// Delimiter used in CSV file
		final String COMMA_DELIMITER = ",";
		BufferedReader fileReader = null;
		List<Total> result = new ArrayList<Total>();

		try {
			String line = "";

			// Create the file reader
			fileReader = new BufferedReader(new FileReader(filePrevSummary));

			// Read the CSV file header to skip it
			fileReader.readLine();

			// Read the file line by line starting from the second line
			while ((line = fileReader.readLine()) != null) {
				// Get all tokens available in line
				String[] tokens = line.split(COMMA_DELIMITER);
				if (tokens.length > 0) {
					if (tokens[1].trim().equals(""))
						continue;
					Account account = accounts.stream().filter(a -> a.getId() == Integer.valueOf(tokens[1])).findFirst()
							.orElse(null);
					if (account == null)
						continue;
					if (tokens[3].equals("N/A"))
						result.add(new Total(account, Util.convertStringToDateByType("2016-07-19", 0), null, null));
					else
						result.add(new Total(account, Util.convertStringToDateByType("2016-07-19", 0),
								Double.valueOf(tokens[3]), null));
				}
			}
		} catch (Exception e) {
			logger.error("Error in CsvFileReader !!!");
			e.printStackTrace();
		} finally {
			try {
				fileReader.close();
			} catch (IOException e) {
				logger.error("Error while closing fileReader !!!");
				e.printStackTrace();
			}
		}

		return result;
	}

	public static List<Transaction> getPrevTransactions(List<Account> accounts) {
		// open latest file to compare results
		File filePrevSummary = getLastFileModified(dirOutputTransactions);

		// Delimiter used in CSV file
		final String COMMA_DELIMITER = ",";
		BufferedReader fileReader = null;
		List<Transaction> result = new ArrayList<Transaction>();

		try {
			String line = "";

			// Create the file reader
			fileReader = new BufferedReader(new FileReader(filePrevSummary));

			// Read the CSV file header to skip it
			fileReader.readLine();

			// Read the file line by line starting from the second line
			while ((line = fileReader.readLine()) != null) {
				// Get all tokens available in line
				String[] tokens = line.split(COMMA_DELIMITER);
				if (tokens.length > 0) {
					System.out.println(line);
					Account account = accounts.stream().filter(a -> a.getId() == Integer.valueOf(tokens[0])).findFirst()
							.orElse(null);
					if (account == null)
						continue;
					result.add(new Transaction(account, null, Util.convertStringToDateByType(tokens[1], 0), tokens[2],
							Double.valueOf(tokens[3]), null, null));
				}
			}
			logger.info("All {} transactions loaded from file", result.size());
		} catch (Exception e) {
			logger.error("Error in CsvFileReader !!!");
			e.printStackTrace();
		} finally {
			try {
				fileReader.close();
			} catch (IOException e) {
				logger.error("Error while closing fileReader !!!");
				e.printStackTrace();
			}
		}

		return result;
	}

	public static List<Account> getAccountsByDriver(List<Account> accounts, String driver) {
		return accounts.stream().filter(a -> a.getBank() != null && a.getBank().getName().equals(driver))
				.collect(Collectors.toList());
	}

	public static String amountToString(Double amount) {
		if (amount == null)
			return "N/A";
		if (amount == 0.0)
			return "-";

		return String.valueOf(Util.roundDouble(amount));
	}

	public static String amountToStringWithSign(Double amount) {
		if (amount == null)
			return "N/A";
		if (amount == 0.0)
			return "-";
		if (amount > 0.0)
			return "<font color='green'>+" + String.valueOf(Util.roundDouble(amount)) + "</font>";
		else
			return "<font color='red'>" + String.valueOf(Util.roundDouble(amount)) + "</font>";
	}

	public static void writeTotalsToFile(List<Total> totals) {

		FileWriter fileWriter = null;
		// Delimiter used in CSV file
		final String COMMA_DELIMITER = ",";
		final String NEW_LINE_SEPARATOR = "\n";

		// CSV file header
		final String FILE_HEADER = "date,code,account,amount,difference";

		try {
			String today = convertDateToStringType1(new Date());
			String fileName = dirOutputTotals + today + ".csv";
			fileWriter = new FileWriter(fileName);

			// Write the CSV file header
			fileWriter.append(FILE_HEADER.toString());

			// Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR);

			// Write a new student object list to the CSV file
			for (Total total : totals) {
				fileWriter.append(today);
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(total.getAccount().getId()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(total.getAccount().getName());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(amountToString(total.getAmount()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(amountToString(total.getDifference()));
				fileWriter.append(NEW_LINE_SEPARATOR);

				// saving to DB

			}

			// Adding total
			fileWriter.append(today);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(" ");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append("TOTAL");
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(amountToString(DataHandler.getFullTotal(totals)));
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(amountToString(DataHandler.getFullDiff(totals)));
			fileWriter.append(NEW_LINE_SEPARATOR);

			logger.info("File with latest totals created: {}", fileName);

		} catch (Exception e) {

			logger.error("Error in CsvFileWriter !!!");
			e.printStackTrace();

		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				logger.info("Error while flushing/closing fileWriter !!!");
				e.printStackTrace();
			}
		}
	}

	public static void writeTransactionsToFile(List<Transaction> transactions) {

		FileWriter fileWriter = null;
		// Delimiter used in CSV file
		final String COMMA_DELIMITER = ",";
		final String NEW_LINE_SEPARATOR = "\n";

		// CSV file header
		final String FILE_HEADER = "code,date,description,amount,category";

		try {
			String today = convertDateToStringType1(new Date());
			String fileName = dirOutputTransactions + today + ".csv";
			fileWriter = new FileWriter(fileName);

			// Write the CSV file header
			fileWriter.append(FILE_HEADER.toString());

			// Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR);

			// Write a new student object list to the CSV file
			for (Transaction transaction : transactions) {
				fileWriter.append(String.valueOf(transaction.getAccount().getId()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Util.convertDateToStringType1(transaction.getDate()));
				fileWriter.append(COMMA_DELIMITER);
				String description = transaction.getDecription().replace(",", " ").replace("http://", "");
				if (description.length() > 50)
					fileWriter.append(description.substring(0, 50));
				else
					fileWriter.append(description);
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(amountToString(transaction.getAmount()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(transaction.getCategory().toString());
				fileWriter.append(NEW_LINE_SEPARATOR);
			}

			logger.info("File with transactions created: {}", fileName);

		} catch (Exception e) {

			logger.error("Error in CsvFileWriter !!!");
			e.printStackTrace();

		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				logger.info("Error while flushing/closing fileWriter !!!");
				e.printStackTrace();
			}
		}
	}

	public static File getLastFileModified(String dir) {
		File fl = new File(dir);
		File[] files = fl.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isFile();
			}
		});
		long lastMod = Long.MIN_VALUE;
		File choice = null;
		for (File file : files) {
			if ("csv".equals(FilenameUtils.getExtension(file.getAbsolutePath()))) {
				if (file.lastModified() > lastMod) {
					choice = file;
					lastMod = file.lastModified();
				}
			}
		}
		return choice;
	}

	public static double roundDouble(double input) {
		return Math.round(input * 100.0) / 100.0;
	}

	public static String prepareTextForQuery(String inText) {
		String outText = inText.replace("'", "");
		outText = outText.replace("#", "");

		return outText;
	}

	public static void addTotal(Statement stmt, java.sql.Timestamp timestamp, int account, Double amount) {
		// addBatch(stmt, "INSERT INTO TotalsBuffer VALUES('" + timestamp + "',"
		// + account + ", " + amount + ")");
		// trace("Total added: " + timestamp + "/" + getAccountName(stmt,
		// account) + "/" + amount, ta);
	}

	public static boolean isPending(String row) {
		if ("".equals(row.trim()) || row.contains("pending transactions") || row.contains("Pending Transactions")
				|| row.contains("There is no recent activity") || row.contains("Posted Transactions")
				|| row.equals("Pending") || row.contains("end of your statement")
				|| row.startsWith("No activity posted")
				|| row.equals("You've reached the end of the statement cycle account activity.")
				|| row.equals("In Progress and Cleared Transactions") || row.contains("Pending*")
				|| row.contains("Total for"))
			return true;
		else
			return false;
	}

	public static void clearBatch(Statement stmt) {
		try {
			stmt.clearBatch();
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
	}

	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ex) {
			logger.error(ex.getMessage());
		}
	}

	public static void executorShutdown(ExecutorService executor) {
		try {
			// logger.info("attempt to shutdown executor");
			executor.shutdown();
			executor.awaitTermination(15, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			logger.error("tasks interrupted");
		} finally {
			if (!executor.isTerminated()) {
				logger.error("cancel non-finished tasks");
			}
			executor.shutdownNow();
			logger.info("shutdown finished");
		}
	}

	public static String getThreadNumber(String string) {
		// thread # within pool
		if (string.length() == 1)
			return string;
		else {
			int i = string.indexOf("Bank accounts");
			return string.substring(i + 15, i + 16);
		}
	}

	public static By getByLocator(String string) {
		if (string == null)
			return null;
		if (string.startsWith("id"))
			return By.id(string.replace("id:", ""));
		if (string.startsWith("css"))
			return By.cssSelector(string.replace("css:", ""));
		if (string.startsWith("link"))
			return By.linkText(string.replace("link:", ""));
		if (string.startsWith("xpath"))
			return By.xpath(string.replace("xpath:", ""));
		if (string.startsWith("name"))
			return By.name(string.replace("name:", ""));
		if (string.startsWith("class"))
			return By.className(string.replace("class:", ""));
		else
			return null;
	}

	public static boolean isDateToday(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(date).equals(sdf.format(new Date()));
	}

	public static boolean isDateThisMonth(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		return sdf.format(date).equals(sdf.format(new Date()));
	}

	public static String formatDateForEmail(Date date) {
		return (isDateToday(date)) ? "today" : convertDateToStringType2(date);
	}

	public static Account getAccountById(List<Account> accounts, int id) {
		return accounts.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
	}

	static Map<Category, Double> getTotalsByCategory(List<Transaction> transactions, Date beginDate, Date endDate) {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		return transactions.stream()
				.filter(t -> t.getCategory() != null
						&& (t.getDate().after(beginDate) || sdf.format(t.getDate()).equals(sdf.format(beginDate)))
						&& t.getDate().before(endDate))
				.collect(Collectors.groupingBy(Transaction::getCategory,
						Collectors.summingDouble(Transaction::getAmount)));
	}

	public static List<Account> skipUpdatedBankAccounts(List<Account> accounts, List<Total> prevTotals) {
		List<Account> result = new ArrayList<Account>();

		prevTotals.stream().filter(t -> !isDateToday(t.getDate())).forEach(t -> {
			if (accounts.contains(t.getAccount()))
				result.add(t.getAccount());
		});

		return result;
	}

	static String getMonth(int month) {
		return new DateFormatSymbols().getMonths()[month - 1];
	}

	static int getCurrentMonthInt() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		return cal.get(Calendar.MONTH) + 1;
	}

	static String[] getCurrentQuaterMonths() {
		// define number of current month
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int currentMonth = cal.get(Calendar.MONTH);
		int quater = Math.floorDiv(currentMonth, 3);

		// retrieving all months
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] months = dfs.getMonths();

		String result[] = new String[3];
		result[0] = months[quater * 3];
		result[1] = months[quater * 3 + 1];
		result[2] = months[quater * 3 + 2];

		return result;
	}

	public static Date getFirstDayOfQuarter(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) / 3 * 3);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	public static Boolean isDateInBetween(Date date, Date start, Date end) {
		return (date.getTime() >= start.getTime() && date.getTime() <= end.getTime());
	}

	public static Date getFirstDayOfYear() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}

	public static Date getFirstDayOfMonthsBack(int monthsBack) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - monthsBack);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}

	public static Date getLastDayOfMonthsBack(int monthsBack) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - monthsBack);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		return cal.getTime();
	}

	// SELENIUM
	public static String getLocatorForWebElement(WebElement we) {
		// assuming locator in xpath
		return we.toString().substring(we.toString().indexOf("->") + 10);
	}

}
