package ecommerce.inventory.consumer;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import javax.annotation.PostConstruct;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.stereotype.Component;

import com.mongodb.client.model.changestream.FullDocument;

import ecommerce.inventory.event.For;
import ecommerce.inventory.model.Inventory;
import ecommerce.inventory.service.InventoryManagementService;

@Component
public class ProductConsumer {
	
	private static final String MONGO_PRODUCT_COLLECTION = "product";
	
	@Autowired
	private ReactiveMongoTemplate reactiveMongoTemplate;
	
	@Autowired
	private InventoryManagementService inventoryManagementService;
	
	@Value("${mongodb.databaseName}")
	private String mongoDBDataBaseName;
	
	private static final String matchOperation = "{ $match : {\r\n"
			+ "					$and:[\r\n"
			+ "							{\"operationType\" : \"insert\" }\r\n"
			+ "						 ]\r\n"
			+ "				}\r\n"
			+ "	  }";
	
	private static final String fullDocumentTransformOperation = "{\r\n"
			+ "		$set : {\r\n"
			+ "			'fullDocument' : {\r\n"
			+ "					'$mergeObjects': {\r\n"
			+ "						'productId' : '$fullDocument.productId'\r\n"
			+ "					}\r\n"
			+ "				}\r\n"
			+ "			}\r\n"
			+ "	  }";
	
	@PostConstruct
	public void init() {
		
		ChangeStreamOptions options = ChangeStreamOptions.builder()
				.fullDocumentLookup(FullDocument.UPDATE_LOOKUP)
				.filter(newAggregation(
						new CustomAggregationExpression(matchOperation),
						new CustomAggregationExpression(fullDocumentTransformOperation)
						))
				.returnFullDocumentOnUpdate()
				.build();
		
		reactiveMongoTemplate
		.changeStream(mongoDBDataBaseName, MONGO_PRODUCT_COLLECTION, options, Product.class)
		.map(ChangeStreamEvent::getBody)
		.subscribe(v -> {
			String productId = v.getProductId();
			
			if (inventoryManagementService.getInventory(productId) == null) {
				For for1 = new For("Product", productId);
				inventoryManagementService.createNewInventory(new Inventory(productId, 0), for1);
			}
		});
	}

}

class Product {
	private String productId;

	public Product(String productId) {
		this.productId = productId;
	}

	public String getProductId() {
		return productId;
	}
}

class CustomAggregationExpression implements AggregationOperation{
	
	private String pipeline;

	CustomAggregationExpression(String pipeLineJson) {
		this.pipeline = pipeLineJson;
	}
	
	@Override
	public Document toDocument(AggregationOperationContext context) {
		return context.getMappedObject(Document.parse(pipeline));
	}
}
