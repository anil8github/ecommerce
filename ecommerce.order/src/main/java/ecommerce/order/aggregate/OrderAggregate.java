package ecommerce.order.aggregate;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.order.dto.EventStore;
import ecommerce.order.event.OrderEventPayloadData;

public class OrderAggregate {
	
	private String orderId;
	
	private String customerId;
	
	private String deliveryInfo;
	
	private PaymentStatus payment;
	
	private List<RuntimeOrderLineItem> orderLineItems;
	
	private OrderStatusEnum status;
	
	public OrderAggregate(String orderId) {
		this.orderId = orderId;
	}

	public void apply(EventStore event) throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		switch (event.getEventType()) {
		case "OrderRequested":
			this.status = OrderStatusEnum.REQUESTED;
			OrderEventPayloadData orderRequestedEventData = objectMapper.readValue(event.getEventData(), OrderEventPayloadData.class);
			this.customerId = orderRequestedEventData.getUserId();
			this.payment = new PaymentStatus(orderRequestedEventData.getTotal(), 
					orderRequestedEventData.getPaymentInfo());
			this.deliveryInfo = orderRequestedEventData.getDeliveryInfo();
			this.orderLineItems = orderRequestedEventData.getOrderLineItems().stream()
			.map(orderLineItem -> {
				return new RuntimeOrderLineItem(orderLineItem.getProductId(), 
						orderLineItem.getQuantity());
			})
			.collect(Collectors.toList());
			break;
			
		case "OrderApproved":
			this.status = OrderStatusEnum.APPROVED;
			break;
			
		case "OrderRejected":
			this.status = OrderStatusEnum.REJECTED;
			break;

		default:
			break;
		}
	}
	
	public boolean hasAllChecksComplete() {
		boolean isAllCheckComplete = true;
		
		isAllCheckComplete &= !this.getOrderLineItems().stream()
		.anyMatch(orderLineItem -> orderLineItem.isConfirmed() == null);
		
//		isAllCheckComplete &= this.payment.isConfirmed != null;
		
		return isAllCheckComplete;
	}
	
	public boolean hasAllChecksSuccess() {
		boolean isAllCheckComplete = true;
		
		isAllCheckComplete &= !this.getOrderLineItems().stream()
		.anyMatch(orderLineItem -> orderLineItem.isConfirmed() == null || !orderLineItem.isConfirmed());
		
//		isAllCheckComplete &= this.payment.isConfirmed == null || this.payment.isConfirmed;
		
		return isAllCheckComplete;
	}
	
	public List<RuntimeOrderLineItem> getOrderLineItems() {
		return orderLineItems;
	}

	static class PaymentStatus {
		
		private double total;
		
		private String paymentInfo;
		
		private Boolean isConfirmed;

		public PaymentStatus(double total, String paymentInfo) {
			this.total = total;
			this.paymentInfo = paymentInfo;
		}

		public Boolean isConfirmed() {
			return isConfirmed;
		}

		public void setConfirmed(Boolean isConfirmed) {
			this.isConfirmed = isConfirmed;
		}
		
	}

}
