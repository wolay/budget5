package com.dashboard.budget.DAO;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String name;
	private boolean isDebit;
	@OneToMany(mappedBy="category")
	private Set<Transaction> transactions;
	@OneToMany(mappedBy="category")
	private Set<BudgetPlan> budgetPlan;	
	@OneToMany(mappedBy="targetCategory")
	private Set<CategorizationRule> categorizationRules;	
	
	public Category(){}
	public Category(String name, boolean isDebit) {
		this.name = name;
		this.isDebit = isDebit;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isDebit(){
		return isDebit;
	}
	
	public Set<Transaction> getTransaction(){
		return transactions;
	}
	
	public Set<BudgetPlan> getBudgetPlan() {
		return budgetPlan;
	}
	@Override
	public String toString() {
		return "TransactionCategory [id=" + id + ", name=" + name + "]";
	} 

}
