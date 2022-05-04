package ecommerce.gateway.user.dto;

public class RateLimit {
	
	private String path;
	
	private long limit;
	
	private long per;

	public RateLimit(String path, long limit, long per) {
		this.path = path;
		this.limit = limit;
		this.per = per;
	}

	public String getPath() {
		return path;
	}

	public long getLimit() {
		return limit;
	}

	public long getPer() {
		return per;
	}
	
}
