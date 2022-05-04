package ecommerce.inventory.event.order;

public class OrderLineItem {
	
	private String productId;
	
	private long quantity;

	public String getProductId() {
		return productId;
	}

	public long getQuantity() {
		return quantity;
	}
	
}
