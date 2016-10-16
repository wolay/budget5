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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Credential;
import com.dashboard.budget.DAO.CreditScore;
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
		if (webDriver.lookupElement(By.xpath("//*[contains(text(),'Secret')]")) != null)
			return true;

		if (webDriver.lookupElement(By.xpath("//*[contains(text(),'Verify Your Identity')]"), 0) != null)
			return true;

		if (webDriver.lookupElement(By.xpath("//*[contains(text(),'Challenge Question')]"), 0) != null)
			return true;

		if (webDriver.lookupElement(By.xpath("//*[contains(text(),'Security Verification')]"), 0) != null)
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
		List<Account> result = new ArrayList<Account>();
		for (Account account : accounts)
			if (account.getBank().getName().equals(driver))
				result.add(account);
		return result;
	}

	public static String amountToString(Double amount) {
		if (amount == null)
			return "N/A";
		return String.valueOf(Util.roundDouble(amount));
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

	public static void sendEmailSummary(List<Total> totals, List<CreditScore> creditScores, String spentTime,
			Credential credentials) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.mail.ru");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");

		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(credentials.getLogin(), credentials.getPassword());
			}
		});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("aianitro@mail.ru"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(summaryReceiver));
			String today = convertDateToStringType1(new Date());
			message.setSubject("Dashboard on " + today);

			// Budget
			String content = "<b>Budget: </b>";
			content = content
					+ "<tr><table border='1' cellpadding='1' cellspacing='1' style='width:550px;'><thead><tr><th>Date</th><th>Account</th><th>Amount</th><th>Diff</th></tr></thead>";
			content = content + "<tfoot><tr><td></td><td><b>TOTAL</b></td><td><b>"
					+ amountToString(DataHandler.getFullTotal(totals)) + "</b></td><td><b>"
					+ amountToString(DataHandler.getFullDiff(totals)) + "</b></td></tr></tfoot><tbody>";
			Collections.sort(totals);
			for (Total total : totals) {
				if (!total.getAccount().getIsEnabled())
					continue;
				content = content + "<tr style='background-color:" + Util.getStatusColor(total) + "'><td>"
						+ formatDateForEmail(total.getDate()) + "</td><td><a href='" + total.getAccount().getUrl()
						+ "'>" + total.getAccount().getName() + "</a>";
				List<Transaction> transactions = total.getTransactions();
				if (transactions != null)
					Collections.sort(transactions);
				if (transactions != null && transactions.size() > 0) {
					content = content + "<br><table border='0' cellpadding='1' cellspacing='1' style='width:100%;'>";
					for (Transaction transaction : total.getTransactions()) {
						content = content + "<tr><td width='10'><font size='1'>"
								+ Util.convertDateToStringType2(transaction.getDate())
								+ "</font></td><td><font size='1'>"
								+ ((transaction.getCategory() == null) ? "" : transaction.getCategory().getName())
								+ "</font></td><td><font size='1'>" + transaction.getDecription()
								+ "</font></td><td width='11'><font size='1'>" + amountToString(transaction.getAmount())
								+ "</font></td></tr>";
					}
					content = content + "</table>";
				}
				content = content + "</td><td>" + amountToString(total.getAmount()) + "</td><td>"
						+ amountToString(total.getDifference()) + "</td></tr>";
			}
			content = content + "</tbody></table>";

			// Uncategorized transactions
			List<Transaction> uncategorized = new ArrayList<Transaction>();
			int allTransactions = 0;
			for (Total total : totals) {
				for (Transaction transaction : total.getTransactions()) {
					if (transaction.getCategory().getName().equals("Unrecognized"))
						uncategorized.add(transaction);
				}
				allTransactions += total.getTransactions().size();
			}
			content = content + "<P><b>Uncategorized transactions: </b>" + uncategorized.size() * 100 / allTransactions
					+ "%";
			content = content + "<br><table border='0' cellpadding='1' cellspacing='1' style='width:500px;'><tbody>";
			for (Transaction transaction : uncategorized) {
				content = content + "<tr><td><font size='1'>" + transaction.getAccount().getName()
						+ "</font></td><td><font size='1'>" + transaction.getDecription()
						+ "</font></td><td width='10'><font size='1'>" + transaction.getCategoryStr()
						+ "</font></td></tr>";
			}
			content = content + "</tbody></table>";

			// Credit scores
			content = content + "<P><b>Credit scores: </b>";
			for (CreditScore creditScore : creditScores) {
				content = content + "<br>" + creditScore.getName() + ": " + creditScore.getScore();
				if (creditScore.getDifference() > 0)
					content = content + "(<font color='green'>+" + creditScore.getDifference() + "</font>)";
				else if (creditScore.getDifference() < 0)
					content = content + "(<font color='red'>" + creditScore.getDifference() + "</font>)";
				else
					content = content + "(+" + creditScore.getDifference() + ")";
			}

			// Time spent for retrieving
			content = content + "<P><font size='1' color='Gainsboro'><b>Time spent: </b>" + spentTime + "</font>";

			message.setContent(content, "text/html");

			Transport.send(message);

			logger.info("Summary sent to {}", summaryReceiver);

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getStatusColor(Total total) {
		if (isDateToday(total.getDate()))
			return "#BCE954";
		// else if (status == DataRetrievalStatus.FAILED)
		// return "#FF7F50";
		else // if (status == DataRetrievalStatus.SKIPPED)
			return "#FFE87C";
		// else
		// return "white";
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
		SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy");
		return sdf.format(date).equals(sdf.format(new Date()));
	}

	public static String formatDateForEmail(Date date) {
		return (isDateToday(date)) ? "today" : convertDateToStringType2(date);
	}

	public static Account getAccountById(List<Account> accounts, int id) {
		return accounts.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
	}

	public static List<Account> skipUpdatedBankAccounts(List<Account> accounts, List<Total> prevTotals) {
		List<Account> result = new ArrayList<Account>();

		prevTotals.stream().filter(t -> !isDateToday(t.getDate())).forEach(t -> {
			if (accounts.contains(t.getAccount()))
				result.add(t.getAccount());
		});

		return result;
	}

	// SELENIUM
	public static String getLocatorForWebElement(WebElement we) {
		// assuming locator in xpath
		return we.toString().substring(we.toString().indexOf("->") + 10);
	}
}
