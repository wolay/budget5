package com.dashboard.budget.DAO;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "categorization_rules")
public class CategorizationRule {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String categoryString;
	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;	
	@OneToMany(mappedBy="categorizationRule")
	private Set<Transaction> transactions;
	
	public CategorizationRule(){}
	public CategorizationRule(String categoryString, Category category) {
		this.categoryString = categoryString;
		this.category = category;
	}
	public int getId() {
		return id;
	}
	
	public String getCategoryString() {
		return categoryString;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public void setCategory(Category category) {
		this.category = category;
	}
	
	public Set<Transaction> getTransactions() {
		return transactions;
	}
	
	@Override
	public String toString() {
		return "CategorizationRule [id=" + id + ", categoryString=" + categoryString + ", category=" + category + "]";
	}	

}
