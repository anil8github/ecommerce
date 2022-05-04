package ecommerce.inventory.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class EventStore {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private String eventType;
	
	private String aggregateType;
	
	private String aggregateId;
	
	public EventStore() {
	}
	
	@Lob
	@Column(name = "EVENT_DATA", columnDefinition="CLOB")
	private String eventData;

	public EventStore(String eventType, String aggregateType, String aggregateId, String eventData) {
		this.eventType = eventType;
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.eventData = eventData;
	}

	public String getEventData() {
		return eventData;
	}

	public void setEventData(String eventData) {
		this.eventData = eventData;
	}

	public String getEventType() {
		return eventType;
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public String getAggregateId() {
		return aggregateId;
	}
	
	
	
	

}
