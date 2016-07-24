package com.dashboard.budget;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.dashboard.budget.DAO.Account;

public class UtilDb {

	public static void createDbEntitiesAccount(List<Account> entityList) {
		entityList.stream().forEach(e -> {
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("budget");
			EntityManager em = emf.createEntityManager();
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
			} finally {
				if (em != null) {
					em.close();
				}
			}
		});
	}
	
	public static List<Account> loadAccounts() {
		
		List<Account> accounts = null;
		
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("budget");
			EntityManager em = emf.createEntityManager();
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
			} finally {
				if (em != null) {
					em.close();
				}
			}
			
			return accounts; 
	}

	/*
	public static void saveTotalsToDb(List<Total> totals) {
		totals.stream().filter(t -> t.getAmount() != null).forEach(t -> {
			Session session = HibernateUtil.getSessionFactory().openSession();
			Transaction txn = session.getTransaction();
			try {
				txn.begin();
				// if ((Account) session.get(Account.class, t.getId()) == null)
				session.save(t);
				txn.commit();
			} catch (Exception e) {
				if (txn != null) {
					txn.rollback();
				}
				e.printStackTrace();
			} finally {
				if (session != null) {
					session.close();
				}
			}
		});
	}

	public static void createDbEntitiesCreditScore(List<CreditScore> entityList) {
		entityList.stream().forEach(e -> {
			Session session = HibernateUtil.getSessionFactory().openSession();
			Transaction txn = session.getTransaction();
			try {
				txn.begin();
				if ((Account) session.get(Account.class, e.getId()) == null)
					session.save(e);
				txn.commit();
			} catch (Exception ex) {
				if (txn != null) {
					txn.rollback();
				}
				ex.printStackTrace();
			} finally {
				if (session != null) {
					session.close();
				}
			}
		});
	}
	*/
}