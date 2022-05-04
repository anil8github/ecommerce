package ecommerce.user.configuration;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import ecommerce.user.controller.Credential;
import ecommerce.user.controller.UserController;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

@Configuration
public class HttpServerConfiguration {
	
	@Value("${server.host}")
	private String host;
	
	@Value("${server.port}")
	private int port;
	
	@Autowired
	private UserController userController;
	
	@Bean
	public void httpServer() {
		RouterFunction<ServerResponse> router = RouterFunctions.route()
				.POST("/user/login", RequestPredicates.contentType(MediaType.APPLICATION_JSON), 
						req -> {
							return req.bodyToMono(Credential.class)
									.flatMap(credentail -> {
											return userController.login(credentail)
														.flatMap(jwtToken -> {
															return ServerResponse.ok()
																	.header("Authorization", jwtToken)
																	.build();
														})
														.onErrorResume(error -> {
															if (error instanceof ResponseStatusException) {
																return ServerResponse
																		.status(((ResponseStatusException) error).getStatus())
																		.bodyValue(((ResponseStatusException) error).getReason());
															}
															return ServerResponse.badRequest().bodyValue(error.getMessage());
														});
									});
						})
				.PUT("/user/extendTokenValidity", RequestPredicates.contentType(MediaType.TEXT_PLAIN),
						req -> {
							return req.bodyToMono(String.class)
								.flatMap(jwtToken -> {
									return userController.extendValidity(jwtToken)
											.flatMap(newJwtToken -> {
												return ServerResponse.ok().bodyValue(newJwtToken);
											});
								});
						})
				.build();
		
		HttpHandler handler = RouterFunctions.toHttpHandler(router);;
		ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);	
	
		HttpServer.create()
			.host(host)
			.port(port)
			.handle(adapter)
			.bind()
			.block();
	}
	
}

class LoginSubscriber implements Subscriber<String>{
	
	private Mono<ServerResponse> responseMono;

	@Override
	public void onSubscribe(Subscription s) {
		
	}

	@Override
	public void onNext(String jwtToken) {
		this.responseMono = ServerResponse.ok()
				.header("Authorization", jwtToken)
				.build();
	}

	@Override
	public void onError(Throwable throwable) {
		ServerResponse.badRequest()
		.bodyValue(throwable.getMessage());
	}

	@Override
	public void onComplete() {
		
	}
	
	public Mono<ServerResponse> getResponseMono() {
		return responseMono;
	}
}
