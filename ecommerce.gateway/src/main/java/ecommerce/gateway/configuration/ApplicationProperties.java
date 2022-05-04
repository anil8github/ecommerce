package ecommerce.gateway.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class ApplicationProperties {
	
	@Bean(name = "redisNodes")
	@ConfigurationProperties(prefix = "redis.cluster.nodes")
	public List<String> getRedisNodes() {
		return new ArrayList<String>();
	}
	
	@Bean(name = "mongodbNodes")
	@ConfigurationProperties(prefix = "mongodb.cluster.nodes")
	public List<String> getMongodbNodes() {
		return new ArrayList<String>();
	}
	
}
