package com.passportsingle;

public class Customer {

	public String name; 
	public String email;
	public int sales = 0;
	public int returns = 0;
	public float total = 0.0f;
	public int id;
	
	public void setName(String name) {
		this.name = name;
	}

	public void setEmail(String email) {
		this.email = email; 
	}

	public void setId(int at) {
		this.id = at;
	}
}
