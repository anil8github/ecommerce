package ecommerce.order.event.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ecommerce.order.event.inventory.serializer.InventoryEventPayloadDeserializer;

@JsonIgnoreProperties({ "ID" })
public class Event {

	@JsonProperty("EVENT_TYPE")
	private String eventType;
	
	@JsonProperty("AGGREGATE_TYPE")
	private String aggregateType;
	
	@JsonProperty("AGGREGATE_ID")
	private String aggregateId;
	
	@JsonProperty("EVENT_DATA")
	@JsonDeserialize(using = InventoryEventPayloadDeserializer.class)
	private InventoryEventPayloadData eventData;
	
	public String getEventType() {
		return eventType;
	}
	public String getAggregateType() {
		return aggregateType;
	}
	public String getAggregateId() {
		return aggregateId;
	}
	public InventoryEventPayloadData getEventData() {
		return eventData;
	}
	
	
}
