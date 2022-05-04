package ecommerce.user.controller;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.user.dto.User;
import ecommerce.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/user")
public class UserController {
	
	@Autowired
	private PrivateKey privateKey;
	
	@Autowired
	private PublicKey publicKey;
	
	@Autowired
	private UserService userService;
	
	@Value("${jwt.ttl}")
	private long jwtTTL;
	
	@PostMapping(value = "/login")
    public Mono<String> login(@RequestBody Credential credential) {
		Mono<User> monoUser = userService.findUserByUserName(credential.getUserName());
				
				return monoUser
				.flatMap(
					user -> {
					
						if (!user.getPassword().equals(credential.getPassword())) {
							return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password"));
						}
						
						ObjectMapper om = new ObjectMapper();
						String userId = user.getUserId();
						Instant now = Instant.now();
						String jwtToken = null;
						try {
							jwtToken = Jwts.builder()
									.claim("userName", user.getUserName())
									.claim("roles", om.writeValueAsString(user.getRoles()))
									.setSubject(userId)
									.setId(UUID.randomUUID().toString())
									.setIssuedAt(Date.from(now))
									.setExpiration(Date.from(now.plus(jwtTTL, ChronoUnit.MILLIS)))
									.signWith(SignatureAlgorithm.RS256, privateKey)
									.compact();
						} catch (IOException e) {
							e.printStackTrace();
						}
						return Mono.just(jwtToken);
					}	
				)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid user")));
	}
	
	@PutMapping(value = "/extendTokenValidity")
    public Mono<String> extendValidity(@RequestBody String jwtToken) {
		JwtBuilder builder = Jwts.builder();
		
		Claims body = null;
		try {
			body = Jwts.parser()
					.setSigningKey(publicKey)
					.parseClaimsJws(jwtToken)
					.getBody();
		} catch (ExpiredJwtException ee) {
			return Mono.empty();
		}
		
		body
		.entrySet()
		.stream()
		.forEach(entry -> {
			builder.claim(entry.getKey(), entry.getValue());
		});
		
		Instant now = Instant.now();
		builder.setSubject(body.getSubject())
		.setId(body.getId())
		.setSubject(body.getSubject())
		.setIssuedAt(Date.from(now))
		.setExpiration(Date.from(now.plus(jwtTTL, ChronoUnit.MILLIS)))
		.signWith(SignatureAlgorithm.RS256, privateKey);
		
		return Mono.just(builder.compact());
					
	}

}
