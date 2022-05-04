package ecommerce.inventory.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.inventory.dto.EventStore;
import ecommerce.inventory.event.InventoryEventData;
import ecommerce.inventory.event.For;
import ecommerce.inventory.model.Inventory;
import ecommerce.inventory.repository.EventStoreRepository;

@Service
public class InventoryManagementService {
	
	@Autowired
	private EventStoreRepository eventStoreRepository;
	
	private static Map<String, Long> productInventory = null;
	
	@PostConstruct
	public void init() {
		if (productInventory == null) {
			List<EventStore> events = new ArrayList<EventStore>();
			eventStoreRepository.findAll()
			.forEach(event -> events.add(event));
			productInventory = events.stream()
			.filter(event -> Arrays.asList("InventoryReserved", "InventoryAdjusted").contains(event.getEventType()))
			.collect(Collectors.groupingBy(EventStore::getAggregateId, Collectors.summingLong(event -> getValue(event)) ));
		}
	}
	
	private static long getValue(EventStore e) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			InventoryEventData eventData = objectMapper.readValue(e.getEventData(), InventoryEventData.class);
			if ("InventoryAdjusted".equals(e.getEventType())) {
				return eventData.getQuantity();
			} else {
				return - eventData.getQuantity();
			}
		} catch (JsonMappingException e1) {
			e1.printStackTrace();
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		return 0;
	}
	
	public void createNewInventory(Inventory inventoryRequested, For for1) {
		String productId = inventoryRequested.getProductId();
		long quantity = inventoryRequested.getQuantity();
		InventoryEventData inventoryReservedEventData = new InventoryEventData(productId, quantity, for1);
		try {
			
			String jsonEventData = new ObjectMapper().writeValueAsString(inventoryReservedEventData);
			EventStore eventStore = new EventStore("InventoryAdjusted", "Inventory", productId, jsonEventData);
			eventStoreRepository.save(eventStore);
			productInventory.put(productId, quantity);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	
	public void updateInventory(String productId, long quantity, For for1) {
		Long oldQuantity = productInventory.get(productId);
		String jsonEventData = null;
		EventStore eventStore = null;
		long differenceInQuantity = Math.abs(quantity - oldQuantity);
		if (differenceInQuantity == 0) {
			return;
		}
		else if (oldQuantity < quantity) {
			InventoryEventData inventoryAdjustedEventData = new InventoryEventData(productId, differenceInQuantity, for1);
			try {
				jsonEventData = new ObjectMapper().writeValueAsString(inventoryAdjustedEventData);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			eventStore = new EventStore("InventoryAdjusted", "Inventory", productId, jsonEventData);
		} else if (oldQuantity > quantity) {
			InventoryEventData inventoryReservedEventData = new InventoryEventData(productId, differenceInQuantity, for1);
			try {
				jsonEventData = new ObjectMapper().writeValueAsString(inventoryReservedEventData);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			eventStore = new EventStore("InventoryReserved", "Inventory", productId, jsonEventData);
		}
		eventStoreRepository.save(eventStore);
		productInventory.put(productId, quantity);
	}

	public Inventory getInventory(String productId) {
		Long quantity = productInventory.get(productId);
		if (quantity != null) {
			return new Inventory(productId, quantity);
		}
		return null;
	}

	public List<Inventory> getInventories() {
		return productInventory.entrySet().stream()
		.map(entry -> {
			return new Inventory(entry.getKey(), entry.getValue());
		})
		.collect(Collectors.toList());
	}
	
	public boolean hasSufficientInventory(String productId, long requestedQuantity) {
		return productInventory.containsKey(productId) && productInventory.get(productId) >= requestedQuantity;
	}

}
