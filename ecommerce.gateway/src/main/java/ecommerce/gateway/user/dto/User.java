package ecommerce.gateway.user.dto;

import java.util.List;

public class User {
	
	private String userId;
	
	private List<RateLimit> rateLimits;
	
	public User() {
	}

	public String getUserId() {
		return userId;
	}

	public List<RateLimit> getRateLimits() {
		return rateLimits;
	}
	
}

