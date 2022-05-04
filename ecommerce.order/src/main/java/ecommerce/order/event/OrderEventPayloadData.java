package ecommerce.order.event;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({})
public class OrderEventPayloadData {
	
	private String userId;
	
	private double total;
	
	private String paymentInfo;
	
	private String deliveryInfo;
	
	private List<OrderLineItem> orderLineItems;
	
	public OrderEventPayloadData(String userId, double total, String paymentInfo, String deliveryInfo,
			List<OrderLineItem> orderLineItems) {
		this.userId = userId;
		this.total = total;
		this.paymentInfo = paymentInfo;
		this.deliveryInfo = deliveryInfo;
		this.orderLineItems = orderLineItems;
	}
	
	public OrderEventPayloadData() {
	}

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
