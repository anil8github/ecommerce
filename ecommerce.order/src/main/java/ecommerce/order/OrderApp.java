package ecommerce.order;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.Acknowledgment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.order.aggregate.OrderAggregate;
import ecommerce.order.aggregate.RejectionEnum;
import ecommerce.order.aggregate.RuntimeOrderLineItem;
import ecommerce.order.dto.EventStore;
import ecommerce.order.event.inventory.Event;
import ecommerce.order.event.inventory.InventoryEventPayloadData;
import ecommerce.order.repository.EventStoreRepository;
import ecommerce.order.service.OrderManagementService;

@EnableDiscoveryClient
@SpringBootApplication
public class OrderApp {
	
	private static final String propertiesFile = "ecommerce-kafka-broker.properties";
	
	@Autowired
	private OrderManagementService orderManagementService;
	
	@Autowired
	private EventStoreRepository eventStoreRepository;
	
	private static Properties brokerConfig = new Properties();
	{
		try {
			brokerConfig.load(this.getClass().getClassLoader().getResourceAsStream(propertiesFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		SpringApplication.run(OrderApp.class, args);
	}
	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<Integer, String> batchFactory() {
		ConcurrentKafkaListenerContainerFactory<Integer, String> factory =
	            new ConcurrentKafkaListenerContainerFactory<>();
	    factory.setConsumerFactory(consumerFactory());
	    factory.setBatchListener(true);
	    factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
	    return factory;
	}
	
	@Bean
    public ConsumerFactory<Integer, Object> consumerFactory() {
		Map<String, Object> keyValueMap = new HashMap<>();
		brokerConfig.forEach((key, value) -> {
			keyValueMap.put(key.toString(), value);
		});
        return new DefaultKafkaConsumerFactory<Integer, Object>(keyValueMap);
    }
	
	@KafkaListener(id = "inventory_events", groupId = "inventory-worker-group", topics = "e-commerce.inventory.event_store", containerFactory = "batchFactory")
	public void pollResults(ConsumerRecords<Integer, String> records, Acknowledgment ack) {
		List<Event> events = new ArrayList<>(records.count());
		
		records.forEach(record -> {
			JSONObject json = new JSONObject(record.value());
			String insertPayload = ((JSONObject)json.get("payload")).get("after").toString();
			ObjectMapper objectMapper = new ObjectMapper();
			Event inventoryEvent;
			try {
				inventoryEvent = objectMapper.readValue(insertPayload, Event.class);
				events.add(inventoryEvent);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		});
		
		events.stream()
		.filter(event -> "Order".equals(event.getEventData().getFor1().getAggregateType()))
		.forEach(event -> {
				InventoryEventPayloadData eventData = event.getEventData();
				String orderId = eventData.getFor1().getAggregateId();
				String productId = event.getAggregateId();
				OrderAggregate orderAggregate = orderManagementService.getOrder(orderId);
				
				//TODO Dirty code removal
				if (orderAggregate == null) {
					System.out.println(orderId + " from inventory not found");
					return;
				}
				final List<RuntimeOrderLineItem> orderLineItems = orderAggregate.getOrderLineItems();
				switch (event.getEventType()) {
				case "InventoryReserved":
					orderLineItems.stream()
					.filter(orderLineItem -> orderLineItem.getProductId().equals(productId))
					.forEach(orderLineItem -> orderLineItem.setConfirmed(true));
					if (orderAggregate.hasAllChecksComplete() && orderAggregate.hasAllChecksSuccess()) {
						EventStore eventStore = new EventStore("OrderApproved", "Order", orderId, "");
						eventStoreRepository.save(eventStore);
					}
					break;
					
				case "InsufficientInventory":
					orderLineItems.stream()
					.filter(orderLineItem -> orderLineItem.getProductId().equals(productId))
					.forEach(orderLineItem -> orderLineItem.setConfirmed(false));
					String jsonPayload;
					try {
						jsonPayload = new ObjectMapper().writeValueAsString(Arrays.asList(RejectionEnum.INSUFFICIENT_INVENTORY));
						EventStore eventStore = new EventStore("OrderRejected", "Order", orderId, jsonPayload);
						eventStoreRepository.save(eventStore);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
					break;
					
				default:
					break;
				}
				
		});
		ack.acknowledge();
	}

}
