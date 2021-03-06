package com.dashboard.budget.DAO;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {

	@Id
	private int id;
	private String name;
	private boolean isMyPortfolio;
	private String myPortfolioId;
	private String url;
	private String owner;
	private boolean isEnabled;
	private boolean isDebit;
	@OneToOne(cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "account_details_login_id", unique = true)
	private AccountDetailsLogin accountDetailsLogin;
	@OneToOne(cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "account_details_navigation_id", unique = true)
	private AccountDetailsNavigation accountDetailsNavigation;
	@OneToOne(cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "account_details_total_id", unique = true)
	private AccountDetailsTotal accountDetailsTotal;
	@OneToOne(cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "account_details_transaction_id", unique = true)
	private AccountDetailsTransaction accountDetailsTransaction;
	@OneToMany(mappedBy = "account")
	private Set<Total> totals;
	@OneToMany(mappedBy = "account")
	private Set<Transaction> transactions;
	@ManyToOne
	@JoinColumn(name = "bank_id")
	private Bank bank;
	@OneToMany(mappedBy = "account")
	private Set<SecretQuestion> secretQuestions;
	@OneToMany(mappedBy = "account")
	private Set<CategorizationRule> categorizationRule;

	public Account() {
	}

	public Account(int id, String name, Bank bank, String url, String owner, boolean isMyPortfolio) {
		this.id = id;
		this.name = name;
		this.bank = bank;
		this.url = url;
		this.owner = owner;
		this.isMyPortfolio = isMyPortfolio;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Bank getBank() {
		return bank;
	}

	public boolean getIsMyProtfolio() {
		return isMyPortfolio;
	}

	public String getMyPortfolioId() {
		return myPortfolioId;
	}

	public String getOwner() {
		return owner;
	}

	public String getUrl() {
		return url;
	}

	public boolean getIsEnabled() {
		return isEnabled;
	}
	
	public boolean getIsDebit() {
		return isDebit;
	}

	public AccountDetailsLogin getAccountDetailsLogin() {
		return accountDetailsLogin;
	}

	public void setAccountDetailsLogin(AccountDetailsLogin accountDetailsLogin) {
		this.accountDetailsLogin = accountDetailsLogin;
	}

	public AccountDetailsNavigation getAccountDetailsNavigation() {
		return accountDetailsNavigation;
	}

	public void setAccountDetailsNavigation(AccountDetailsNavigation accountDetailsNavigation) {
		this.accountDetailsNavigation = accountDetailsNavigation;
	}

	public AccountDetailsTotal getAccountDetailsTotal() {
		return accountDetailsTotal;
	}

	public void setAccountDetailsTotal(AccountDetailsTotal accountDetailsTotal) {
		this.accountDetailsTotal = accountDetailsTotal;
	}

	public AccountDetailsTransaction getAccountDetailsTransaction() {
		return accountDetailsTransaction;
	}

	public void setAccountDetailsTransaction(AccountDetailsTransaction accountDetailsTransaction) {
		this.accountDetailsTransaction = accountDetailsTransaction;
	}

	public Set<Total> getTotals() {
		return totals;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		Account other = (Account) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Account [id=" + id + ", name=" + name + ", bank=" + bank + ", isMyPortfolio=" + isMyPortfolio
				+ ", myPortfolioId=" + myPortfolioId + ", url=" + url + ", owner=" + owner + ", isEnabled=" + isEnabled
				+ "]";
	}

}
