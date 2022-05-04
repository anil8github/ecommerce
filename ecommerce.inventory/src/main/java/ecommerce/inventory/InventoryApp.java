package ecommerce.inventory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

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

import ecommerce.inventory.dto.EventStore;
import ecommerce.inventory.dto.Order;
import ecommerce.inventory.event.For;
import ecommerce.inventory.event.InventoryEventData;
import ecommerce.inventory.event.order.Event;
import ecommerce.inventory.event.order.OrderEventPayloadData;
import ecommerce.inventory.event.order.OrderLineItem;
import ecommerce.inventory.model.Inventory;
import ecommerce.inventory.repository.EventStoreRepository;
import ecommerce.inventory.repository.OrderRepository;
import ecommerce.inventory.service.InventoryManagementService;

@EnableDiscoveryClient
@SpringBootApplication
public class InventoryApp {
	
	private static final String propertiesFile = "ecommerce-kafka-broker.properties";
	
	@Autowired
	private InventoryManagementService inventoryManagementService;
	
	@Autowired
	private OrderRepository orderRepository;
	
	private static Properties brokerConfig = new Properties();
	{
		try {
			brokerConfig.load(this.getClass().getClassLoader().getResourceAsStream(propertiesFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Autowired
	private EventStoreRepository eventStoreRepository;

	public static void main(String[] args) {
		SpringApplication.run(InventoryApp.class, args);
	}
	
	@Transactional //Very very important
	@KafkaListener(id = "order_events", topics = "e-commerce.orders.event_store", containerFactory = "batchFactory")
	public void pollResults(ConsumerRecords<Integer, String> records, Acknowledgment ack) {
		records.forEach(record -> {
			System.out.println("Order Id -> " +record.key());
			System.out.println("Order Details -> " +record.value());
			JSONObject json = new JSONObject(record.value());
			ObjectMapper objectMapper = new ObjectMapper();
			String insertPayload = ((JSONObject)json.get("payload")).get("after").toString();
			ObjectMapper objectMapper2 = new ObjectMapper();
			Event orderEvent;
			try {
				orderEvent = objectMapper.readValue(insertPayload, Event.class);
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
				return;
			}
			String orderId = orderEvent.getAggregateId();
			String eventDataRaw = orderEvent.getEventData();
			For for1 = new For("Order", orderId);
			switch (orderEvent.getEventType()) {
			case "OrderRequested":
				
				try {
					OrderEventPayloadData eventData = new ObjectMapper().readValue(eventDataRaw, OrderEventPayloadData.class);
					List<OrderLineItem> orderLineItems = eventData.getOrderLineItems();
					
					List<OrderLineItem> insufficientInventoryLineItems = orderLineItems.stream()
							.filter(orderLineItem -> {
								String productId = orderLineItem.getProductId();
								long quantity = orderLineItem.getQuantity();
								return !inventoryManagementService.hasSufficientInventory(productId, quantity);
							})
							.collect(Collectors.toList());
					
					if (insufficientInventoryLineItems.isEmpty()) {
						List<ecommerce.inventory.dto.OrderLineItem> lineItems = new ArrayList<>();
						orderLineItems.forEach(orderLineItem -> {
							String productId = orderLineItem.getProductId();
							long quantity = orderLineItem.getQuantity();
							lineItems.add(new ecommerce.inventory.dto.OrderLineItem(orderId, productId, quantity));
						});
						orderRepository.save(new ecommerce.inventory.dto.Order(orderId, lineItems));
						
						orderLineItems.forEach(orderLineItem -> {
							String productId = orderLineItem.getProductId();
							long quantity = orderLineItem.getQuantity();
							Inventory inventory = inventoryManagementService.getInventory(productId);
							inventoryManagementService.updateInventory(productId, inventory.getQuantity() - quantity, for1);
						});
					} else {
						insufficientInventoryLineItems.forEach(orderLineItem -> {
							String productId = orderLineItem.getProductId();
							long quantity = orderLineItem.getQuantity();
							InventoryEventData inventoryReserveFailedEventData = new InventoryEventData(productId, quantity, for1);
							try {
								String jsonEventData = objectMapper2.writeValueAsString(inventoryReserveFailedEventData);
								EventStore eventStore = new EventStore("InsufficientInventory", "Inventory", productId, jsonEventData);
								eventStoreRepository.save(eventStore);
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							}
						});
					}
				} catch (JsonProcessingException e1) {
					e1.printStackTrace();
				}
				break;
				
			case "OrderCancelled":
			case "OrderRejection":
				Optional<Order> optional = orderRepository.findById(orderId);
				if (optional.isPresent()) {
					Order order = optional.get();
					List<ecommerce.inventory.dto.OrderLineItem> lineItems = order.getLineItems();
					lineItems
					.forEach(orderLineItem -> {
						String productId = orderLineItem.getProductId();
						Inventory inventory = inventoryManagementService.getInventory(productId);
						inventoryManagementService.updateInventory(productId, inventory.getQuantity() + orderLineItem.getQuantity(), for1);
					});
				}
				break;
				
			case "OrderDelevered":
				optional = orderRepository.findById(orderId);
				if (optional.isPresent()) {
					orderRepository.delete(optional.get());
				}
				
			default:
				break;
			}
		});
		ack.acknowledge();
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

}
