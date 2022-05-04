package ecommerce.gateway.configuration;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

@Configuration
public class SubscriberConfiguration {
	
	@Autowired
	private List<String> mongodbNodes;
	
	@Value("${mongodb.databaseName}")
	private String mongoDBDataBaseName;
	
	@Bean(name = "reactiveMongoTemplate")
	public ReactiveMongoTemplate mongodbReactiveTemplate() {
		MongoClient mongoClient = mongoClient();
		
		return new ReactiveMongoTemplate(mongoClient, mongoDBDataBaseName);
	}

	@Bean(name = "mongoClient")
	public MongoClient mongoClient() {
		return MongoClients.create(
	            MongoClientSettings.builder()
	                    .applyToClusterSettings(builder ->
	                            builder.hosts(
	                            		mongodbNodes.stream()
	                            			.map(raw -> {
	                            				String[] splits = raw.split(":");
	                            				return new ServerAddress(splits[0], Integer.parseInt(splits[1]));
	                            			})
	                            			.collect(Collectors.toList())
	                            		))
	                    .build());
	}

}
