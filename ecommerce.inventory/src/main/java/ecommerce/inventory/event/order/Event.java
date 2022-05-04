package ecommerce.inventory.event.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "ID" })
public class Event {
	
	@JsonProperty("EVENT_TYPE")
	private String eventType;
	
	@JsonProperty("AGGREGATE_TYPE")
	private String aggregateType;
	
	@JsonProperty("AGGREGATE_ID")
	private String aggregateId;
	
	@JsonProperty("EVENT_DATA")
	private String eventData;
	
	public String getEventType() {
		return eventType;
	}
	public String getAggregateType() {
		return aggregateType;
	}
	public String getAggregateId() {
		return aggregateId;
	}
	public String getEventData() {
		return eventData;
	}

}
