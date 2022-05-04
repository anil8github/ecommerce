package ecommerce.inventory.event;

public class For {
	
	private String aggregateType;
	
	private String aggregateId;
	
	public For() {
	}

	public For(String aggregateType, String aggregateId) {
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public String getAggregateId() {
		return aggregateId;
	}

}
