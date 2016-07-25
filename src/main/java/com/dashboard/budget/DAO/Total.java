package com.dashboard.budget.DAO;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.dashboard.budget.DataRetrievalStatus;

@Entity
@Table(name = "totals")
public class Total implements Comparable<Object> {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@Type(type = "date")
	private Date date;
	@ManyToOne
	@JoinColumn(name = "account_id")
	private Account account;
	private Double amount;
	private Double difference;
	private DataRetrievalStatus status;

	public Total() {
	}

	public Total(Date date, Account account, Double amount, Double difference) {
		this.date = date;
		this.account = account;
		this.amount = amount;
		this.difference = difference;
	}

	public Total(Account account, Double amount, Double difference, DataRetrievalStatus status) {
		this.date = new Date();
		this.account = account;
		this.amount = amount;
		this.difference = difference;
		this.status = status;
	}

	public Total(Account account, Double amount, Double difference) {
		this.date = new Date();
		this.account = account;
		this.amount = amount;
		this.difference = difference;
	}

	public int getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Double getDifference() {
		return difference;
	}

	public void setDifference(Double difference) {
		this.difference = difference;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public DataRetrievalStatus getStatus() {
		return status;
	}

	public void setStatus(DataRetrievalStatus status) {
		this.status = status;
	}

	@Override
	public int compareTo(Object o) {
		int compareQuantity = Integer.valueOf(((Total) o).getAccount().getId());

		// ascending order
		return Integer.valueOf(this.getAccount().getId()) - compareQuantity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
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
		Total other = (Total) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (amount == null) {
			if (other.amount != null)
				return false;
		} else if (!amount.equals(other.amount))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy");
			if (!sdf.format(date).equals(sdf.format(other.date)))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Total [id=" + id + ", date=" + date + ", account=" + account + ", amount=" + amount + ", difference="
				+ difference + ", status=" + status + "]";
	}

}
