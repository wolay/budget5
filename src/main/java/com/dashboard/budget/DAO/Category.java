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
public class Category implements Comparable<Object>{
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String name;
	private int displayOrder;
	private int type; // 1 - debit, 2 - credit, 3 - others
	@OneToMany(mappedBy="category")
	private Set<Transaction> transactions;
	@OneToMany(mappedBy="category")
	private Set<BudgetPlan> budgetPlan;	
	@OneToMany(mappedBy="targetCategory")
	private Set<CategorizationRule> categorizationRules;	
	
	public Category(){}
	public Category(String name, int displayOrder, int type) {
		this.name = name;
		this.displayOrder = displayOrder;
		this.type = type;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getDisplayOrder() {
		return displayOrder;
	}
	
	public int getType(){
		return type;
	}
	
	public Set<Transaction> getTransaction(){
		return transactions;
	}
	
	public Set<BudgetPlan> getBudgetPlan() {
		return budgetPlan;
	}
	
	@Override
	public int compareTo(Object o) {
		return this.displayOrder - ((Category) o).displayOrder;
	}
	
	@Override
	public String toString() {
		return "Category [id=" + id + ", name=" + name + ", type=" + type + "]";
	}

}
