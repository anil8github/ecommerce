package ecommerce.order.cosumer;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.json.JSONObject;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

@Component
public class OrderEventConsumer {
	
	/*
	 * private static final String propertiesFile =
	 * "ecommerce-kafka-broker.properties";
	 */
	
	private Sinks.Many<ServerSentEvent<String>> sink;
	
	private Map<String, Sinks.Many<ServerSentEvent<String>>> orderStatusSinkMap = null;
	
	@PostConstruct
	public void init() {
		sink = Sinks.many().multicast().onBackpressureBuffer();
		orderStatusSinkMap = new HashMap<>();
	}
	
	@KafkaListener(id = "order_events", groupId = "order_server_sent_events", topics = "e-commerce.orders.event_store", containerFactory = "batchFactory")
	public void pollResults(ConsumerRecords<Integer, String> records, Acknowledgment ack) {
		records.forEach(record -> {
			JSONObject json = new JSONObject(record.value());
			String insertPayload = ((JSONObject)json.get("payload")).get("after").toString();
			JSONObject jsonObject = new JSONObject(insertPayload);
			String eventType = (String)jsonObject.get("EVENT_TYPE");
			String orderId = (String)jsonObject.get("AGGREGATE_ID");
			
			ServerSentEvent<String> sse = null;
			if ("OrderRejected".equals(eventType)) {
				String eventData = (String) jsonObject.get("EVENT_DATA");
				sse = ServerSentEvent.builder(eventData)
						.event(eventType)
						.id(orderId)
						.build();
			} else {
				sse = ServerSentEvent.builder("")
						.event(eventType)
						.id(orderId)
						.build();
			}
			
			sink = orderStatusSinkMap.get(sse.id());
			if (sink == null) {
				sink = Sinks.many().multicast().onBackpressureBuffer();
				orderStatusSinkMap.put(sse.id(), sink);
			}
			EmitResult emitResult = sink.tryEmitNext(sse);
			if (emitResult.isFailure()) {
				System.out.println("Error emiting");
			}
		});
	}
	
	public Flux<ServerSentEvent<String>> getEventStream(String orderId) {
		Sinks.Many<ServerSentEvent<String>> sink = orderStatusSinkMap.get(orderId);
		if (sink == null) {
			sink = Sinks.many().multicast().onBackpressureBuffer();
			orderStatusSinkMap.put(orderId, sink);
		}
		return sink.asFlux();
	}
	
	public void deleteEventStream(String orderId) {
		orderStatusSinkMap.get(orderId).emitComplete(new Sinks.EmitFailureHandler() {
			
			@Override
			public boolean onEmitFailure(SignalType signalType, EmitResult emitResult) {
				return false;
			}
		});
		orderStatusSinkMap.remove(orderId);
	}
	
	

}
