package ecommerce.user.dto;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "user")
public class User {
	
	@Field(name = "userId")
	private String userId;
	
	private String userName;
	
	private String password;
	
	private List<String> roles;
	
	private List<RateLimit> rateLimits;

	public User(String userId, String userName, String password) {
		this.userId = userId;
		this.userName = userName;
		this.password = password;
	}
	
	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public List<String> getRoles() {
		return roles;
	}

	public List<RateLimit> getRateLimits() {
		return rateLimits;
	}

	public String getPassword() {
		return password;
	}

}
