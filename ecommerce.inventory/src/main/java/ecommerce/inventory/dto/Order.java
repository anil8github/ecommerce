package ecommerce.inventory.dto;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
	
	@Id
	private String orderId;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "order_id")
	private List<OrderLineItem> lineItems = new ArrayList<>();
	
	public Order(String orderId, List<OrderLineItem> lineItems) {
		this.orderId = orderId;
		this.lineItems = lineItems;
	}
	
	public Order() {
	}

	public List<OrderLineItem> getLineItems() {
		return lineItems;
	}
	
	public void addLineItem(OrderLineItem lineItem) {
		this.lineItems.add(lineItem);
	}
	
}
