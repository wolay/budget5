package com.dashboard.budget.DAO;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "budget_plan")
public class BudgetPlan {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private boolean isActive;
	@ManyToOne
	@JoinColumn(name = "category_id")	
	private Category category;
	@Type(type = "date")
	private Date startDate;
	@Type(type = "date")
	private Date endDate;
	private int length;
	private Double amount;
	
	public BudgetPlan(){}
	public BudgetPlan(Category category, Date startDate, Date endDate, int length, Double amount) {
		super();
		this.category = category;
		this.startDate = startDate;
		this.endDate = endDate;
		this.length = length;
		this.amount = amount;
		this.isActive = true;
	}

	public boolean isActive() {
		return isActive;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getId() {
		return id;
	}
	
	public Double getAmount() {
		return amount;
	}
	
	@Override
	public String toString() {
		return "BudgetPlan [id=" + id + ", isActive=" + isActive + ", category=" + category + ", startDate=" + startDate
				+ ", endDate=" + endDate + ", length=" + length + ", amount=" + amount + "]";
	}	
	
}
