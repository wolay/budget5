package com.dashboard.budget.DAO;

import java.util.List;

public class BudgetSummary {

	private String monthName;
	private Double amountBegin;
	private Double totalBudgetPlan;
	private Double totalBudgetOver;
	private Double totalDiffToday;
	private Double totalMonthDynamic;
	private Double amountEnd;
	private List<PlanFact> planFactList;

	public BudgetSummary(String monthName, Double amountBegin, Double totalBudgetPlan, Double totalBudgetOver,
			Double totalDiffToday, Double totalMonthDynamic, Double amountEnd, List<PlanFact> planFactList) {
		this.monthName = monthName;
		this.amountBegin = amountBegin;
		this.totalBudgetPlan = totalBudgetPlan;
		this.totalBudgetOver = totalBudgetOver;
		this.totalDiffToday = totalDiffToday;
		this.totalMonthDynamic = totalMonthDynamic;
		this.amountEnd = amountEnd;
		this.planFactList = planFactList;
	}

	public String getMonthName() {
		return monthName;
	}

	public void setMonthName(String monthName) {
		this.monthName = monthName;
	}

	public Double getAmountBegin() {
		return amountBegin;
	}

	public void setAmountBegin(Double amountBegin) {
		this.amountBegin = amountBegin;
	}

	public Double getTotalBudgetPlan() {
		return totalBudgetPlan;
	}

	public void setTotalBudgetPlan(Double totalBudgetPlan) {
		this.totalBudgetPlan = totalBudgetPlan;
	}

	public Double getTotalBudgetOver() {
		return totalBudgetOver;
	}

	public void setTotalBudgetOver(Double totalBudgetOver) {
		this.totalBudgetOver = totalBudgetOver;
	}

	public Double getTotalDiffToday() {
		return totalDiffToday;
	}

	public void setTotalDiffToday(Double totalDiffToday) {
		this.totalDiffToday = totalDiffToday;
	}

	public Double getTotalMonthDynamic() {
		return totalMonthDynamic;
	}

	public void setTotalMonthDynamic(Double totalMonthDynamic) {
		this.totalMonthDynamic = totalMonthDynamic;
	}

	public Double getAmountEnd() {
		return amountEnd;
	}

	public void setAmountEnd(Double amountEnd) {
		this.amountEnd = amountEnd;
	}

	public List<PlanFact> getPlanFactList() {
		return planFactList;
	}

	public void setPlanFactList(List<PlanFact> planFactList) {
		this.planFactList = planFactList;
	}

	@Override
	public String toString() {
		return "PlanFactWithBalances [monthName=" + monthName + ", amountBegin=" + amountBegin + ", totalBudgetPlan="
				+ totalBudgetPlan + ", totalBudgetOver=" + totalBudgetOver + ", totalDiffToday=" + totalDiffToday
				+ ", totalMonthDynamic=" + totalMonthDynamic + ", amountEnd=" + amountEnd + "]";
	}

}
