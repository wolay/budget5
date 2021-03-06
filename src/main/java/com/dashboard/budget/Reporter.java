package com.dashboard.budget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.DAO.BudgetSummary;
import com.dashboard.budget.DAO.Category;
import com.dashboard.budget.DAO.Credential;
import com.dashboard.budget.DAO.CreditScore;
import com.dashboard.budget.DAO.DataRetrievalStatus;
import com.dashboard.budget.DAO.PicturesStorage;
import com.dashboard.budget.DAO.PlanFact;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;
import com.sun.mail.smtp.SMTPMessage;

public class Reporter implements Config {

	private static Logger logger = LoggerFactory.getLogger(Reporter.class);
	private List<Transaction> allTransactions;
	private List<Total> totals;
	private List<BudgetSummary> budgetSummary;
	private List<CreditScore> creditScores;

	public Reporter(DataHandler dataHandler) {
		this.allTransactions = dataHandler.getAllTransactions();
		this.totals = dataHandler.getLastTotals();
		this.budgetSummary = dataHandler.getBudgetSummary();
		this.creditScores = dataHandler.getLastCreditScores();
	}

	public void sendEmailSummary(String spentTime, Credential credentials) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
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
			SMTPMessage message = new SMTPMessage(session);
			MimeMultipart messageContent = new MimeMultipart();
			MimeBodyPart mainPart = new MimeBodyPart();
			message.setFrom(new InternetAddress("aianitro@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(summaryReceiver));
			message.setSubject("Dashboard on " + Util.convertDateToStringType1(new Date()));

			// Budget
			// Showing summary (important to see as email short summary)
			String content = "Overall balance: <b>" + Util.amountToString(DataHandler.getFullTotal(totals)) + " </b>";

			Double fullDiff = DataHandler.getFullDiff(totals);
			if (fullDiff > 0)
				content = content + "(<font color='green'>+" + Util.amountToString(DataHandler.getFullDiff(totals))
						+ "</font>)";
			else if (fullDiff < 0)
				content = content + "(<font color='red'>" + Util.amountToString(DataHandler.getFullDiff(totals))
						+ "</font>)";
			else
				content = content + "(+0)";
			content = content + "<br><br>";

			int currentMonth = Util.getCurrentMonthInt();
			if (currentMonth == 1)
				content = content + getBudgetContentJanuary();
			else
				content = content + getBudgetContentAllYear(currentMonth - 1);
			content = content + "</tbody></table>";

			content = content + getAnnualTargetBalanceContent();

			content = content + getTransactionsContent();

			content = content + getUncategorizedContent();
			
			content = content + getQuestionableContent();
			
			content = content + getReimbursableContent();

			content = content + getTransfersContent();
			
			content = content + getOutOfBalanceContent();

			content = content + getYearPictureContent();

			content = content + getStatisticContent();

			content = content + getCreditScoresContent();

			// Time spent for retrieving
			content = content + "<P><font size='1' color='Gainsboro'><b>Time spent: </b>" + spentTime + "</font>";

			mainPart.setText(content, "US-ASCII", "html");
			messageContent.addBodyPart(mainPart);

			// Attaching pictures
			if(PicturesStorage.INSTANCE.size()>0){				
				PicturesStorage.INSTANCE.getAllPictures().stream().forEach(f -> {
					MimeBodyPart imagePart = new MimeBodyPart();
					try {
						imagePart.attachFile(f);
						messageContent.addBodyPart(imagePart);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}

			// Sending
			message.setContent(messageContent);
			Transport.send(message);
			logger.info("Summary sent to {}", summaryReceiver);

		} catch (MessagingException e)
		{
			throw new RuntimeException(e);
		}
	}

	private String getBudgetContentJanuary() {

		String content = "<table border='1' cellpadding='1' cellspacing='1' style='width:550px;'>"
				+ "<thead><tr style='color: gray'><th>Beginning balance</th>" + "<th colspan='4'>"
				+ Util.amountToString(budgetSummary.get(0).getAmountBegin()) + "</th><th>"
				+ Util.amountToString(budgetSummary.get(1).getAmountBegin()) + "</th><th>"
				+ Util.amountToString(budgetSummary.get(2).getAmountBegin()) + "</th></tr>"
				+ "<tr><th rowspan='2'>Category</th><th colspan='4'>" + budgetSummary.get(0).getMonthName()
				+ "</th><th rowspan='2'><font color='gray'>" + budgetSummary.get(1).getMonthName()
				+ "</font></th><th rowspan='2'><font color='gray'>" + budgetSummary.get(2).getMonthName()
				+ "</font></th></tr>" + "<tr><th>Plan</th><th>Fact</th><th>Today</th><th>Over</th></tr>" + "</thead>";

		content = content + "<tfoot><tr><td><b>TOTAL</b></td><td><b>"
				+ Util.amountToString(budgetSummary.get(0).getTotalBudgetPlan()) + "</b></td><td>-</td><td><b>"
				+ Util.amountToString(budgetSummary.get(0).getTotalDiffToday()) + "</b></td><td>"
				+ Util.amountToString(budgetSummary.get(0).getTotalBudgetOver()) + "</td><td><b><font color='gray'>"
				+ Util.amountToString(budgetSummary.get(1).getTotalBudgetPlan())
				+ "</font></b></td><td><b><font color='gray'>"
				+ Util.amountToString(budgetSummary.get(2).getTotalBudgetPlan()) + "</font></b></td></tr>"
				+ "<tr><td rowspan='2'><b>By the end of month</b></td><td colspan='4' align='center'><b><font size='4'>"
				+ Util.amountToStringWithSign(budgetSummary.get(0).getTotalMonthDynamic())
				+ "</font></b></td><td align='center'><b><font size='4'>"
				+ Util.amountToStringWithSign(budgetSummary.get(1).getTotalBudgetPlan())
				+ "</font></b></td><td align='center'><b><font size='4'>"
				+ Util.amountToStringWithSign(budgetSummary.get(2).getTotalBudgetPlan()) + "</font></b></td></tr>"
				+ "<tr><td colspan='4' align='center'><b><font size='4'>"
				+ Util.amountToString(budgetSummary.get(0).getAmountEnd())
				+ "</b></td><td align='center'><b><font size='4'>"
				+ Util.amountToString(budgetSummary.get(1).getAmountEnd())
				+ "</font></b></td><td align='center'><b><font size='4'>"
				+ Util.amountToString(budgetSummary.get(2).getAmountEnd()) + "</font></b></td></tr></font>"
				+ "</tfoot><tbody>";
		// Grouping by type
		// - Income
		// - caption with totals
		List<PlanFact> planFact0 = budgetSummary.get(0).getPlanFactList(); // this
																			// month
		List<PlanFact> planFact1 = budgetSummary.get(1).getPlanFactList(); // next
																			// month
		List<PlanFact> planFact2 = budgetSummary.get(2).getPlanFactList(); // month
																			// after
																			// next

		Double totalIncomePlan = planFact0.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountPlan).sum();
		Double totalIncomeFact = planFact0.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalIncomeDiffToday = planFact0.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountTodayDiff).sum();
		Double totalIncomeOver = planFact0.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountOver).sum();
		content = content + "<tr style='background-color:#27AE60'><td><b>Income</b></td><td><b>"
				+ Util.amountToString(totalIncomePlan) + "</b></td><td><b>" + Util.amountToString(totalIncomeFact)
				+ "</b></td><td><b>" + Util.amountToString(totalIncomeDiffToday) + "</b></td><td><b>"
				+ Util.amountToString(totalIncomeOver) + "</b></td><td><b><font color='gray'>"
				+ Util.amountToString(totalIncomePlan) + "</font></b></td><td><b><font color='gray'>"
				+ Util.amountToString(totalIncomePlan) + "</font></b></td></tr>";
		// - table with categories
		for (PlanFact planFact : planFact0) {
			if (planFact.getCategory().getType() != 1)
				continue;
			content = content + "<tr style='background-color:#D5F5E3'><td><p style='margin-left:10px;'>"
					+ planFact.getCategory().getName() + "</p</td><td>" + Util.amountToString(planFact.getAmountPlan())
					+ "</td><td>" + Util.amountToString(planFact.getAmountFact()) + "</td><td>"
					+ Util.amountToString(planFact.getAmountTodayDiff()) + "</td><td>"
					+ Util.amountToString(planFact.getAmountOver()) + "</td><td><font color='gray'>"
					+ Util.amountToString(planFact.getAmountPlan()) + "</font></td><td><font color='gray'>"
					+ Util.amountToString(planFact.getAmountPlan()) + "</font></td></tr>";
		}

		// - Outcome
		// - caption with totals
		Double totalOutcomePlan0 = planFact0.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountPlan).sum();
		Double totalOutcomePlan1 = planFact1.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountPlan).sum();
		Double totalOutcomePlan2 = planFact2.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountPlan).sum();
		Double totalOutcomeFact = planFact0.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalOutcomeDiffToday = planFact0.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountTodayDiff).sum();
		Double totalOutcomeOver = planFact0.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountOver).sum();
		content = content + "<tr style='background-color:#EC7063'><td><b>Outcome</b></td><td><b>"
				+ Util.amountToString(totalOutcomePlan0) + "</b></td><td><b>" + Util.amountToString(totalOutcomeFact)
				+ "</b></td><td><b>" + Util.amountToString(totalOutcomeDiffToday) + "</b></td><td><b>"
				+ Util.amountToString(totalOutcomeOver) + "</b></td><td><b><font color='gray'>"
				+ Util.amountToString(totalOutcomePlan1) + "</font></b></td><td><b><font color='gray'>"
				+ Util.amountToString(totalOutcomePlan2) + "</font></b></td></tr>";
		// - table with categories
		for (PlanFact planFact : planFact0) {
			if (planFact.getCategory().getType() != 2 || !planFact.getCategory().getIsActive())
				continue;
			Double plan1 = 0.0;
			Double plan2 = 0.0;
			if (!planFact.getCategory().getName().equals("Unrecognized")) {
				plan1 = planFact1.stream().filter(pf -> pf.getCategory().equals(planFact.getCategory())).findFirst()
						.get().getAmountPlan();
				plan2 = planFact2.stream().filter(pf -> pf.getCategory().equals(planFact.getCategory())).findFirst()
						.get().getAmountPlan();
			}

			content = content + "<tr style='background-color:#FADBD8'><td><p style='margin-left:10px;'>"
					+ planFact.getCategory().getName() + "</p</td><td>" + Util.amountToString(planFact.getAmountPlan())
					+ "</td><td>" + Util.amountToString(planFact.getAmountFact()) + "</td><td>"
					+ Util.amountToString(planFact.getAmountTodayDiff()) + "</td><td>"
					+ Util.amountToString(planFact.getAmountOver()) + "</td><td><font color='gray'>"
					+ Util.amountToString(plan1) + "</font></td><td><font color='gray'>" + Util.amountToString(plan2)
					+ "</font></td></tr>";
		}

		// - Transfers
		// - caption with totals
		Double totalTransferFact = planFact0.stream().filter(pf -> pf.getCategory().getType() == 3)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalTransferToday = planFact0.stream().filter(pf -> pf.getCategory().getType() == 3)
				.mapToDouble(PlanFact::getAmountTodayDiff).sum();
		content = content + "<tr style='background-color:#85C1E9'><td><b>Transfer</b></td><td>-</td><td><b>"
				+ Util.amountToString(totalTransferFact) + "</b></td><td><b>" + Util.amountToString(totalTransferToday)
				+ "</b></td><td>-</td><td><b><font color='gray'>-</font></b></td><td><b><font color='gray'>-</font></b></td></tr>";

		return content;
	}

	private String getBudgetContentAllYear(int currentMonth) {

		String content = "<table border='1' cellpadding='1' cellspacing='1' style='width:550px;'>"
				+ "<thead><tr style='color: gray'><th>Beginning balance</th>" + "<th>"
				+ Util.amountToString(budgetSummary.get(currentMonth - 1).getAmountBegin()) + "</th><th colspan='4'>"
				+ Util.amountToString(budgetSummary.get(currentMonth).getAmountBegin()) + "</th><th>"
				+ Util.amountToString(budgetSummary.get(currentMonth + 1).getAmountBegin()) + "</th></tr>"
				+ "<tr><th rowspan='2'>Category</th><th rowspan='2'><font color='gray'>"
				+ budgetSummary.get(currentMonth - 1).getMonthName() + "</font></th><th colspan='4'>"
				+ budgetSummary.get(currentMonth).getMonthName() + "</th><th rowspan='2'><font color='gray'>"
				+ budgetSummary.get(currentMonth + 1).getMonthName() + "</font></th></tr>"
				+ "<tr><th>Plan</th><th>Fact</th><th>Today</th><th>Over</th></tr>" + "</thead>";

		content = content + "<tfoot><tr><td><b>TOTAL</b></td><td><b><font color='gray'>"
				+ Util.amountToString(budgetSummary.get(currentMonth - 1).getTotalMonthDynamic())
				+ "</font></b></td><td><b>" + Util.amountToString(budgetSummary.get(currentMonth).getTotalBudgetPlan())
				+ "</b></td><td><b>-</b></td><td><b>"
				+ Util.amountToString(budgetSummary.get(currentMonth).getTotalDiffToday()) + "</b></td><td><b>"
				+ Util.amountToString(budgetSummary.get(currentMonth).getTotalBudgetOver())
				+ "</b></td><td><b><font color='gray'>"
				+ Util.amountToString(budgetSummary.get(currentMonth + 1).getTotalBudgetPlan())
				+ "</font></b></td></tr>"
				+ "<tr><td rowspan='2'><b>By the end of month</b></td><td align='center'><b><font size='4'>"
				+ Util.amountToStringWithSign(budgetSummary.get(currentMonth - 1).getTotalMonthDynamic())
				+ "</font></b></td><td colspan='4' align='center'><b><font size='4'>"
				+ Util.amountToStringWithSign(budgetSummary.get(currentMonth).getTotalMonthDynamic())
				+ "</font></b></td><td align='center'><b><font size='4'>"
				+ Util.amountToStringWithSign(budgetSummary.get(currentMonth + 1).getTotalBudgetPlan())
				+ "</font></b></td></tr>" + "<tr><td align='center'><b><font size='4'>"
				+ Util.amountToString(budgetSummary.get(currentMonth - 1).getAmountEnd())
				+ "</b></td><td colspan='4' align='center'><b><font size='4'>"
				+ Util.amountToString(budgetSummary.get(currentMonth).getAmountEnd())
				+ "</font></b></td><td align='center'><b><font size='4'>"
				+ Util.amountToString(budgetSummary.get(currentMonth + 1).getAmountEnd())
				+ "</font></b></td></tr></font>" + "</tfoot><tbody>";
		// Grouping by type
		// - Income
		// - caption with totals
		List<PlanFact> planFact0 = budgetSummary.get(currentMonth - 1).getPlanFactList(); // previous
		// month
		List<PlanFact> planFact1 = budgetSummary.get(currentMonth).getPlanFactList(); // this
		// month
		List<PlanFact> planFact2 = budgetSummary.get(currentMonth + 1).getPlanFactList(); // next
		// months

		Double totalIncomeFact0 = planFact0.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalIncomePlan1 = planFact1.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountPlan).sum();
		Double totalIncomeFact1 = planFact1.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalIncomeDiffToday1 = planFact1.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountTodayDiff).sum();
		Double totalIncomeOver1 = planFact1.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountOver).sum();
		Double totalIncomePlan2 = planFact2.stream().filter(pf -> pf.getCategory().getType() == 1)
				.mapToDouble(PlanFact::getAmountPlan).sum();
		content = content + "<tr style='background-color:#27AE60'><td><b>Income</b></td><td><b><font color='gray'>"
				+ Util.amountToString(totalIncomeFact0) + "</font></b></td><td><b>"
				+ Util.amountToString(totalIncomePlan1) + "</b></td><td><b>" + Util.amountToString(totalIncomeFact1)
				+ "</b></td><td><b>" + Util.amountToString(totalIncomeDiffToday1) + "</b></td><td><b>"
				+ Util.amountToString(totalIncomeOver1) + "</b></td><td><b><font color='gray'>"
				+ Util.amountToString(totalIncomePlan2) + "</font></b></td></tr>";

		// - table with income categories
		Collections.sort(planFact0);
		for (PlanFact planFact : planFact1) {
			if (planFact.getCategory().getType() != 1)
				continue;
			Double incomeFact0 = planFact0.stream().filter(pf -> pf.getCategory() == planFact.getCategory())
					.mapToDouble(PlanFact::getAmountFact).sum();
			Double incomePlan2 = planFact2.stream().filter(pf -> pf.getCategory() == planFact.getCategory())
					.mapToDouble(PlanFact::getAmountPlan).sum();
			content = content + "<tr style='background-color:#D5F5E3'><td><p style='margin-left:10px;'>"
					+ planFact.getCategory().getName() + "</p</td><td><font color='gray'>"
					+ Util.amountToString(incomeFact0) + "</font></td><td>"
					+ Util.amountToString(planFact.getAmountPlan()) + "</td><td>"
					+ Util.amountToString(planFact.getAmountFact()) + "</td><td>"
					+ Util.amountToString(planFact.getAmountTodayDiff()) + "</td><td>"
					+ Util.amountToString(planFact.getAmountOver()) + "</td><td><font color='gray'>"
					+ Util.amountToString(incomePlan2) + "</font></td></tr>";
		}

		// - Outcome
		// - caption with totals
		Double totalOutcomeFact0 = planFact0.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalOutcomePlan1 = planFact1.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountPlan).sum();
		Double totalOutcomeFact1 = planFact1.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalOutcomeDiffToday1 = planFact1.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountTodayDiff).sum();
		Double totalOutcomeOver1 = planFact1.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountOver).sum();
		Double totalOutcomePlan2 = planFact2.stream().filter(pf -> pf.getCategory().getType() == 2)
				.mapToDouble(PlanFact::getAmountPlan).sum();
		content = content + "<tr style='background-color:#EC7063'><td><b>Outcome</b></td><td><b><font color='gray'>"
				+ Util.amountToString(totalOutcomeFact0) + "</font></b></td><td><b>"
				+ Util.amountToString(totalOutcomePlan1) + "</b></td><td><b>" + Util.amountToString(totalOutcomeFact1)
				+ "</b></td><td><b>" + Util.amountToString(totalOutcomeDiffToday1) + "</b></td><td><b>"
				+ Util.amountToString(totalOutcomeOver1) + "</b></td><td><b><font color='gray'>"
				+ Util.amountToString(totalOutcomePlan2) + "</font></b></td></tr>";
		// - table with categories
		for (PlanFact planFact : planFact1) {
			if (planFact.getCategory().getType() != 2 || !planFact.getCategory().getIsActive())
				continue;
			Double outcomeFact0 = 0.0;
			Double outcomePlan2 = 0.0;
			if (!planFact.getCategory().getName().equals("Unrecognized")) {
				outcomeFact0 = planFact0.stream().filter(pf -> pf.getCategory().equals(planFact.getCategory()))
						.mapToDouble(PlanFact::getAmountFact).sum();
				outcomePlan2 = planFact2.stream().filter(pf -> pf.getCategory().equals(planFact.getCategory()))
						.mapToDouble(PlanFact::getAmountPlan).sum();
			}

			content = content + "<tr style='background-color:#FADBD8'><td><p style='margin-left:10px;'>"
					+ planFact.getCategory().getName() + "</p</td><td><font color='gray'>"
					+ Util.amountToString(outcomeFact0) + "</font></td><td>"
					+ Util.amountToString(planFact.getAmountPlan()) + "</td><td>"
					+ Util.amountToString(planFact.getAmountFact()) + "</td><td>"
					+ Util.amountToString(planFact.getAmountTodayDiff()) + "</td><td>"
					+ Util.amountToString(planFact.getAmountOver()) + "</td><td><font color='gray'>"
					+ Util.amountToString(outcomePlan2) + "</font></td></tr>";
		}

		// - Transfers
		// - caption with totals
		Double totalTransferFact = planFact0.stream().filter(pf -> pf.getCategory().getType() == 3)
				.mapToDouble(PlanFact::getAmountFact).sum();
		Double totalTransferToday = planFact0.stream().filter(pf -> pf.getCategory().getType() == 3)
				.mapToDouble(PlanFact::getAmountTodayDiff).sum();
		content = content + "<tr style='background-color:#85C1E9'><td><b>Transfer</b></td><td>-</td><td><b>"
				+ Util.amountToString(totalTransferFact) + "</b></td><td><b>" + Util.amountToString(totalTransferToday)
				+ "</b></td><td>-</td><td><b><font color='gray'>-</font></b></td><td><b><font color='gray'>-</font></b></td></tr>";

		return content;
	}

	// Annual target balance
	private String getAnnualTargetBalanceContent() {

		Double annualTarget = budgetSummary.get(11).getAmountEnd();
		boolean isPresentUncategorizedTransaction = allTransactions.stream()
				.filter(t -> t.getCategory() != null && t.getCategory().getId() == 19).findAny().isPresent();

		String content = "<p>Annual target balance: <b>";
		if (isPresentUncategorizedTransaction)
			content = content + "N/A</b> (uncategorized)";
		else
			content = content + Util.amountToString(annualTarget) + "</b>(<font color='green'>+"
					+ Util.amountToString(annualTarget - budgetSummary.get(0).getAmountBegin()) + "</font>)<tr><tr>";

		return content;
	}

	private String getTransactionsContent() {

		String content = "<P><b>Totals & transactions: </b>";
		content = content
				+ "<tr><table border='1' cellpadding='1' cellspacing='1' style='width:550px;'><thead><tr><th>Date</th><th>Account</th><th>Amount</th><th>Diff</th><th>?</th></tr></thead>";
		content = content + "<tfoot><tr><td></td><td><b>TOTAL</b></td><td><b>"
				+ Util.amountToString(DataHandler.getFullTotal(totals)) + "</b></td><td><b>"
				+ Util.amountToString(DataHandler.getFullDiff(totals)) + "</b></td><td></td></tr></tfoot><tbody>";
		Collections.sort(totals);
		for (Total total : totals) {
			if (!total.getAccount().getIsEnabled())
				continue;
			content = content + "<tr style='background-color:" + getStatusColor(total) + "'><td>"
					+ Util.formatDateForEmail(total.getDate()) + "</td><td><a href='" + total.getAccount().getUrl()
					+ "'>" + total.getAccount().getName() + "</a>";
			List<Transaction> transactions = total.getTransactions();
			if (transactions != null)
				Collections.sort(transactions);
			if (transactions != null && transactions.size() > 0) {
				content = content + "<br><table border='0' cellpadding='1' cellspacing='1' style='width:100%;'>";
				for (Transaction transaction : total.getTransactions()) {
					content = content + "<tr><td width='10'><font size='1'>"
							+ Util.convertDateToStringType2(transaction.getDate()) + "</font></td><td><font size='1'>"
							+ ((transaction.getCategory() == null) ? "" : transaction.getCategory().getName())
							+ "</font></td><td><font size='1'>" + transaction.getDecription()
							+ "</font></td><td width='11'><font size='1'>"
							+ Util.amountToString(transaction.getAmount()) + "</font></td></tr>";
				}
				content = content + "</table>";
			}
			content = content + "</td><td>" + Util.amountToString(total.getAmount()) + "</td><td>"
					+ Util.amountToString(total.getDifference()) + "</td>";

			// Status column
			if (total.getStatus() == null)
				content = content + "<td title='Status is 'null''>!</td>";
			else
				content = content + "<td title='" + total.getStatus()
						+ ((total.getErrorMessage() == null) ? "" : (": " + total.getErrorMessage())) + "'>"
						+ total.getStatus().getAbbr() + "</td></tr>";

			content = content + "</tr>";
		}

		return content + "</tbody></table>";
	}

	private String getUncategorizedContent() {
		List<Transaction> uncategorized = new ArrayList<Transaction>();
		int allTransactionsCounter = (int) allTransactions.stream().count();
		allTransactions.stream().forEach(t -> {
			if (t.getCategory() != null && t.getCategory().getName().equals("Unrecognized"))
				uncategorized.add(t);
		});

		if (uncategorized.isEmpty())
			return "<P><b>Great news! There is no uncategorized transactions this month</b>";
		else {
			String content = "<P><b>Uncategorized transactions: </b>"
					+ uncategorized.size() * 100 / allTransactionsCounter + "% (" + uncategorized.size() + ")";
			content = content + "<br><table border='0' cellpadding='1' cellspacing='1' style='width:500px;'><tbody>";
			for (Transaction transaction : uncategorized) {
				content = content + "<tr><td><font size='1'>" + transaction.getAccount().getName()
						+ "</font></td><td><font size='1'>" + Util.convertDateToStringType2(transaction.getDate())
						+ "</font></td><td><font size='1'>" + transaction.getDecription()
						+ "</font></td><td width='10'><font size='1'>" + transaction.getCategoryStr()
						+ "</font></td></tr>";
			}
			return content + "</tbody></table>";
		}
	}
	
	private String getQuestionableContent() {
		List<Transaction> transfers = new ArrayList<Transaction>();
		allTransactions.stream().filter(t -> t.getCategory() != null && t.getCategory().getName().equals("Questions")
				&& !t.getIsTransferComplete()).forEach(t -> transfers.add(t));
		if (transfers.isEmpty())
			return "";
		else {
			String content = "<P><b>Questionable transactions: </b> (" + transfers.size() + ")";
			content = content + "<br><table border='0' cellpadding='1' cellspacing='1' style='width:500px;'><tbody>";
			for (Transaction transfer : transfers) {
				content = content + "<tr><td><font size='1'>" + transfer.getAccount().getName()
						+ "</font></td><td><font size='1'>" + Util.convertDateToStringType2(transfer.getDate())
						+ "</font></td><td><font size='1'>" + transfer.getDecription()
						+ "</font></td><td width='10'><font size='1'>" + transfer.getAmount() + "</font></td></tr>";
			}
			return content + "</tbody></table>";
		}
	}
	
	private String getReimbursableContent() {
		List<Transaction> transfers = new ArrayList<Transaction>();
		allTransactions.stream().filter(t -> t.getCategory() != null && t.getCategory().getName().equals("To reimburse")
				&& !t.getIsTransferComplete()).forEach(t -> transfers.add(t));
		if (transfers.isEmpty())
			return "";
		else {
			String content = "<P><b>Transactions to reimburse: </b> (" + transfers.size() + ")";
			content = content + "<br><table border='0' cellpadding='1' cellspacing='1' style='width:500px;'><tbody>";
			for (Transaction transfer : transfers) {
				content = content + "<tr><td><font size='1'>" + transfer.getAccount().getName()
						+ "</font></td><td><font size='1'>" + Util.convertDateToStringType2(transfer.getDate())
						+ "</font></td><td><font size='1'>" + transfer.getDecription()
						+ "</font></td><td width='10'><font size='1'>" + transfer.getAmount() + "</font></td></tr>";
			}
			return content + "</tbody></table>";
		}
	}

	private String getTransfersContent() {
		List<Transaction> transfers = new ArrayList<Transaction>();
		allTransactions.stream().filter(t -> t.getCategory() != null && t.getCategory().getName().equals("Transfer")
				&& !t.getIsTransferComplete()).forEach(t -> transfers.add(t));
		if (transfers.isEmpty())
			return "<P><b>There is no transferring transactions</b>";
		else {
			String content = "<P><b>Transactions in transfer: </b> (" + transfers.size() + ")";
			content = content + "<br><table border='0' cellpadding='1' cellspacing='1' style='width:500px;'><tbody>";
			for (Transaction transfer : transfers) {
				content = content + "<tr><td><font size='1'>" + transfer.getAccount().getName()
						+ "</font></td><td><font size='1'>" + Util.convertDateToStringType2(transfer.getDate())
						+ "</font></td><td><font size='1'>" + transfer.getDecription()
						+ "</font></td><td width='10'><font size='1'>" + transfer.getAmount() + "</font></td></tr>";
			}
			return content + "</tbody></table>";
		}
	}
	
	private String getOutOfBalanceContent() {
		List<Transaction> transfers = new ArrayList<Transaction>();
		allTransactions.stream().filter(t -> t.getCategory() != null && t.getCategory().getName().equals("Out balance")
				&& !t.getIsTransferComplete()).forEach(t -> transfers.add(t));
		if (transfers.isEmpty())
			return "";
		else {
			String content = "<P><b>Out of balance transactions: </b> (" + transfers.size() + ")";
			content = content + "<br><table border='0' cellpadding='1' cellspacing='1' style='width:500px;'><tbody>";
			for (Transaction transfer : transfers) {
				content = content + "<tr><td><font size='1'>" + transfer.getAccount().getName()
						+ "</font></td><td><font size='1'>" + Util.convertDateToStringType2(transfer.getDate())
						+ "</font></td><td><font size='1'>" + transfer.getDecription()
						+ "</font></td><td width='10'><font size='1'>" + transfer.getAmount() + "</font></td></tr>";
			}
			return content + "</tbody></table>";
		}
	}

	private String getYearPictureContent() {

		String content = "<P><b>Whole year: </b>";
		content = content + "<br><table border='1' cellpadding='1' cellspacing='1' style='width:550px;'><tbody>";
		// caption
		content = content + "<tr><td></td><td>Total</td>";
		for (BudgetSummary oneMonthBudget : budgetSummary)
			content = content + "<td><font size='1'>" + oneMonthBudget.getMonthName() + "</font></td>";
		content = content + "</tr>";

		// rows
		// beginning balance
		content = content + "<tr><td>Begining</td><td></td>";
		for (BudgetSummary oneMonthBudget : budgetSummary)
			content = content + "<td><font size='1'>" + Util.amountToString(oneMonthBudget.getAmountBegin())
					+ "</font></td>";
		content = content + "</tr>";
		// income
		for (PlanFact planFact : budgetSummary.get(0).getPlanFactList()) {
			Category curCategory = planFact.getCategory();
			if (curCategory.getType() != 1)
				continue;
			content = content + "<tr><td><font size='1'>" + curCategory.getName() + "</font></td>";
			Double amountTotalFact = budgetSummary.stream().mapToDouble(m -> m.getPlanFactList().stream()
					.filter(pf -> pf.getCategory() == curCategory).mapToDouble(PlanFact::getAmountFact).sum()).sum();
			content = content + "<td><font size='1'>" + Util.amountToString(amountTotalFact) + "</font></td>";
			for (BudgetSummary oneMonthBudget : budgetSummary) {
				Double amountFact = oneMonthBudget.getPlanFactList().stream()
						.filter(pf -> pf.getCategory() == curCategory).mapToDouble(PlanFact::getAmountFact).sum();
				content = content + "<td><font size='1'>" + Util.amountToString(amountFact) + "</font></td>";
			}
			content = content + "</tr>";
		}
		// outcome
		for (PlanFact planFact : budgetSummary.get(0).getPlanFactList()) {
			Category curCategory = planFact.getCategory();
			if (curCategory.getType() != 2 || !curCategory.getIsActive())
				continue;
			content = content + "<tr><td><font size='1'>" + curCategory.getName() + "</font></td>";
			Double amountTotalFact = budgetSummary.stream().mapToDouble(m -> m.getPlanFactList().stream()
					.filter(pf -> pf.getCategory() == curCategory).mapToDouble(PlanFact::getAmountFact).sum()).sum();
			content = content + "<td><font size='1'>" + Util.amountToString(amountTotalFact) + "</font></td>";			
			for (BudgetSummary oneMonthBudget : budgetSummary) {
				Double amountFact = oneMonthBudget.getPlanFactList().stream()
						.filter(pf -> pf.getCategory() == curCategory).mapToDouble(PlanFact::getAmountFact).sum();
				content = content + "<td><font size='1'>" + Util.amountToString(amountFact) + "</font></td>";
			}
			content = content + "</tr>";
		}
		// ending balance
		content = content + "<tr><td>Ending</td><td></td>";
		for (BudgetSummary oneMonthBudget : budgetSummary)
			content = content + "<td><font size='1'>" + Util.amountToString(oneMonthBudget.getAmountEnd())
					+ "</font></td>";
		content = content + "</tr>";

		return content + "</tbody></table>";
	}

	private String getStatisticContent() {

		String content = "<P><b>Statistic (last 100 transactions): </b>";

		// Retrieving last 100 transactions
		List<Transaction> lastHundredTransactions = allTransactions.stream()
				.sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate())).filter(t -> t.getCategorizationRule() != null)
				.limit(100).collect(Collectors.toList());

		// Showing failing categorization rules
		content = content + "<p><font size='1'>Failing rules:</font>"
				+ "<br><table border='0' cellpadding='1' cellspacing='1' style='width:200px;'>"
				+ "<thead><tr><th><font size='1'>Rule id</font></th><th><font size='1'># of transactions</font></th>"
				+ "</tr></thead><tbody>";
		Map<Integer, Long> failingCategorizationRules = lastHundredTransactions.stream()
				.filter(t -> t.getCategory() != t.getCategorizationRule().getTargetCategory())
				.collect(Collectors.groupingBy(t -> t.getCategorizationRule().getId(),
						Collectors.mapping(t -> t, Collectors.counting())));
		for (Map.Entry<Integer, Long> entry : failingCategorizationRules.entrySet()) {
			content = content + "<tr><td><font size='1'>" + entry.getKey() + "</font></td><td><font size='1'>"
					+ entry.getValue() + "</font></td></tr>";
		}
		content = content + "</tbody></table>";

		// Categories as in rule
		int categoriesAsInRule = (int) lastHundredTransactions.stream()
				.filter(t -> t.getCategory() == t.getCategorizationRule().getTargetCategory()).count();
		content = content + "<p><font size='1'>Categories as in rule: <b>" + categoriesAsInRule + "%</b></font>";

		content = content + "<br><table border='0' cellpadding='1' cellspacing='1' style='width:600px;'>"
				+ "<thead><tr><th><font size='1'>Date</font></th><th><font size='1'>Amount</font></th>"
				+ "<th><font size='1'>Description</font></th><th><font size='1'>Category (rule)</font></th>"
				+ "<th><font size='1'>Rule</font></th><th><font size='1'>Category (fact)</font></th></tr></thead><tbody>";
		for (Transaction t : lastHundredTransactions) {
			content = content + "<tr><td><font size='1'>" + t.getDate() + "</font></td><td><font size='1'>"
					+ t.getAmount() + "</font></td><td><font size='1'>" + t.getDecription()
					+ "</font></td><td><font size='1'>"
					+ ((t.getCategorizationRule() == null) ? ""
							: t.getCategorizationRule().getTargetCategory().getName())
					+ "</font></td><td><font size='1'>" + t.getCategorizationRule().getId()
					+ "</font></td><td><font size='1'>" + t.getCategory().getName() + "</font></td></tr>";
		}

		return content + "</tbody></table>";
	}

	private String getCreditScoresContent() {
		String content = "<P><b>Credit scores: </b>";
		for (CreditScore creditScore : creditScores) {
			content = content + "<br>" + creditScore.getName() + ": " + creditScore.getScore();
			if (creditScore.getDifference() > 0)
				content = content + "(<font color='green'>+" + creditScore.getDifference() + "</font>)";
			else if (creditScore.getDifference() < 0)
				content = content + "(<font color='red'>" + creditScore.getDifference() + "</font>)";
			else
				content = content + "(+" + creditScore.getDifference() + ")";
		}
		return content;
	}

	private String getStatusColor(Total total) {
		if (total.getStatus() == DataRetrievalStatus.SUCCESS)
			return "#BCE954";
		else if (total.getStatus() == DataRetrievalStatus.NO_MATCH_FOR_TOTAL)
			return "#E6FFAA";
		else if (total.getStatus() == DataRetrievalStatus.NAVIGATION_BROKEN)
			return "#FF7F50";
		else if (total.getStatus() == DataRetrievalStatus.SERVICE_UNAVAILABLE)
			return "#FFBDBD";
		else if (total.getStatus() == DataRetrievalStatus.UNKNOWN)
			return "#FFE87C";
		else
			return "white";
	}

}
