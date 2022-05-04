package ecommerce.order.event.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ })
public class InventoryEventPayloadData {
	
	private String productId;
	
	private long quantity;
	
	private For for1;
	
	public InventoryEventPayloadData() {
	}

	public InventoryEventPayloadData(String productId, long quantity, For for1) {
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
