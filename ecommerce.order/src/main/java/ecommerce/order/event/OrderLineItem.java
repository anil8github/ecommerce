package ecommerce.order.event;

public class OrderLineItem {
	
	private String productId;
	
	private long quantity;
	
	public OrderLineItem() {
	}
	
	public OrderLineItem(String productId, long quantity) {
		this.productId = productId;
		this.quantity = quantity;
	}

	public String getProductId() {
		return productId;
	}

	public long getQuantity() {
		return quantity;
	}
	
}
