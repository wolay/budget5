package com.dashboard.budget.DAO;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String name;
	@OneToMany(mappedBy="category")
	private Set<Transaction> transactions;
	@OneToOne(mappedBy="category")
	private BudgetPlan budgetPlan;	
	
	public Category(){}
	public Category(String name) {
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public Set<Transaction> getTransaction(){
		return transactions;
	}
	
	@Override
	public String toString() {
		return "TransactionCategory [id=" + id + ", name=" + name + "]";
	} 

}
