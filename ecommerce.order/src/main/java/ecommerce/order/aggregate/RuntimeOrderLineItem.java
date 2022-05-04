package ecommerce.order.aggregate;

import ecommerce.order.event.OrderLineItem;

public class RuntimeOrderLineItem extends OrderLineItem {
	
	private Boolean isConfirmed;
	
	public RuntimeOrderLineItem(String productId, long quantity) {
		super(productId, quantity);
	}


	public Boolean isConfirmed() {
		return isConfirmed;
	}

	public void setConfirmed(Boolean isConfirmed) {
		this.isConfirmed = isConfirmed;
	}

}
