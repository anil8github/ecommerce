package ecommerce.catalogue.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import ecommerce.catalogue.controller.CatalogueController;
import ecommerce.catalogue.controller.Product;
import reactor.netty.http.server.HttpServer;

@Configuration
public class HttpServerConfiguration {
	
	@Value("${server.host}")
	private String host;
	
	@Value("${server.port}")
	private int port;
	
	@Autowired
	private CatalogueController catalogueController;
	
//	@Bean
	public void httpServer() {
		RouterFunction<ServerResponse> router = RouterFunctions.route()
				.GET("/catalogue", RequestPredicates.contentType(MediaType.APPLICATION_JSON), 
						req -> {
							return req.bodyToMono(String.class)
									.flatMap(criteria -> {
											return ServerResponse.ok()
													.contentType(MediaType.APPLICATION_JSON)
													.body(catalogueController.searchCatalogue(criteria), Product.class)
													.onErrorResume(error -> {
														return ServerResponse
																.status(((ResponseStatusException) error).getStatus())
																.bodyValue(((ResponseStatusException) error).getReason());
													});
									})
									.onErrorResume(error -> {
										return ServerResponse
										.status(((ResponseStatusException) error).getStatus())
										.bodyValue(((ResponseStatusException) error).getReason());
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