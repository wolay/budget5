package com.dashboard.budget.DAO;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "transaction_categories")
public class TransactionCategory {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String name;
	@OneToOne(mappedBy="category")
	private Transaction transaction;
	@OneToOne(mappedBy="category")
	private BudgetPlan budgetPlan;	
	
	public TransactionCategory(){}
	public TransactionCategory(String name) {
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return "TransactionCategory [id=" + id + ", name=" + name + "]";
	} 

}