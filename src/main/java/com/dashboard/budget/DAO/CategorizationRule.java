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
	private int priority;
	private String originalCategory;
	@ManyToOne
	@JoinColumn(name = "account_id")
	private Account account;
	private String context;
	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category targetCategory;	
	@OneToMany(mappedBy="categorizationRule")
	private Set<Transaction> transactions;
	
	public CategorizationRule(){}
	public CategorizationRule(int priority, String originalCategory, Account account, String context, Category targetCategory,
			Set<Transaction> transactions) {
		this.priority = priority;
		this.originalCategory = originalCategory;
		this.account = account;
		this.context = context;
		this.targetCategory = targetCategory;
		this.transactions = transactions;
	}
	
	public int getId() {
		return id;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public String getOriginalCategory() {
		return originalCategory;
	}
	
	public Account getAccount() {
		return account;
	}
	
	public String getContext() {
		return context;
	}
	
	public Category getTargetCategory() {
		return targetCategory;
	}
	
	public Set<Transaction> getTransactions() {
		return transactions;
	}
	
	@Override
	public String toString() {
		return "CategorizationRule [id=" + id + ", priority=" + priority + ", originalCategory=" + originalCategory
				+ ", account=" + account + ", context=" + context + ", targetCategory=" + targetCategory
				+ ", transactions=" + transactions + "]";
	}
}
