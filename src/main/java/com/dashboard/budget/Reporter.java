package com.dashboard.budget;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dashboard.budget.DAO.BudgetPlan;
import com.dashboard.budget.DAO.Credential;
import com.dashboard.budget.DAO.CreditScore;
import com.dashboard.budget.DAO.PlanFact;
import com.dashboard.budget.DAO.BudgetSummary;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;

public class Reporter implements Config {

	private static Logger logger = LoggerFactory.getLogger(Reporter.class);
	private DataHandler dataHandler;
	private List<Transaction> allTransactions;
	private List<Transaction> todayTransactions = new ArrayList<Transaction>();
	private List<Total> totals;
	private List<BudgetPlan> budgetPlans;
	private List<BudgetSummary> budgetSummary = new ArrayList<BudgetSummary>();
	private List<CreditScore> creditScores;

	public Reporter(DataHandler dataHandler) {
		this.dataHandler = dataHandler;
		this.allTransactions = dataHandler.getAllTransactions();
		this.totals = dataHandler.getLastTotals();
		// populating list of todays transactions
		totals.stream().filter(to -> to.getTransactions() != null)
				.forEach(to -> this.todayTransactions.addAll(to.getTransactions()));
		this.budgetPlans = dataHandler.getBudgetPlansList();
		this.creditScores = dataHandler.getLastCreditScores();
	}

	public void sendEmailSummary(String spentTime, Credential credentials) {
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

			generateDataSet();

			content = content + getBudgetContent();

			content = content + "</tbody></table>";

			// Annual target balance
			content = content + "<p>Annual target balance: <b>"
					+ Util.amountToString(budgetSummary.get(11).getAmountEnd()) + "</b>";
			content = content + "(<font color='green'>+"
					+ Util.amountToString(budgetSummary.get(11).getAmountEnd() - budgetSummary.get(0).getAmountBegin())
					+ "</font>)<tr><tr>";

			content = content + getTransactionsContent();

			content = content + getUncategorizedContent();

			content = content + getTransfersContent();

			content = content + getCreditScoresContent();

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

	private void generateDataSet() {

		int currentMonthInt = Util.getCurrentMonthInt();

		// Storage for all budget length months (even closed)
		// This concept allows to implement full year print out
		budgetSummary = new ArrayList<BudgetSummary>();

		for (int i = 1; i <= 12; i++) {
			Double monthBeginBalance = 0.0;
			Double totalBudgetPlan = 0.0;
			Double totalBudgetOver = 0.0;
			Double totalDiffToday = 0.0;
			Double totalMonthDynamic = 0.0;
			Double monthEndBalance = 0.0;
			List<PlanFact> planFact = new ArrayList<PlanFact>();

			if (i < currentMonthInt) {
				// closed month
			} else if (i == currentMonthInt) {
				// current month
				monthBeginBalance = 26025.0;
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
						amountPlan = budgetPlan.getAmount() / budgetPlan.getLength();
					Double amountOver = 0.0;
					if (category.getType() == 1 && amountFact - amountPlan > 0)
						amountOver = amountFact - amountPlan;
					else if (category.getType() == 2 && amountPlan - amountFact > 0)
						amountOver = amountFact - amountPlan;

					planFact.add(new PlanFact(category, null, amountPlan, amountFact, amountDiffToday, amountOver));
				});
				Collections.sort(planFact);

				totalBudgetPlan = planFact.stream().mapToDouble(PlanFact::getAmountPlan).sum();
				totalBudgetOver = planFact.stream().mapToDouble(PlanFact::getAmountOver).sum();
				totalDiffToday = planFact.stream().mapToDouble(PlanFact::getAmountTodayDiff).sum();
				totalMonthDynamic = totalBudgetPlan + totalBudgetOver;
				monthEndBalance = monthBeginBalance + totalMonthDynamic;
			} else {
				// future month
				BudgetSummary currentMonth = budgetSummary.get(currentMonthInt - 1);
				dataHandler.getCategories().stream().forEach(category -> {
					BudgetPlan budgetPlan = budgetPlans.stream().filter(b -> b.getCategory() == category).findFirst()
							.orElse(null);
					if (budgetPlan != null) {
						Double amountPrevFact = currentMonth.getPlanFactList().stream()
								.filter(pf -> pf.getCategory().equals(category)).findFirst().get().getAmountFact();
						Double amountPrevOver = currentMonth.getPlanFactList().stream()
								.filter(pf -> pf.getCategory().equals(category)).findFirst().get().getAmountOver();
						Double amountPlan;
						if (amountPrevOver < 0)
							amountPlan = (budgetPlan.getAmount() - amountPrevFact)
									/ (budgetPlan.getLength() - currentMonthInt);
						else
							amountPlan = budgetPlan.getAmount() / budgetPlan.getLength();
						planFact.add(new PlanFact(category, null, amountPlan, 0.0, 0.0, 0.0));

					}
				});
				Collections.sort(planFact);

				monthBeginBalance = budgetSummary.get(i - 2).getAmountEnd();
				totalBudgetPlan = planFact.stream().mapToDouble(PlanFact::getAmountPlan).sum();
				monthEndBalance = monthBeginBalance + totalBudgetPlan;
			}

			budgetSummary.add(new BudgetSummary(Util.getMonth(i), monthBeginBalance, totalBudgetPlan, totalBudgetOver,
					totalDiffToday, totalMonthDynamic, monthEndBalance, planFact));
		}

		budgetSummary.stream().forEach(bs -> System.out.println(bs));

		/*
		 * String[] quater = Util.getCurrentQuaterMonths();
		 * 
		 * List<PlanFact> planFactList = new ArrayList<PlanFact>();
		 * 
		 * // THIS MONTH
		 * 
		 * // collecting table of plan-fact by category for this month LocalDate
		 * today = LocalDate.now();
		 * dataHandler.getCategories().stream().forEach(category ->
		 * 
		 * { Double amountFact = allTransactions.stream().filter( t ->
		 * t.getCategory() == category && Util.isDateThisMonth(t.getDate()) &&
		 * !t.getIsTransferComplete())
		 * .mapToDouble(Transaction::getAmount).sum(); Double amountDiffToday =
		 * todayTransactions.stream() .filter(t -> t.getCategory() == category
		 * && !t.getIsTransferComplete())
		 * .mapToDouble(Transaction::getAmount).sum(); BudgetPlan budgetPlan =
		 * budgetPlans.stream().filter(b -> b.getCategory() ==
		 * category).findFirst() .orElse(null); Double amountPlan = 0.0; if
		 * (budgetPlan == null) logger.info(
		 * "Category '{}' is not in budget plan", category.getName()); else
		 * amountPlan = budgetPlan.getAmount() / budgetPlan.getLength(); Double
		 * amountOver = 0.0; if (category.getType() == 1 && amountFact -
		 * amountPlan > 0) amountOver = amountFact - amountPlan; else if
		 * (category.getType() == 2 && amountPlan - amountFact > 0) amountOver =
		 * amountFact - amountPlan;
		 * 
		 * planFactList.add(new PlanFact(category, today.withDayOfMonth(1),
		 * amountPlan, amountFact, amountDiffToday, amountOver)); });
		 * Collections.sort(planFactList);
		 * 
		 * Double monthBeginBalance = 26025.0; Double totalBudgetPlan =
		 * planFactList.stream().mapToDouble(PlanFact::getAmountPlan).sum();
		 * Double totalBudgetOver =
		 * planFactList.stream().mapToDouble(PlanFact::getAmountOver).sum();
		 * Double totalDiffToday =
		 * planFactList.stream().mapToDouble(PlanFact::getAmountTodayDiff).sum()
		 * ; Double totalMonthDynamic = totalBudgetPlan + totalBudgetOver;
		 * Double monthEndBalance = monthBeginBalance + totalMonthDynamic;
		 * 
		 * budgetSummary.add(new BudgetSummary(quater[0], monthBeginBalance,
		 * totalBudgetPlan, totalBudgetOver, totalDiffToday, totalMonthDynamic,
		 * monthEndBalance, planFactList));
		 * 
		 * // NEXT MONTH
		 * 
		 * List<PlanFact> planFactListNext = new ArrayList<PlanFact>(); //
		 * collecting table of plan-fact by category for this month
		 * dataHandler.getCategories().stream().forEach(category ->
		 * 
		 * { BudgetPlan budgetPlan = budgetPlans.stream().filter(b ->
		 * b.getCategory() == category).findFirst() .orElse(null); if
		 * (budgetPlan != null) { Double amountPrevFact =
		 * planFactList.stream().filter(pf -> pf.getCategory().equals(category))
		 * .findFirst().get().getAmountFact(); Double amountPrevOver =
		 * planFactList.stream().filter(pf -> pf.getCategory().equals(category))
		 * .findFirst().get().getAmountOver(); Double amountPlan; if
		 * (amountPrevOver < 0) amountPlan = (budgetPlan.getAmount() -
		 * amountPrevFact) / (budgetPlan.getLength() - currentMonthInt); else
		 * amountPlan = budgetPlan.getAmount() / budgetPlan.getLength();
		 * planFactListNext.add(new PlanFact(category, today.withDayOfMonth(1),
		 * amountPlan, 0.0, 0.0, 0.0)); } });
		 * Collections.sort(planFactListNext);
		 * 
		 * monthBeginBalance = monthEndBalance; totalBudgetPlan =
		 * planFactListNext.stream().mapToDouble(PlanFact::getAmountPlan).sum();
		 * monthEndBalance = monthEndBalance + totalBudgetPlan;
		 * 
		 * budgetSummary.add(new BudgetSummary(quater[1], monthBeginBalance,
		 * totalBudgetPlan, 0.0, 0.0, totalBudgetPlan, monthEndBalance,
		 * planFactListNext));
		 * 
		 * // MONTH AFTER NEXT monthBeginBalance = monthEndBalance;
		 * monthEndBalance = monthEndBalance + totalBudgetPlan;
		 * 
		 * budgetSummary.add(new BudgetSummary(quater[2], monthBeginBalance,
		 * totalBudgetPlan, 0.0, 0.0, totalBudgetPlan, monthEndBalance,
		 * planFactListNext));
		 */
	}

	private String getBudgetContent() {

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
				+ Util.amountToStringWithSign(budgetSummary.get(1).getTotalMonthDynamic())
				+ "</font></b></td><td align='center'><b><font size='4'>"
				+ Util.amountToStringWithSign(budgetSummary.get(2).getTotalMonthDynamic()) + "</font></b></td></tr>"
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

	private String getTransactionsContent() {

		String content = "<P><b>Totals & transactions: </b>";
		content = content
				+ "<tr><table border='1' cellpadding='1' cellspacing='1' style='width:550px;'><thead><tr><th>Date</th><th>Account</th><th>Amount</th><th>Diff</th></tr></thead>";
		content = content + "<tfoot><tr><td></td><td><b>TOTAL</b></td><td><b>"
				+ Util.amountToString(DataHandler.getFullTotal(totals)) + "</b></td><td><b>"
				+ Util.amountToString(DataHandler.getFullDiff(totals)) + "</b></td></tr></tfoot><tbody>";
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
					+ Util.amountToString(total.getDifference()) + "</td></tr>";
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
		if (Util.isDateToday(total.getDate()))
			return "#BCE954";
		// else if (status == DataRetrievalStatus.FAILED)
		// return "#FF7F50";
		else // if (status == DataRetrievalStatus.SKIPPED)
			return "#FFE87C";
		// else
		// return "white";
	}

}
