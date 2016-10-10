package com.dashboard.budget.DAO;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "credentials")
public class Credential {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String name;
	private String login;
	private String password;
	
	public Credential(){}
	public Credential(String name, String login, String password) {
		this.name = name;
		this.login = login;
		this.password = password;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getLogin() {
		return login;
	}
	
	public String getPassword() {
		return password;
	}
	
	@Override
	public String toString() {
		return "Credential [id=" + id + ", name=" + name + ", login=" + login + ", password=" + password + "]";
	}
	
}
