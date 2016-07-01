package com.dashboard.budget;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.dashboard.budget.DAO.Account;
import com.dashboard.budget.DAO.Total;

public class UtilDb {

	public static void saveTotalsToDb(List<Total> totals) {
		totals.stream().forEach(t -> {
			Session session = HibernateUtil.getSessionFactory().openSession();
			Transaction txn = session.getTransaction();
			try {
				txn.begin();
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

	public static void verifyAccountsInDb(List<Account> accountsList) {
		accountsList.stream().forEach(a -> {
			Session session = HibernateUtil.getSessionFactory().openSession();
			Transaction txn = session.getTransaction();
			try {
				txn.begin();
				if ((Account) session.get(Account.class, a.getId()) == null)
					session.save(a);
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
}
