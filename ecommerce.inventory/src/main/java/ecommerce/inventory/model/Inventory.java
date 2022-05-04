package ecommerce.inventory.model;

public class Inventory {
	
	private String productId;
	
	private Long quantity;

	public Inventory(String productId, long quantity) {
		this.productId = productId;
		this.quantity = quantity;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public String getProductId() {
		return productId;
	}
	
}
