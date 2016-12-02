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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.BudgetPlan;
import com.dashboard.budget.DAO.Credential;
import com.dashboard.budget.DAO.CreditScore;
import com.dashboard.budget.DAO.PlanFact;
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
			return "<font color='green'>+" + String.valueOf(Util.roundDouble(amount))+ "</font>";
		else
			return "<font color='red'>" +String.valueOf(Util.roundDouble(amount))+ "</font>";
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

	public static void sendEmailSummary(DataHandler dataHandler, String spentTime, Credential credentials) {
		List<Total> totals = dataHandler.getLastTotals();
		List<Transaction> allTransactions = dataHandler.getAllTransactions();
		List<BudgetPlan> budgetPlans = dataHandler.getBudgetPlansList();
		List<CreditScore> creditScores = dataHandler.getLastCreditScores();

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
			message.setSubject("Dashboard on " + convertDateToStringType1(new Date()));

			// Preparation
			List<Transaction> todayTransactions = new ArrayList<Transaction>();
			for (Total total : totals) {
				if (total.getTransactions() != null)
					todayTransactions.addAll(total.getTransactions());
			}

			// collecting table of plan-fact by category
			List<PlanFact> planFactList = new ArrayList<PlanFact>();
			LocalDate today = LocalDate.now();
			dataHandler.getCategories().stream().forEach(category -> {
				Double amountFact = allTransactions.stream().filter(t -> t.getCategory() == category
						&& Util.isDateThisMonth(t.getDate()) && !t.getIsTransferComplete())
						.mapToDouble(Transaction::getAmount).sum();
				Double amountDiffToday = todayTransactions.stream()
						.filter(t -> t.getCategory() == category && !t.getIsTransferComplete())
						.mapToDouble(Transaction::getAmount).sum();
				BudgetPlan budgetPlan = budgetPlans.stream().filter(b -> b.getCategory() == category).findFirst()
						.orElse(null);
				Double amountPlan = 0.0;
				if (budgetPlan == null)
					logger.info("Category '{}' is not in budget plan", category.getName());
				else
					amountPlan = budgetPlan.getAmount() / 3;
				Double amountOver = 0.0;
				if (category.getType() == 1 && amountFact - amountPlan > 0)
					amountOver = amountFact - amountPlan;
				else if (category.getType() == 2 && amountPlan - amountFact > 0)
					amountOver = amountFact - amountPlan;

				planFactList.add(new PlanFact(category, today.withDayOfMonth(1), amountPlan, amountFact,
						amountDiffToday, amountOver));
			});
			Collections.sort(planFactList);

			// collecting table of balances (begin & end of the month)
			String[] quater = getCurrentQuaterMonths();
			Double monthBeginBalance = 1.11;// dataHandler.getBalances().stream().filter(b
											// ->
											// Util.isDateThisMonth(b.getDate())).findAny().orElse(null).getAmount();

			// Budget
			// Collecting all categories in transactions
			String content = "<b>Budget (this month): </b>";
			switch (getCurrentMonthInt() % 3) {
			case 1:
				content = content + getBudgetContent1(planFactList, monthBeginBalance, quater);
				break;
			case 2:
				content = content + getBudgetContent3(planFactList, monthBeginBalance, quater);
				break;
			case 3:
				content = content + getBudgetContent3(planFactList, monthBeginBalance, quater);
				break;
			}
			content = content + "</tbody></table>";

			// Totals & transactions
			content = content + "<P><b>Totals & transactions: </b>";
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
			int allTransactionsCounter = (int) allTransactions.stream().filter(t -> Util.isDateThisMonth(t.getDate()))
					.count();
			allTransactions.stream().filter(t -> Util.isDateThisMonth(t.getDate())).forEach(t -> {
				if (t.getCategory() == null || t.getCategory().getName().equals("Unrecognized"))
					uncategorized.add(t);
			});
			if (uncategorized.isEmpty())
				content = content + "<P><b>Great news! There is no uncategorized transactions this month</b>";
			else {
				content = content + "<P><b>Uncategorized transactions: </b>"
						+ uncategorized.size() * 100 / allTransactionsCounter + "% (" + uncategorized.size() + ")";
				content = content
						+ "<br><table border='0' cellpadding='1' cellspacing='1' style='width:500px;'><tbody>";
				for (Transaction transaction : uncategorized) {
					content = content + "<tr><td><font size='1'>" + transaction.getAccount().getName()
							+ "</font></td><td><font size='1'>" + transaction.getDecription()
							+ "</font></td><td width='10'><font size='1'>" + transaction.getCategoryStr()
							+ "</font></td></tr>";
				}
				content = content + "</tbody></table>";
			}

			// Transactions in transfer
			List<Transaction> transfers = new ArrayList<Transaction>();
			allTransactions
					.stream().filter(t -> Util.isDateThisMonth(t.getDate())
							&& t.getCategory().getName().equals("Transfer") && !t.getIsTransferComplete())
					.forEach(t -> transfers.add(t));
			if (transfers.isEmpty())
				content = content + "<P><b>There is no transferring transactions</b>";
			else {
				content = content + "<P><b>Transactions in transfer: </b> (" + transfers.size() + ")";
				content = content
						+ "<br><table border='0' cellpadding='1' cellspacing='1' style='width:500px;'><tbody>";
				for (Transaction transfer : transfers) {
					content = content + "<tr><td><font size='1'>" + transfer.getAccount().getName()
							+ "</font></td><td><font size='1'>" + transfer.getDecription()
							+ "</font></td><td width='10'><font size='1'>" + transfer.getAmount() + "</font></td></tr>";
				}
				content = content + "</tbody></table>";
			}

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

		} catch (

		MessagingException e)

		{
			throw new RuntimeException(e);
		}

	}
	

	private static String getBudgetContent1(List<PlanFact> planFactList, Double monthBeginBalance, String[] quater) {
		String content;

		content = "<table border='1' cellpadding='1' cellspacing='1' style='width:550px;'>"
				+ "<thead><tr style='color: gray'><th>Beginning balance</th>" + "<th colspan='4'>" + monthBeginBalance
				+ "</th><th>18920</th><th>21093</th></tr>" + "<tr><th rowspan='2'>Category</th><th colspan='4'>"
				+ quater[0] + "</th><th rowspan='2'><font color='gray'>" + quater[1]
				+ "</font></th><th rowspan='2'><font color='gray'>" + quater[2] + "</font></th></tr>"
				+ "<tr><th>Plan</th><th>Fact</th><th>Today</th><th>Over</th></tr>" + "</thead>";
		Double totalBudgetPlan = planFactList.stream().mapToDouble(PlanFact::getAmountPlan).sum();
		Double totalBudgetOver = planFactList.stream().mapToDouble(PlanFact::getAmountOver).sum();
		Double totalDiffToday = planFactList.stream().mapToDouble(PlanFact::getAmountTodayDiff).sum();
		Double totalMonthDynamic = totalBudgetPlan + totalBudgetOver;
		Double monthEndBalance = monthBeginBalance + totalMonthDynamic;
		content = content + "<tfoot><tr><td><b>TOTAL</b></td><td><b>" + amountToString(totalBudgetPlan)
				+ "</b></td><td>-</td><td><b>" + amountToString(totalDiffToday) + "</b></td><td>"
				+ amountToString(totalBudgetOver) + "</td><td><b><font color='gray'>" + amountToString(totalBudgetPlan)
				+ "</font></b></td><td><b><font color='gray'>" + amountToString(totalBudgetPlan)
				+ "</font></b></td></tr>"
				+ "<tr><td rowspan='2'><b>By the end of month</b></td><td colspan='4' align='center'><b><font size='4'>"
				+ amountToStringWithSign(totalMonthDynamic)
				+ "</font></b></td><td align='center'><b><font size='4'>+3432</font></b></td><td align='center'><b><font size='4'>-2312</font></b></td></tr>"
				+ "<tr><td colspan='4' align='center'><b><font size='4'>" + amountToString(monthEndBalance)
				+ "</b></td><td align='center'><b><font size='4'>25000</font></b></td><td align='center'><b><font size='4'>30000</font></b></td></tr></font>"
				+ "</tfoot><tbody>";
		// Grouping by type
		// - Income
		// - caption with totals
		Double totalIncomePlan = planFactList.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountPlan).sum();
		Double totalIncomeFact = planFactList.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalIncomeDiffToday = planFactList.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountTodayDiff).sum();
		Double totalIncomeOver = planFactList.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountOver).sum();
		content = content + "<tr style='background-color:#27AE60'><td><b>Income</b></td><td><b>"
				+ amountToString(totalIncomePlan) + "</b></td><td><b>" + amountToString(totalIncomeFact)
				+ "</b></td><td><b>" + amountToString(totalIncomeDiffToday) + "</b></td><td><b>"
				+ amountToString(totalIncomeOver) + "</b></td><td><b><font color='gray'>"
				+ amountToString(totalIncomePlan) + "</font></b></td><td><b><font color='gray'>"
				+ amountToString(totalIncomePlan) + "</font></b></td></tr>";
		// - table with categories
		for (PlanFact planFact : planFactList) {
			if (planFact.getCategory().getType() != 1)
				continue;
			content = content + "<tr style='background-color:#D5F5E3'><td><p style='margin-left:10px;'>"
					+ planFact.getCategory().getName() + "</p</td><td>" + amountToString(planFact.getAmountPlan())
					+ "</td><td>" + amountToString(planFact.getAmountFact()) + "</td><td>"
					+ amountToString(planFact.getAmountTodayDiff()) + "</td><td>"
					+ amountToString(planFact.getAmountOver()) + "</td><td><font color='gray'>"
					+ amountToString(planFact.getAmountPlan()) + "</font></td><td><font color='gray'>"
					+ amountToString(planFact.getAmountPlan()) + "</font></td></tr>";
		}

		// - Outcome
		// - caption with totals
		Double totalOutcomePlan = planFactList.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountPlan).sum();
		Double totalOutcomeFact = planFactList.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalOutcomeDiffToday = planFactList.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountTodayDiff).sum();
		Double totalOutcomeOver = planFactList.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountOver).sum();
		content = content + "<tr style='background-color:#EC7063'><td><b>Outcome</b></td><td><b>"
				+ amountToString(totalOutcomePlan) + "</b></td><td><b>" + amountToString(totalOutcomeFact)
				+ "</b></td><td><b>" + amountToString(totalOutcomeDiffToday) + "</b></td><td><b>"
				+ amountToString(totalOutcomeOver) + "</b></td><td><b><font color='gray'>"
				+ amountToString(totalOutcomePlan) + "</font></b></td><td><b><font color='gray'>"
				+ amountToString(totalOutcomePlan) + "</font></b></td></tr>";
		// - table with categories
		for (PlanFact planFact : planFactList) {
			if (planFact.getCategory().getType() != 2)
				continue;
			content = content + "<tr style='background-color:#FADBD8'><td><p style='margin-left:10px;'>"
					+ planFact.getCategory().getName() + "</p</td><td>" + amountToString(planFact.getAmountPlan())
					+ "</td><td>" + amountToString(planFact.getAmountFact()) + "</td><td>"
					+ amountToString(planFact.getAmountTodayDiff()) + "</td><td>"
					+ amountToString(planFact.getAmountOver()) + "</td><td><font color='gray'>"
					+ amountToString(planFact.getAmountPlan()) + "</font></td><td><font color='gray'>"
					+ amountToString(planFact.getAmountPlan()) + "</font></td></tr>";
		}

		// - Transfers
		// - caption with totals
		Double totalTransferFact = planFactList.stream().filter(pf -> pf.getCategory().getType() == 3)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalTransferToday = planFactList.stream().filter(pf -> pf.getCategory().getType() == 3)
				.mapToDouble(PlanFact::getAmountTodayDiff).sum();
		content = content + "<tr style='background-color:#85C1E9'><td><b>Transfer</b></td><td>-</td><td><b>"
				+ amountToString(totalTransferFact) + "</b></td><td><b>" + amountToString(totalTransferToday)
				+ "</b></td><td>-</td><td><b><font color='gray'>-</font></b></td><td><b><font color='gray'>-</font></b></td></tr>";

		return content;
	}

	private static String getBudgetContent3(List<PlanFact> planFactList, Double monthBeginBalance, String[] quater) {
		String content;

		content = "<table border='1' cellpadding='1' cellspacing='1' style='width:550px;'>"
				+ "<thead><tr style='color: gray'><th>Beginning balance</th>" + "<th colspan='4'>" + monthBeginBalance
				+ "</th><th>18920</th><th>21093</th></tr>" + "<tr><th rowspan='2'>Category</th><th colspan='4'>"
				+ quater[0] + "</th><th rowspan='2'><font color='gray'>" + quater[1]
				+ "</font></th><th rowspan='2'><font color='gray'>" + quater[2] + "</font></th></tr>"
				+ "<tr><th>Plan</th><th>Fact</th><th>Today</th><th>Over</th></tr>" + "</thead>";
		Double totalBudgetPlan = planFactList.stream().mapToDouble(PlanFact::getAmountPlan).sum();
		Double totalBudgetOver = planFactList.stream().mapToDouble(PlanFact::getAmountOver).sum();
		Double totalDiffToday = planFactList.stream().mapToDouble(PlanFact::getAmountTodayDiff).sum();
		Double totalMonthDynamic = totalBudgetPlan + totalBudgetOver;
		Double monthEndBalance = monthBeginBalance + totalMonthDynamic;
		content = content + "<tfoot><tr><td><b>TOTAL</b></td><td><b>" + amountToString(totalBudgetPlan)
				+ "</b></td><td>-</td><td><b>" + amountToString(totalDiffToday) + "</b></td><td>"
				+ amountToString(totalBudgetOver) + "</td><td><b><font color='gray'>" + amountToString(totalBudgetPlan)
				+ "</font></b></td><td><b><font color='gray'>" + amountToString(totalBudgetPlan)
				+ "</font></b></td></tr>"
				+ "<tr><td rowspan='2'><b>By the end of month</b></td><td colspan='4' align='center'><b><font size='4'>"
				+ amountToStringWithSign(totalMonthDynamic)
				+ "</font></b></td><td align='center'><b><font size='4'>+3432</font></b></td><td align='center'><b><font size='4'>-2312</font></b></td></tr>"
				+ "<tr><td colspan='4' align='center'><b><font size='4'>" + amountToString(monthEndBalance)
				+ "</b></td><td align='center'><b><font size='4'>25000</font></b></td><td align='center'><b><font size='4'>30000</font></b></td></tr></font>"
				+ "</tfoot><tbody>";
		// Grouping by type
		// - Income
		// - caption with totals
		Double totalIncomePlan = planFactList.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountPlan).sum();
		Double totalIncomeFact = planFactList.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalIncomeDiffToday = planFactList.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountTodayDiff).sum();
		Double totalIncomeOver = planFactList.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountOver).sum();
		content = content + "<tr style='background-color:#27AE60'><td><b>Income</b></td><td><b>"
				+ amountToString(totalIncomePlan) + "</b></td><td><b>" + amountToString(totalIncomeFact)
				+ "</b></td><td><b>" + amountToString(totalIncomeDiffToday) + "</b></td><td><b>"
				+ amountToString(totalIncomeOver) + "</b></td><td><b><font color='gray'>"
				+ amountToString(totalIncomePlan) + "</font></b></td><td><b><font color='gray'>"
				+ amountToString(totalIncomePlan) + "</font></b></td></tr>";
		// - table with categories
		for (PlanFact planFact : planFactList) {
			if (planFact.getCategory().getType() != 1)
				continue;
			content = content + "<tr style='background-color:#D5F5E3'><td><p style='margin-left:10px;'>"
					+ planFact.getCategory().getName() + "</p</td><td>" + amountToString(planFact.getAmountPlan())
					+ "</td><td>" + amountToString(planFact.getAmountFact()) + "</td><td>"
					+ amountToString(planFact.getAmountTodayDiff()) + "</td><td>"
					+ amountToString(planFact.getAmountOver()) + "</td><td><font color='gray'>"
					+ amountToString(planFact.getAmountPlan()) + "</font></td><td><font color='gray'>"
					+ amountToString(planFact.getAmountPlan()) + "</font></td></tr>";
		}

		// - Outcome
		// - caption with totals
		Double totalOutcomePlan = planFactList.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountPlan).sum();
		Double totalOutcomeFact = planFactList.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalOutcomeDiffToday = planFactList.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountTodayDiff).sum();
		Double totalOutcomeOver = planFactList.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountOver).sum();
		content = content + "<tr style='background-color:#EC7063'><td><b>Outcome</b></td><td><b>"
				+ amountToString(totalOutcomePlan) + "</b></td><td><b>" + amountToString(totalOutcomeFact)
				+ "</b></td><td><b>" + amountToString(totalOutcomeDiffToday) + "</b></td><td><b>"
				+ amountToString(totalOutcomeOver) + "</b></td><td><b><font color='gray'>"
				+ amountToString(totalOutcomePlan) + "</font></b></td><td><b><font color='gray'>"
				+ amountToString(totalOutcomePlan) + "</font></b></td></tr>";
		// - table with categories
		for (PlanFact planFact : planFactList) {
			if (planFact.getCategory().getType() != 2)
				continue;
			content = content + "<tr style='background-color:#FADBD8'><td><p style='margin-left:10px;'>"
					+ planFact.getCategory().getName() + "</p</td><td>" + amountToString(planFact.getAmountPlan())
					+ "</td><td>" + amountToString(planFact.getAmountFact()) + "</td><td>"
					+ amountToString(planFact.getAmountTodayDiff()) + "</td><td>"
					+ amountToString(planFact.getAmountOver()) + "</td><td><font color='gray'>"
					+ amountToString(planFact.getAmountPlan()) + "</font></td><td><font color='gray'>"
					+ amountToString(planFact.getAmountPlan()) + "</font></td></tr>";
		}

		// - Transfers
		// - caption with totals
		Double totalTransferFact = planFactList.stream().filter(pf -> pf.getCategory().getType() == 3)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalTransferToday = planFactList.stream().filter(pf -> pf.getCategory().getType() == 3)
				.mapToDouble(PlanFact::getAmountTodayDiff).sum();
		content = content + "<tr style='background-color:#85C1E9'><td><b>Transfer</b></td><td>-</td><td><b>"
				+ amountToString(totalTransferFact) + "</b></td><td><b>" + amountToString(totalTransferToday)
				+ "</b></td><td>-</td><td><b><font color='gray'>-</font></b></td><td><b><font color='gray'>-</font></b></td></tr>";

		return content;
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

	public static List<Account> skipUpdatedBankAccounts(List<Account> accounts, List<Total> prevTotals) {
		List<Account> result = new ArrayList<Account>();

		prevTotals.stream().filter(t -> !isDateToday(t.getDate())).forEach(t -> {
			if (accounts.contains(t.getAccount()))
				result.add(t.getAccount());
		});

		return result;
	}

	private static int getCurrentMonthInt() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		return cal.get(Calendar.MONTH);
	}

	private static String[] getCurrentQuaterMonths() {
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

	// SELENIUM
	public static String getLocatorForWebElement(WebElement we) {
		// assuming locator in xpath
		return we.toString().substring(we.toString().indexOf("->") + 10);
	}

}
