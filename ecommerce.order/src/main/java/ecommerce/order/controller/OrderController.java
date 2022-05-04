package ecommerce.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ecommerce.order.cosumer.OrderEventConsumer;
import ecommerce.order.event.OrderEventPayloadData;
import ecommerce.order.service.OrderManagementService;
import reactor.core.publisher.Flux;

@RestController
public class OrderController {
	
	@Autowired
	private OrderManagementService orderManagementService;
	
	@Autowired
	private OrderEventConsumer orderEventConsumer;
	
	@PutMapping("/order")
	public String createOrder(@RequestBody OrderEventPayloadData orderRequested) {
		return orderManagementService.createNewOrder(orderRequested);
	}
	
	@GetMapping(value = "/order/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> getEventStream(@RequestParam("orderId") String orderId) {
		
		 return orderEventConsumer.getEventStream(orderId); 
	}
	
	@DeleteMapping(value = "/order/events")
    public void deleteEventStream(@RequestParam("orderId") String orderId) {
		
		 orderEventConsumer.deleteEventStream(orderId); 
	}

}
