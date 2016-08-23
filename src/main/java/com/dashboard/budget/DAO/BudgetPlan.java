package com.dashboard.budget.DAO;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "budget_plan")
public class BudgetPlan {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private boolean isActive;
	@OneToOne
	@JoinColumn(name = "category_id", unique=true)	
	private Category category;
	@Type(type = "date")
	private Date startDate;
	@Type(type = "date")
	private Date endDate;
	
	public BudgetPlan(){}
	public BudgetPlan(Category category, Date startDate, Date endDate) {
		super();
		this.category = category;
		this.startDate = startDate;
		this.endDate = endDate;
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
	
	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return "BudgetPlan [id=" + id + ", isActive=" + isActive + ", category=" + category + ", startDate=" + startDate
				+ ", endDate=" + endDate + "]";
	}
	
}
