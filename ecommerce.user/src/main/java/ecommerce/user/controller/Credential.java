package ecommerce.user.controller;

public class Credential {
	
	private String userName;
	
	private String password;
	
	public Credential() {
	}

	public Credential(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

}
