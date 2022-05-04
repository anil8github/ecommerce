package ecommerce.gateway.filter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.connection.ReactiveRedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.lettuce.core.Range;
import io.lettuce.core.Range.Boundary;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.codec.RedisCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import reactor.core.publisher.Mono;

@Component(value = "rateLimitingFilter")
public class RateLimitingFilter implements GatewayFilter {
	
	private static final String RATE_LIMIT_PREFIX = "RateLimit";

	@Autowired
	private ReactiveRedisTemplate<Pair<String, String>, String> reactiveRedisTemplate;
	
	private ReactiveHashOperations<Pair<String, String>, String, Long> opsForHash;
	
	@Autowired
	private RedisClusterClient redisClient;
	
	private ReactiveRedisClusterConnection reactiveClusterConnection;
	
	@PostConstruct
	public void init() {
		opsForHash = reactiveRedisTemplate.<String, Long>opsForHash();
		reactiveClusterConnection = reactiveRedisTemplate.getConnectionFactory().getReactiveClusterConnection();
	}
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		
		ServerHttpRequest request = exchange.getRequest();
		ServerHttpResponse response = exchange.getResponse();
		
		String userId = request.getHeaders().getOrEmpty("sub").get(0);
		
		String uriPath = request.getURI().getPath();
		
		Pair<String, String> access_Key = Pair.of(userId, uriPath);
		Mono<Boolean> isAllowed = opsForHash
		.multiGet(access_Key, Arrays.asList("limit", "per"))
		.flatMap(list -> {
			
			Long limit = list.get(0);
			Long per = list.get(1);
			
			String rateLimitKey = RATE_LIMIT_PREFIX + "->" + access_Key.toString();
			Mono<RedisClusterNode> clusterGetNodeForKey = reactiveClusterConnection.clusterGetNodeForKey(convertUTF8ToByteBuff(rateLimitKey));
			
			return clusterGetNodeForKey
						.flatMap(node -> {
							CompletableFuture<StatefulRedisConnection<Pair<String, String>, Long>> connectionAsync = redisClient
									.connect(new RedisCodec<Pair<String, String>, Long>() {
			
										@Override
										public Pair<String, String> decodeKey(ByteBuffer bytes) {
											String[] splits = Unpooled.wrappedBuffer(bytes).toString(StandardCharsets.UTF_8).split("->");
											return Pair.of(splits[1], splits[2]);
										}
			
										@Override
										public Long decodeValue(ByteBuffer bytes) {
											String rawLong = Unpooled.wrappedBuffer(bytes).toString(StandardCharsets.UTF_8);
											return Long.valueOf(rawLong);
										}
			
										@Override
										public ByteBuffer encodeKey(Pair<String, String> key) {
											String keyRaw = RATE_LIMIT_PREFIX + "->" + key.toString();
											
											return convertUTF8ToByteBuff(keyRaw);
										}
			
			
										@Override
										public ByteBuffer encodeValue(Long value) {
											return convertUTF8ToByteBuff(value.toString());
										}
										
									})
									.getConnectionAsync(node.getHost(), node.getPort());
							return Mono.fromCompletionStage(connectionAsync)
									.flatMap(conn -> {
										RedisReactiveCommands<Pair<String, String>, Long> reactive = conn
										.reactive();
										
										return reactive
										.multi()
										.flatMap(res -> {
											Instant now = Instant.now();
											
											reactive
											.zremrangebyscore(access_Key
												, 
												Range.<Long>from(
														Boundary.including(0L)
														, Boundary.excluding(Date.from(now.minus(per, ChronoUnit.MILLIS)).getTime()))
											)
											.subscribe();
											
											reactive
											.zrange(access_Key, 0, -1)
											.subscribe();
											
											long nowInMilis = Date.from(now).getTime();
											reactive
											.zadd(access_Key, nowInMilis, nowInMilis)
											.subscribe();
											
											reactive
											.expire(access_Key, per/1000)
											.subscribe();
											
											return reactive
													.exec()
													.doOnNext(it -> {
														System.out.println("RateLimit txn results : " + it + ": " + Thread.currentThread());
														it.forEach(System.out::println);
														it.<List<Long>>get(1)
														.forEach(accessMilis -> {
															System.out.print(Instant.ofEpochMilli(accessMilis) + ", ");
														});
													});
										})
										.flatMap(tr -> {
											return tr.wasDiscarded() ? Mono.error(new RuntimeException("Something went wrong in Redis RateLimiter transaction")) : Mono.just(tr);
										})
										.map(results -> {
											long consumed = ((List<Long>)results.get(1)).size();
											return Boolean.valueOf(consumed < limit);
										});
									});
						});
		});
		
		return isAllowed.flatMap(allowed -> {
			
			if (allowed) {
				return chain
						.filter(exchange)
						//Post Filter Logic
						.then(Mono.fromRunnable(() -> {}))
						;
			} else {
				response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
				return response.setComplete();
			}
		});
		
//		isAllowed.subscribe(v -> {
//			System.out.println("Is RateLimit Allowed :" + v);
//		},
//		throwable -> {
//			throw new RuntimeException(throwable);
//		}
//		);
//		
//		return chain
//				.filter(exchange)
//				//Post Filter Logic
//				.then(Mono.fromRunnable(() -> {}))
//				;
	}
	
	private ByteBuffer convertUTF8ToByteBuff(String keyRaw) {
		if (keyRaw.length() == 0) {
			return ByteBuffer.wrap(new byte[0]);
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(ByteBufUtil.utf8MaxBytes(keyRaw));
		
		ByteBuf byteBuf = Unpooled.wrappedBuffer(buffer);
		byteBuf.clear();
		ByteBufUtil.writeUtf8(byteBuf,keyRaw);
		buffer.limit(byteBuf.writerIndex());
		
		return buffer;
	}
	
}
