package com.dashboard.budget;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;



public class UtilDb {
	private EntityManager em;

	public void dropDb() {

		// JDBC driver name and database URL
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		String DB_URL = "jdbc:mysql://localhost:3306/mydb";

		// Database credentials
		String USER = "root";
		String PASS = "root";

		Connection conn = null;
		Statement stmt = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// STEP 3: Open a connection
			System.out.println("Connecting to a selected database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
			System.out.println("Deleting database...");
			stmt = conn.createStatement();

			String sql = "DROP DATABASE mydb";
			stmt.executeUpdate(sql);
			System.out.println("Database deleted successfully...");
			
			sql = "CREATE DATABASE mydb";
			stmt.executeUpdate(sql);
			System.out.println("Database created successfully...");
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			} // do nothing
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		}
	}

	public void initializeSchema(){
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("budget");
		em = emf.createEntityManager();	
	}
	
	public void createDbEntitiesAccount(List<Account> entityList) {
		entityList.stream().forEach(e -> {
			EntityTransaction txn = em.getTransaction();
			try {
				txn.begin();
				if (em.find(Account.class, e.getId()) == null)
					em.persist(e);
				txn.commit();
			} catch (Exception ex) {
				if (txn != null) {
					txn.rollback();
				}
				ex.printStackTrace();
			}
		});
	}

	public List<Account> loadAccounts() {

		List<Account> accounts = null;

		EntityTransaction txn = em.getTransaction();
		try {
			txn.begin();
			Query query = em.createQuery("select account from Account as account");
			accounts = query.getResultList();
			txn.commit();
		} catch (Exception ex) {
			if (txn != null) {
				txn.rollback();
			}
			ex.printStackTrace();
		}

		return accounts;
	}

	@SuppressWarnings("unchecked")
	public List<Total> loadPrevTotals() {

		List<Total> totals = null;
		

		EntityTransaction txn = em.getTransaction();
		try {
			txn.begin();
			String sql = "select id, amount, date, difference, status, all_totals.account_id as account_id from mydb.totals all_totals "
					+ "right join (SELECT max(date) as max_date, account_id FROM mydb.totals group by account_id) last_totals "
					+ "on all_totals.account_id=last_totals.account_id and all_totals.date=last_totals.max_date";
			Query query = em.createNativeQuery(sql, Total.class);
			totals = (List<Total>) query.getResultList();
			txn.commit();
		} catch (Exception ex) {
			if (txn != null) {
				txn.rollback();
			}
			ex.printStackTrace();
		}

		return totals;
	}

	public List<Transaction> loadPrevTransactions() {

		List<Transaction> transactions = null;

		EntityTransaction txn = em.getTransaction();
		try {
			txn.begin();
			Query query = em.createQuery("select transaction from Transaction transaction");
			transactions = query.getResultList();
			txn.commit();
		} catch (Exception ex) {
			if (txn != null) {
				txn.rollback();
			}
			ex.printStackTrace();
		}

		return transactions;
	}

	public void saveTotalsToDb(List<Total> totals) {
		totals.stream().filter(t -> t.getAmount() != null).forEach(t -> {
			EntityTransaction txn = em.getTransaction();
			try {
				txn.begin();
				em.persist(t);
				txn.commit();
			} catch (Exception e) {
				if (txn != null) {
					txn.rollback();
				}
				e.printStackTrace();
			}
		});
	}

	public void saveTotalToDb(Total total) {
		EntityTransaction txn = em.getTransaction();
		try {
			txn.begin();
			em.persist(total);
			txn.commit();
		} catch (Exception e) {
			if (txn != null) {
				txn.rollback();
			}
			e.printStackTrace();
		}
	}

	public void saveTransactionToDb(Transaction transaction) {
		EntityTransaction txn = em.getTransaction();
		try {
			txn.begin();
			em.persist(transaction);
			txn.commit();
		} catch (Exception e) {
			if (txn != null) {
				txn.rollback();
			}
			e.printStackTrace();
		}
	}

	public void close() {
		if (em != null) {
			em.close();
		}
	}
}