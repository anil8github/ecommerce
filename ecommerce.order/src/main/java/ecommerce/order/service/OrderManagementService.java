package ecommerce.order.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.order.aggregate.OrderAggregate;
import ecommerce.order.dto.EventStore;
import ecommerce.order.event.OrderEventPayloadData;
import ecommerce.order.repository.EventStoreRepository;

@Service
public class OrderManagementService {
	
	@Autowired
	private EventStoreRepository eventStoreRepository;
	
	private static Map<String, OrderAggregate> orderMap = null;
	
	@PostConstruct
	public void init() {
		if (orderMap == null) {
			orderMap = new HashMap<>();
			List<EventStore> events = new ArrayList<EventStore>();
			eventStoreRepository.findAll()
			.forEach(event -> events.add(event));
			events.stream()
			.filter(event -> Arrays.asList("Order").contains(event.getAggregateType()))
			.forEach(event -> {
				String aggregateId = event.getAggregateId();
				OrderAggregate orderAggregate = orderMap.get(aggregateId);
				if (orderAggregate == null) {
					orderAggregate = new OrderAggregate(aggregateId);
					orderMap.put(aggregateId, orderAggregate);
				}
				try {
					orderAggregate.apply(event);
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			});
			/*.collect(Collectors.groupingBy(EventStore::getAggregateId, 
					Collectors.reducing(new OrderAggregate(), 
							(event) -> {
								
								switch (event.getEventType()) {
								case "OrderRequested":
									
									break;

								default:
									break;
								}
								
								return new OrderAggregate();
							},
							(agg1, agg2) -> {
								return agg1;
							}) ));*/
		}
	}
	
	public String createNewOrder(OrderEventPayloadData orderRequested) {
		try {
			String aggregateId = eventStoreRepository.getNewOrderId() + "|" + orderRequested.getUserId();
			String jsonEventData = new ObjectMapper().writeValueAsString(orderRequested);
			EventStore eventStore = new EventStore("OrderRequested", "Order", aggregateId, jsonEventData);
			eventStoreRepository.save(eventStore);
			OrderAggregate orderAggregate = new OrderAggregate(aggregateId);
			orderAggregate.apply(eventStore);
			orderMap.put(aggregateId, orderAggregate);
			return aggregateId;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public OrderAggregate getOrder(String orderId) {
		return orderMap.get(orderId);
	}

}
