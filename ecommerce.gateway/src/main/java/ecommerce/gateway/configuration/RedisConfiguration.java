package ecommerce.gateway.configuration;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializationContext.RedisSerializationContextBuilder;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.util.Pair;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;

@Configuration
public class RedisConfiguration {

	@Autowired
	private List<String> redisNodes;

	@Bean(name = "redisFactory")
	public RedisConnectionFactory lettuceConnectionFactory() {

		LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
//				.useSsl()
//				.and()
				.commandTimeout(Duration.ofSeconds(2))
				.shutdownTimeout(Duration.ZERO)
				.build();

		Set<RedisNode> nodes = redisNodes.stream()
				.map(redisNode -> {
						String[] splits = redisNode.split(":");
						return new RedisNode(splits[0], Integer.valueOf(splits[1]));
				})
				.collect(Collectors.toSet());

		RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
		redisClusterConfiguration.setClusterNodes(nodes);
		redisClusterConfiguration.setMaxRedirects(2);

		return new LettuceConnectionFactory(redisClusterConfiguration, clientConfig);
	}

	@Bean(name = "reactiveRedisTemplate")
	ReactiveRedisTemplate<Pair<String, String>, String> reactiveRedisTemplate(RedisConnectionFactory redisFactory) {
		
		RedisSerializationContextBuilder<Pair<String, String>, String> newSerializationContext = RedisSerializationContext.<Pair<String, String>, String>newSerializationContext()
//				.key(new Jackson2JsonRedisSerializer(Pair.class))
//				.key(SerializationPair.<Pair<String, String>>fromSerializer(new GenericToStringSerializer(Pair.class)))
				.key(SerializationPair.<Pair<String, String>>fromSerializer(new RedisSerializer<Pair<String, String>>() {

					@Override
					public byte[] serialize(Pair<String, String> t) throws SerializationException {
						// String.format("%s->%s", this.first, this.second);
						return t.toString().getBytes(StandardCharsets.UTF_8);
					}

					@Override
					public Pair<String, String> deserialize(byte[] bytes) throws SerializationException {
						String[] splits = new String(bytes, StandardCharsets.UTF_8).split("->");
						return Pair.of(splits[0], splits[1]);
					}
				}))
				.value(new StringRedisSerializer())
				.hashKey(StringRedisSerializer.UTF_8)
				.hashValue(new GenericToStringSerializer<Long>(Long.class));
		
		return new ReactiveRedisTemplate<Pair<String, String>, String>((ReactiveRedisConnectionFactory)redisFactory, newSerializationContext.build());
	}
	
	@Bean(name = "redisClient")
	public RedisClusterClient lettuceRedisClient() {
		
		Set<RedisURI> redisURIs = redisNodes.stream()
		.map(redisNode -> {
				String[] splits = redisNode.split(":");
				return RedisURI.Builder.redis(splits[0], Integer.valueOf(splits[1])).build();
		})
		.collect(Collectors.toSet());
		
		return RedisClusterClient.create(redisURIs);
		
	}

}
