package ecommerce.gateway.filter;

import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest.Builder;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import reactor.core.publisher.Mono;

@Component(value = "authFilter")
public class AuthenticationFilter implements GatewayFilter {
	
	private static final String AUTHORIZATION = "Authorization";

	@Autowired
	PublicKey jwtPublicKey;
	
	@Value("${jwt.renewal-threshlod}")
	private long renewalThreshold;
	
	@Value("${server.host}")
	private String gatewayHost;
	
	@Value("${server.port}")
	private int gatewayPort;
	
	private boolean isTokenRenewalRequired = true;
	
	public void setTokenRenewalRequired(boolean isTokenRenewalRequired) {
		this.isTokenRenewalRequired = isTokenRenewalRequired;
	}

	public AuthenticationFilter() {
		this(true);
	}
	
	public AuthenticationFilter(boolean isTokenRenewalRequired) {
		this.isTokenRenewalRequired = isTokenRenewalRequired;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		
		ServerHttpRequest request = exchange.getRequest();
		ServerHttpResponse response = exchange.getResponse();
		
		List<String> auths = request.getHeaders().getOrEmpty(AUTHORIZATION);
		
		if (auths.isEmpty()) {
			response.setStatusCode(HttpStatus.BAD_REQUEST);
			return response.setComplete();
		}
		String jwtToken = auths.get(0);
		
		Builder requestMutationBuilder = request.mutate();
		
		try {
			Jwts.parser()
			.setSigningKey(jwtPublicKey)
			.parseClaimsJws(jwtToken)
			.getBody()
			.entrySet()
			.stream()
			.filter(entry -> Arrays.asList("sub", "roles").contains(entry.getKey()))
			.forEach(entry -> {
				requestMutationBuilder.header(entry.getKey(), String.valueOf(entry.getValue()));
			});
			requestMutationBuilder.build();
		} catch (SignatureException se) {
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return response.setComplete();
		} catch (ExpiredJwtException ee) {
			//TODO Renew the token
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return response.setComplete();
		} catch (JwtException jge) {
			response.setStatusCode(HttpStatus.BAD_REQUEST);
			return response.setComplete();
		}
		
//		response.beforeCommit(() -> {
//			
//			if (!isTokenRenewalRequired /* || exchange.getResponse().isCommitted() */) {
//				return Mono.empty();
//			}
//			
//			Instant issuedAt = Instant.now();
//			
//			try {
//				issuedAt = Jwts.parser()
//						.setSigningKey(jwtPublicKey)
//						.parseClaimsJws(jwtToken)
//						.getBody()
//						.getIssuedAt()
//						.toInstant();
//			} catch (ExpiredJwtException ee) {
//				return Mono.empty();
//			}
//			
//			long durationElapsed = Duration.between(issuedAt, Instant.now()).toMillis();
//			if (durationElapsed > renewalThreshold) {
//				WebClient.builder()
//				.build()
//				.put()
//				.uri(builder -> {
//					return builder
//							.scheme("http")
//							.host(gatewayHost)
//							.port(gatewayPort)
//							.path("/user/extendTokenValidity")
//							.build();
//				})
//				.header(AUTHORIZATION, jwtToken)
//				.contentType(MediaType.TEXT_PLAIN)
//				.bodyValue(jwtToken)
//				.retrieve()
//				.bodyToMono(String.class)
//				.subscribe(newJwtToken -> {
//					response.getHeaders().set(AUTHORIZATION, newJwtToken);
//				});
//			}
//			return Mono.empty();
//		});
		
		return chain
				.filter(exchange)
				//Post Filter Logic
				.then(Mono.fromRunnable(() -> {}))
				;
	}
	
}
