package ecommerce.inventory.event.order;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "userId", "total", "paymentInfo", "deliveryInfo" })
public class OrderEventPayloadData {
	
	private String userId;
	
	private double total;
	
	private String paymentInfo;
	
	private String deliveryInfo;
	
	private List<OrderLineItem> orderLineItems;

	public String getUserId() {
		return userId;
	}
	
	public double getTotal() {
		return total;
	}

	public String getPaymentInfo() {
		return paymentInfo;
	}

	public String getDeliveryInfo() {
		return deliveryInfo;
	}

	public List<OrderLineItem> getOrderLineItems() {
		return orderLineItems;
	}
	
	

}
