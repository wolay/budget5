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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.CreditScore;
import com.dashboard.budget.DAO.DataRetrievalStatus;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author aanpilogov
 */
public class Util implements Config {

	private static Logger logger = LoggerFactory.getLogger(Util.class);
	
	public static Date convertStringToDateType0(String string) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(string.trim());
		} catch (ParseException ex) {
			logger.error(ex.getMessage());
		}
		return null;
	}

	public static Date convertStringToDateType1(String string) {
		try {
			return new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH).parse(string.trim());
		} catch (ParseException ex) {
			logger.error(ex.getMessage());
		}
		return null;
	}
	
	public static Date convertStringToDateType2(String string) {
		try {
			return new SimpleDateFormat("MMM. dd, yyyy", Locale.ENGLISH).parse(string.trim());
		} catch (ParseException ex) {
			logger.error(ex.getMessage());
		}
		return null;
	}	
	
	public static Date convertStringToDateType3(String string) {
		try {
			return new SimpleDateFormat("MMMdd", Locale.ENGLISH).parse(string.trim());
		} catch (ParseException ex) {
			logger.error(ex.getMessage());
		}
		return null;
	}	
	
	public static Date convertStringToDateType4(String string) {
		try {
			return new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).parse(string.trim());
		} catch (ParseException ex) {
			logger.error(ex.getMessage());
		}
		return null;
	}	
	
	public static Date convertStringToDateType5(String string) {
		try {
			return new SimpleDateFormat("MMMMM dd, yyyy", Locale.ENGLISH).parse(string.trim());
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
		out = out.replace("$", "");
		out = out.replace(" ", "");
		out = out.replace(",", "");
		out = out.replace(" dollars and", "");
		out = out.replace(" Cents", "");
		out = out.replace("(pending)", "");

		return Double.parseDouble(out);
	}

	public static Double wrapAmount(Double input) {
		if (input == -0.00)
			return 0.00;
		else
			return input;
	}

	public static Map<String, Double> getPrevTotals() {
		// open latest file to compare results
		File filePrevSummary = getLastFileModified(dirOutputTotals);

		// Delimiter used in CSV file
		final String COMMA_DELIMITER = ",";
		BufferedReader fileReader = null;
		Map<String, Double> result = new HashMap<String, Double>();

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
					if (tokens[3].equals("N/A"))
						result.put(tokens[1], null);
					else
						result.put(tokens[1], Double.valueOf(tokens[3]));
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
	
	public static List<Transaction> getPrevTransactions() {
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
					result.add(new Transaction(tokens[0],Util.convertStringToDateType0(tokens[1]),tokens[2],Double.valueOf(tokens[3]),""));
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

	public static List<Account> getAccountsByDriver(List<Account> accounts, String driver) {
		List<Account> result = new ArrayList<Account>();
		for (Account account : accounts)
			if (account.getBank().equals(driver))
				result.add(account);
		return result;
	}

	public static List<Account> getAccountsByDriver(List<Account> accounts, String driver, String owner) {
		List<Account> result = new ArrayList<Account>();
		for (Account account : accounts) {
			if (!account.getBank().equals(driver))
				continue;
			if (!account.getOwner().equals(owner))
				continue;
			result.add(account);
		}
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
				fileWriter.append(total.getAccount().getCode());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(total.getAccount().getName());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(amountToString(total.getAmount()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(amountToString(total.getDifference()));
				fileWriter.append(NEW_LINE_SEPARATOR);
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
				fileWriter.append(transaction.getCode());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(Util.convertDateToStringType1(transaction.getDate()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(transaction.getDecription().replace(",", " "));				
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(amountToString(transaction.getAmount()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(transaction.getCategory());
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

	public static void sendEmailSummary(List<Total> totals, List<Transaction> transactions,
			List<CreditScore> creditScores, String spentTime) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.mail.ru");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");

		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("aianitro@mail.ru", "Irina14022009");
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
					+ "<tr><table border='1' cellpadding='1' cellspacing='1' style='width:450px;'><thead><tr><th>Account</th><th>Amount</th><th>Diff</th></tr></thead>";
			content = content + "<tfoot><tr><td><b>TOTAL</b></td><td><b>"
					+ amountToString(DataHandler.getFullTotal(totals)) + "</b></td><td><b>"
					+ amountToString(DataHandler.getFullDiff(totals)) + "</b></td></tr></tfoot><tbody>";
			for (Total total : totals) {
				content = content + "<tr style='background-color:"+Util.getStatusColor(total.getStatus())+"'><td><a href='"+total.getAccount().getUrl()+"'>"+ total.getAccount().getName()+"</a>";
				List<Transaction> transactionsByAccount = transactions.stream()
						.filter(p -> p.getCode().equals(total.getAccount().getCode())).collect(Collectors.toList());
				if (transactionsByAccount.size() > 0) {
					content = content + "<br><table border='0' cellpadding='1' cellspacing='1' style='width:100%;'>";
					for (Transaction transaction : transactionsByAccount) {
						content = content + "<tr><td><font size='1'>" + Util.convertDateToStringType2(transaction.getDate())
								+ "</font></td><td><font size='1'>" + transaction.getDecription()
								+ "</font></td><td><font size='1'>" + amountToString(transaction.getAmount())
								+ "</font></td></tr>";
					}
					content = content + "</table>";
				}
				content = content + "</td><td>" + amountToString(total.getAmount()) + "</td><td>"
						+ amountToString(total.getDifference()) + "</td></tr>";
			}
			content = content + "</tbody></table>";

			// Credit scores
			content = content + "<P><b>Credit scores: </b>";
			for (CreditScore creditScore : creditScores) {
				content = content + "<br>" + creditScore.getName() + ": " + creditScore.getScore();
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

	private static String getStatusColor(DataRetrievalStatus status) {
		if(status==DataRetrievalStatus.OK)
			return "#BCE954";
		else if(status==DataRetrievalStatus.FAILED)
			return "#FF7F50";
		else if(status==DataRetrievalStatus.SKIPPED)
			return "#FFE87C";
		else
			return "white";
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

	public static String rotateDate(String string, String mode) {
		Date date = null;
		try {
			if (null != mode) {
				switch (mode) {
				case "MMMM dd":
					date = new SimpleDateFormat("MMMM-dd-yyyy", Locale.ENGLISH)
							.parse(string.replace(" ", "-") + (new SimpleDateFormat("yyyy").format(new Date())));
					break;
				case "MMMM\ndd":
					date = new SimpleDateFormat("MMMM-dd-yyyy", Locale.ENGLISH)
							.parse(string.replace("\n", "-") + "-" + (new SimpleDateFormat("yyyy").format(new Date())));
					break;
				default:
					date = new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH).parse(string);
					break;
				}
			}
			return new SimpleDateFormat("yyyy-MM-dd").format(date);
		} catch (ParseException ex) {
			logger.error(ex.getMessage());
		}
		return null;
	}

	public static String prepareTextForQuery(String inText) {
		String outText = inText.replace("'", "");
		outText = outText.replace("#", "");

		return outText;
	}

	public static String getAccountName(Statement stmt, int code) {
		ResultSet rsER;

		if (code == 4)
			return "CHECKING XXXXXX6763";
		else if (code == 5)
			return "WAY2SAVE® SAVINGS XXXXXX3119";
		else if (code == 6)
			return "PLATINUM CARD XXXX-XXXX-XXXX-4116";

		try {
			rsER = stmt.executeQuery("SELECT NAME FROM ACCOUNTS WHERE ID = " + code);
			if (rsER.next()) {
				return rsER.getString("Name");
			}
		} catch (SQLException ex) {
		}
		;

		return "";
	}

	public static void addTotal(Statement stmt, java.sql.Timestamp timestamp, int account, Double amount) {
		// addBatch(stmt, "INSERT INTO TotalsBuffer VALUES('" + timestamp + "',"
		// + account + ", " + amount + ")");
		// trace("Total added: " + timestamp + "/" + getAccountName(stmt,
		// account) + "/" + amount, ta);
	}

	public static void clearBatch(Statement stmt) {
		try {
			stmt.clearBatch();
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
	}

	public static void waitForElement(WebDriverWait wait, By locator) {
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		}
		catch(Exception e){
			logger.error("Unable to find locator: {}", locator);			
		};
	}

	public static void waitAndClick(WebDriver driver, WebDriverWait wait, By locator) {
		wait.until(ExpectedConditions.elementToBeClickable(locator));
		driver.findElement(locator).click();
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
			//logger.info("attempt to shutdown executor");
			executor.shutdown();
			executor.awaitTermination(10, TimeUnit.MINUTES);			
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
		int i = string.indexOf("thread");
		return string.substring(i+7, i+8);
	}

}
