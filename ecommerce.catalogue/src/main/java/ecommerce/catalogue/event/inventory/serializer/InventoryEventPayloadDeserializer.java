package ecommerce.catalogue.event.inventory.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import ecommerce.catalogue.event.inventory.InventoryEventPayloadData;

public class InventoryEventPayloadDeserializer extends StdDeserializer<InventoryEventPayloadData> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected InventoryEventPayloadDeserializer(Class<?> vc) {
		super(vc);
	}
	
    public InventoryEventPayloadDeserializer() { 
        this(null); 
    } 

	@Override
	public InventoryEventPayloadData deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String payload = parser.getText();
		
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(payload, InventoryEventPayloadData.class);
	}

}
