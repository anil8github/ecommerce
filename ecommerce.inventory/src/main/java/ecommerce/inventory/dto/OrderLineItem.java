package ecommerce.inventory.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "order_item")
public class OrderLineItem {
	
	@EmbeddedId
	private LineItemId lineItemId;
	
	private long quantity;

	public OrderLineItem(String orderId, String productId, long quantity) {
		this.lineItemId = new LineItemId(orderId, productId);
		this.quantity = quantity;
	}
	
	public OrderLineItem() {
	}

	public String getProductId() {
		return lineItemId.productId;
	}

	public long getQuantity() {
		return quantity;
	}

	@Embeddable
	public static class LineItemId implements Serializable {
		
		@Column(name  = "order_id")
		private String orderId;
		
		private String productId;

		public LineItemId(String orderId, String productId) {
			this.orderId = orderId;
			this.productId = productId;
		}
		
		public LineItemId() {
		}
		
		
	}
	
}
