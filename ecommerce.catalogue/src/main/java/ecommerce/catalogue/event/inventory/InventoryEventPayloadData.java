package ecommerce.catalogue.event.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "for1", "productId" })
public class InventoryEventPayloadData {
	
	private long quantity;
	
	public InventoryEventPayloadData() {
	}

	public InventoryEventPayloadData(long quantity) {
		this.quantity = quantity;
	}

	public long getQuantity() {
		return quantity;
	}

}
