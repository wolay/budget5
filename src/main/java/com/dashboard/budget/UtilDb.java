package com.dashboard.budget;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.CategorizationRule;
import com.dashboard.budget.DAO.Category;
import com.dashboard.budget.DAO.Credential;
import com.dashboard.budget.DAO.CreditScore;
import com.dashboard.budget.DAO.Total;
import com.dashboard.budget.DAO.Transaction;


public class UtilDb implements Config{
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

	@SuppressWarnings("unchecked")
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
	public List<Total> loadTotalsFromDb() {

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

	@SuppressWarnings("unchecked")
	public List<Transaction> loadTransactionsFromDb() {

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
	

	@SuppressWarnings("unchecked")
	public List<CreditScore> loadCreditScoresFromDb() {

		List<CreditScore> creditScores = null;

		EntityTransaction txn = em.getTransaction();
		try {
			txn.begin();
			String sql = "select id, credit_scores.date, difference,name,score,credit_scores.account_id from mydb.credit_scores "
					+ "right join (SELECT max(date) as date, account_id FROM mydb.credit_scores group by account_id) max_dates "
					+ "on credit_scores.date=max_dates.date and credit_scores.account_id=max_dates.account_id";
			Query query = em.createNativeQuery(sql, CreditScore.class);
			creditScores = (List<CreditScore>) query.getResultList();
			txn.commit();
		} catch (Exception ex) {
			if (txn != null) {
				txn.rollback();
			}
			ex.printStackTrace();
		}

		return creditScores;
	}
	

	@SuppressWarnings("unchecked")
	public List<Category> loadCategoriesFromDb() {

		List<Category> categories = null;

		EntityTransaction txn = em.getTransaction();
		try {
			txn.begin();
			Query query = em.createQuery("select category from Category category");
			categories = query.getResultList();
			txn.commit();
		} catch (Exception ex) {
			if (txn != null) {
				txn.rollback();
			}
			ex.printStackTrace();
		}

		return categories;
	}
	

	@SuppressWarnings("unchecked")
	public List<CategorizationRule> loadCategorizationRulesFromDb() {
		List<CategorizationRule> categorizationRules = null;

		EntityTransaction txn = em.getTransaction();
		try {
			txn.begin();
			Query query = em.createQuery("select categorizationRule from CategorizationRule categorizationRule");
			categorizationRules = query.getResultList();
			txn.commit();
		} catch (Exception ex) {
			if (txn != null) {
				txn.rollback();
			}
			ex.printStackTrace();
		}

		return categorizationRules;
	}
	
	@SuppressWarnings("unchecked")
	public List<Credential> loadCredentialsFromDb() {

		List<Credential> credentials = null;

		EntityTransaction txn = em.getTransaction();
		try {
			txn.begin();
			Query query = em.createQuery("select credential from Credential credential");
			credentials = query.getResultList();
			txn.commit();
		} catch (Exception ex) {
			if (txn != null) {
				txn.rollback();
			}
			ex.printStackTrace();
		}

		return credentials;
	}

	
	public void saveToDb(Object object) {
		/*if(!bankAccountsFilter.isEmpty()){
			logger.info("Debug mode -> save to db not happened");
			return;
		}*/
		EntityTransaction txn = em.getTransaction();
		try {
			txn.begin();
			em.persist(object);
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