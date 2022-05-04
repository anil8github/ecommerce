package ecommerce.catalogue.consumer;

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
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.catalogue.event.inventory.Event;
import ecommerce.catalogue.service.CatalogueService;

@Component
public class InventoryConsumer {
	
	private static final String propertiesFile = "ecommerce-kafka-broker.properties";
	
	@Autowired
	private CatalogueService catalogueService;
	
	private static Properties brokerConfig = new Properties();
	{
		try {
			brokerConfig.load(this.getClass().getClassLoader().getResourceAsStream(propertiesFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Bean
    public ConsumerFactory<Integer, Object> consumerFactory() {
		Map<String, Object> keyValueMap = new HashMap<>();
		brokerConfig.forEach((key, value) -> {
			keyValueMap.put(key.toString(), value);
		});
        return new DefaultKafkaConsumerFactory<Integer, Object>(keyValueMap);
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
	
	@KafkaListener(id = "inventory_events", groupId = "inventory-to-catalogue-worker-group", topics = "e-commerce.inventory.event_store", containerFactory = "batchFactory")
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
		
		List<String> relevantEventType = Arrays.asList("InventoryReserved", "InventoryAdjusted");
		
		events.stream()
		.filter(event -> relevantEventType.contains(event.getEventType()))
		.forEach(event -> {
			long value = 0;
			
			switch (event.getEventType()) {
			case "InventoryReserved":
				value = - event.getEventData().getQuantity();
				break;

			case "InventoryAdjusted":
				value = + event.getEventData().getQuantity();
				break;
			default:
				break;
			}
			
			catalogueService.addStockToProduct(event.getAggregateId(), value);
		});
	}

}
