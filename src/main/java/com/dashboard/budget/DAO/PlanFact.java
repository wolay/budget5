package com.dashboard.budget.DAO;

import java.time.LocalDate;

public class PlanFact implements Comparable<Object> {

	private Category category;
	private LocalDate startDate;
	private Double amountPlan;
	private Double amountFact;
	private Double amountTodayDiff;
	private Double amountOver;

	public PlanFact(Category category, LocalDate localDate, Double amountPlan, Double amountFact,
			Double amountTodayDiff, Double amountOver) {
		this.category = category;
		this.startDate = localDate;
		this.amountPlan = amountPlan;
		this.amountFact = amountFact;
		this.amountTodayDiff = amountTodayDiff;
		this.amountOver = amountOver;
	}

	public Category getCategory() {
		return category;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public Double getAmountPlan() {
		return amountPlan;
	}

	public Double getAmountFact() {
		return amountFact;
	}

	public Double getAmountTodayDiff() {
		return amountTodayDiff;
	}

	public Double getAmountOver() {
		return amountOver;
	}

	@Override
	public String toString() {
		return "PlanFact [category=" + category + ", startDate=" + startDate + ", amountPlan=" + amountPlan
				+ ", amountFact=" + amountFact + ", amountTodayDiff=" + amountTodayDiff + ", amountOver=" + amountOver
				+ "]";
	}

	@Override
	public int compareTo(Object o) {
		return this.getCategory().getDisplayOrder() - ((PlanFact) o).getCategory().getDisplayOrder();
	}

}
