package ecommerce.catalogue.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

import ecommerce.catalogue.conversions.ProductReadConverter;
import ecommerce.catalogue.conversions.ProductWriteConverter;

@Configuration
public class CatalogueServerConfiguration {
	
	@Autowired
	private List<String> mongodbNodes;
	
	@Value("${mongodb.databaseName}")
	private String mongoDBDataBaseName;
	
	@Bean(name = "reactiveMongoTemplate")
	public ReactiveMongoTemplate mongodbReactiveTemplate() {
		MongoClient mongoClient = mongoClient();
		
		ReactiveMongoTemplate reactiveMongoTemplate = new ReactiveMongoTemplate(mongoClient, mongoDBDataBaseName);
		
		MappingMongoConverter converter = (MappingMongoConverter) reactiveMongoTemplate.getConverter();
		
		List<Converter<?, ?>> asList = Arrays.asList(new ProductReadConverter(), new ProductWriteConverter());
		converter.setCustomConversions(new MongoCustomConversions(asList));
		converter.afterPropertiesSet();
		
		return reactiveMongoTemplate;
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
