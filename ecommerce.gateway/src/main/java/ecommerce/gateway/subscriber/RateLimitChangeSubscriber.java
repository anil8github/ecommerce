package ecommerce.gateway.subscriber;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import com.mongodb.client.model.changestream.FullDocument;

import ecommerce.gateway.user.dto.User;

@Component
public class RateLimitChangeSubscriber {

	private static final String MONGO_USER_COLLECTION = "user";
	@Autowired
	private ReactiveMongoTemplate reactiveMongoTemplate;
	
	@Value("${mongodb.databaseName}")
	private String mongoDBDataBaseName;
	
	@Autowired
	private ReactiveRedisTemplate<Pair<String, String>, String> reactiveRedisTemplate;
	
	private String matchOperation = "{'$match': { '$expr': {\r\n"
			+ "	'$gt': [\r\n"
			+ "		{\r\n"
			+ "			'$size': {\r\n"
			+ "						'$filter': {\r\n"
			+ "							input: {\r\n"
			+ "								'$objectToArray': '$updateDescription.updatedFields'\r\n"
			+ "							},\r\n"
			+ "							as: 'change',\r\n"
			+ "							cond: {'$regexMatch': {\r\n"
			+ "									input: '$$change.k',\r\n"
			+ "									regex: /^rateLimits./\r\n"
			+ "								}\r\n"
			+ "							}\r\n"
			+ "						}\r\n"
			+ "					}\r\n"
			+ "		},\r\n"
			+ "		0\r\n"
			+ "	]\r\n"
			+ "}}}";
	
	private String fullDocumentTransformOperation = "{\r\n"
			+ "	'$set' : {\r\n"
			+ "		'fullDocument': {\r\n"
			+ "			'$let': {\r\n"
			+ "						vars: {\r\n"
			+ "							userId: '$fullDocument.userId',\r\n"
			+ "							changes: {\r\n"
			+ "								'$map': {\r\n"
			+ "											input: {\r\n"
			+ "														'$filter': {\r\n"
			+ "															input: {\r\n"
			+ "																'$objectToArray': '$updateDescription.updatedFields'\r\n"
			+ "															},\r\n"
			+ "															as: 'change',\r\n"
			+ "															cond: {'$regexMatch': {\r\n"
			+ "																	input: '$$change.k',\r\n"
			+ "																	regex: /^rateLimits./\r\n"
			+ "																}\r\n"
			+ "															}\r\n"
			+ "														}\r\n"
			+ "											},\r\n"
			+ "											as: 'matched',\r\n"
			+ "											in: {\r\n"
			+ "												'$let': {\r\n"
			+ "													vars: {\r\n"
			+ "														rateLimit: { '$arrayElemAt': [\r\n"
			+ "																				'$fullDocument.rateLimits',\r\n"
			+ "																				{'$toInt': { '$arrayElemAt': [\r\n"
			+ "																										{'$getField': {\r\n"
			+ "																														field: 'captures',\r\n"
			+ "																														input: { $regexFind: { input: \"$$matched.k\", regex: /^rateLimits.([0-9]+)./ } }\r\n"
			+ "																													}\r\n"
			+ "																										},\r\n"
			+ "																										0\r\n"
			+ "																									]\r\n"
			+ "																							}\r\n"
			+ "																				}\r\n"
			+ "																			]\r\n"
			+ "											\r\n"
			+ "														},\r\n"
			+ "														updatedFieldName: {\r\n"
			+ "																			'$arrayElemAt': [\r\n"
			+ "																			{'$getField': {\r\n"
			+ "																							field: 'captures',\r\n"
			+ "																							input: { $regexFind: { input: \"$$matched.k\", regex: /^rateLimits\\.([0-9]+)\\.(.+)/, options: 's' } }\r\n"
			+ "																						}\r\n"
			+ "																			}\r\n"
			+ "																			, 1]\r\n"
			+ "														}\r\n"
			+ "													},\r\n"
			+ "													in: {\r\n"
			+ "													\r\n"
			+ "														'$cond': {\r\n"
			+ "															if: { '$ne': ['$$updatedFieldName', null]},\r\n"
			+ "															then: {\r\n"
			+ "																'$mergeObjects': {\r\n"
			+ "																	'path': '$$rateLimit.path',\r\n"
			+ "																	'limit': {\r\n"
			+ "																		'$cond': {\r\n"
			+ "																			if: { '$eq': ['$$updatedFieldName', 'limit']},\r\n"
			+ "																			then: '$$matched.v',\r\n"
			+ "																			else: '$$rateLimit.limit'\r\n"
			+ "																		}\r\n"
			+ "																	},\r\n"
			+ "																	'per': {\r\n"
			+ "																		'$cond': {\r\n"
			+ "																			if: { '$eq': ['$$updatedFieldName', 'per']},\r\n"
			+ "																			then: '$$matched.v',\r\n"
			+ "																			else: '$$rateLimit.per'\r\n"
			+ "																		}\r\n"
			+ "																	}\r\n"
			+ "																}\r\n"
			+ "															},\r\n"
			+ "															else: {\r\n"
			+ "																'$mergeObjects': {\r\n"
			+ "																	'path': '$$matched.v.path',\r\n"
			+ "																	'limit': '$$matched.v.limit',\r\n"
			+ "																	'per': '$$matched.v.per'\r\n"
			+ "																}\r\n"
			+ "															}\r\n"
			+ "														\r\n"
			+ "														}\r\n"
			+ "													}\r\n"
			+ "												\r\n"
			+ "												}\r\n"
			+ "											}\r\n"
			+ "										}\r\n"
			+ "							}\r\n"
			+ "						},\r\n"
			+ "						in: {\r\n"
			+ "							'$mergeObjects': {\r\n"
			+ "								'userId': '$$userId',\r\n"
			+ "								'rateLimits': {\r\n"
			+ "										   '$reduce':{\r\n"
			+ "											  'input':'$$changes',\r\n"
			+ "											  'initialValue':[\r\n"
			+ "												 \r\n"
			+ "											  ],\r\n"
			+ "											  'in':{\r\n"
			+ "												 '$let':{\r\n"
			+ "													'vars':{\r\n"
			+ "													   'matches':{\r\n"
			+ "														  '$filter':{\r\n"
			+ "															 'input':'$$value',\r\n"
			+ "															 'as':'curr',\r\n"
			+ "															 'cond':{\r\n"
			+ "																'$eq':[\r\n"
			+ "																   '$$curr.path',\r\n"
			+ "																   '$$this.path'\r\n"
			+ "																]\r\n"
			+ "															 }\r\n"
			+ "														  }\r\n"
			+ "													   }\r\n"
			+ "													},\r\n"
			+ "													'in':{\r\n"
			+ "													   '$cond':{\r\n"
			+ "														  'if':{\r\n"
			+ "															 '$gt':[\r\n"
			+ "																{\r\n"
			+ "																   '$size':'$$matches'\r\n"
			+ "																},\r\n"
			+ "																0\r\n"
			+ "															 ]\r\n"
			+ "														  },\r\n"
			+ "														  'then':{\r\n"
			+ "															 '$concatArrays':[\r\n"
			+ "																{\r\n"
			+ "																   '$filter':{\r\n"
			+ "																	  'input':'$$value',\r\n"
			+ "																	  'as':'curr',\r\n"
			+ "																	  'cond':{\r\n"
			+ "																		 '$ne':[\r\n"
			+ "																			'$$curr.path',\r\n"
			+ "																			'$$this.path'\r\n"
			+ "																		 ]\r\n"
			+ "																	  }\r\n"
			+ "																   }\r\n"
			+ "																},\r\n"
			+ "																['$$this']\r\n"
			+ "															 ]\r\n"
			+ "														  },\r\n"
			+ "														  'else':{\r\n"
			+ "															 '$concatArrays':[\r\n"
			+ "																'$$value',\r\n"
			+ "																['$$this']\r\n"
			+ "															 ]\r\n"
			+ "														  }\r\n"
			+ "													   }\r\n"
			+ "													}\r\n"
			+ "												 }\r\n"
			+ "											  }\r\n"
			+ "										   }\r\n"
			+ "										}\r\n"
			+ "							}\r\n"
			+ "						}\r\n"
			+ "					\r\n"
			+ "					}\r\n"
			+ "		}\r\n"
			+ "	}\r\n"
			+ "}";
	
	@PostConstruct
	public void init() {
		
		final ReactiveHashOperations<Pair<String, String>, String, Long> opsForHash = reactiveRedisTemplate.<String, Long>opsForHash();
		
		ChangeStreamOptions options = ChangeStreamOptions.builder()
				.fullDocumentLookup(FullDocument.UPDATE_LOOKUP)
				.filter(newAggregation(
						new CustomAggregationExpression(matchOperation),
						new CustomAggregationExpression(fullDocumentTransformOperation)
						))
				.returnFullDocumentOnUpdate()
				.build();
		
		
		reactiveMongoTemplate
		.changeStream(mongoDBDataBaseName, MONGO_USER_COLLECTION, options, User.class)
		.map(ChangeStreamEvent::getBody)
		.subscribe(v -> {
			v.getRateLimits().stream()
			.forEach(rateLimit -> {
				Map<String, Long> propValMap = new HashMap<String, Long>();
				propValMap.put("limit", rateLimit.getLimit());
				propValMap.put("per", rateLimit.getPer());
				opsForHash
				.putAll(Pair.of(v.getUserId(), rateLimit.getPath()), propValMap)
				.subscribe(
						success -> {
							if (!success) {
								throw new RuntimeException("rateLimit change unsuccessful for userId: " + v.getUserId() );
							} else {
								System.out.println("Successfully saved rateLimit change for userId: " + v.getUserId() );
							}
						},
						throwable -> {
							throw new RuntimeException(throwable);
						}
						);
			});		
		});
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
