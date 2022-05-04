package ecommerce.inventory.event;

public class InventoryEventData {
	
	private String productId;
	
	private long quantity;
	
	private For for1;
	
	public InventoryEventData() {
	}

	public InventoryEventData(String productId, long quantity, For for1) {
		this.productId = productId;
		this.quantity = quantity;
		this.for1 = for1;
	}

	public String getProductId() {
		return productId;
	}

	public long getQuantity() {
		return quantity;
	}

	public For getFor1() {
		return for1;
	}
	
}
