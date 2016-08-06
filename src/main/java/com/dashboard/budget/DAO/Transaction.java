package com.dashboard.budget.DAO;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "transactions")
public class Transaction implements Comparable<Object> {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@Type(type = "date")
	private Date date;
	@ManyToOne
	@JoinColumn(name = "account_id")
	private Account account;
	@ManyToOne
	@JoinColumn(name = "total_id")
	private Total total;
	private String decription;
	private double amount;
	@OneToOne
	@JoinColumn(name = "category_id", unique = true)
	private TransactionCategory category;
	private String categoryStr;

	public Transaction() {
	}

	public Transaction(Account account, Total total, Date date, String decription, double amount,
			String categoryStr, TransactionCategory category) {
		this.account = account;
		this.total = total;
		this.date = date;
		this.decription = decription;
		this.amount = amount;
		this.categoryStr = categoryStr;
		this.category = category;
	}

	public int getId() {
		return id;
	}

	public Account getAccount() {
		return account;
	}

	public Total getTotal() {
		return total;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDecription() {
		return decription;
	}

	public void setDecription(String decription) {
		this.decription = decription;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public TransactionCategory getCategory() {
		return category;
	}

	public void setCategory(TransactionCategory category) {
		this.category = category;
	}
	
	public String getCategoryStr() {
		return categoryStr;
	}

	public void setCategoryStr(String categoryStr) {
		this.categoryStr = categoryStr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		long temp;
		temp = Double.doubleToLongBits(amount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((decription == null) ? 0 : decription.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (Double.doubleToLongBits(amount) != Double.doubleToLongBits(other.amount))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (decription == null) {
			if (other.decription != null)
				return false;
		} else if (!decription.equals(other.decription))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Transaction [id=" + id + ", date=" + date + ", account=" + account + ", total=" + total
				+ ", decription=" + decription + ", amount=" + amount + ", category=" + category + ", categoryStr="
				+ categoryStr + "]";
	}

	@Override
	public int compareTo(Object o) {
		return date.compareTo(((Transaction) o).getDate());
	}

}
