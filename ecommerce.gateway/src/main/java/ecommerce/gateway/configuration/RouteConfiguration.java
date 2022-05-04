package ecommerce.gateway.configuration;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ecommerce.gateway.filter.AuthenticationFilter;
import ecommerce.gateway.filter.RateLimitingFilter;

@Configuration
public class RouteConfiguration {
	
	@Autowired
	private AuthenticationFilter regularAuthFilter;
	
	private AuthenticationFilter nonRenewableAuthFilter;
	
	@Autowired
	private RateLimitingFilter rateLimitingFilter;
	
	@Autowired
	private AutowireCapableBeanFactory beanFactory;
	
	@PostConstruct
	public void init() {
		this.nonRenewableAuthFilter = (AuthenticationFilter) beanFactory.createBean(AuthenticationFilter.class, AutowireCapableBeanFactory.AUTOWIRE_NO, true);
		this.nonRenewableAuthFilter.setTokenRenewalRequired(false);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
	    return builder.routes()
	      .route("inventory-router", r -> r
	    		  					.path("/inventory")
	    		  					.or()
	    		  					.path("/inventories")
	    		  					.or()
	    		  					.path("/inventory/*")
	    		  					.filters(f -> f
	    		  							.filter(regularAuthFilter, 0)
//	    		  							.filter(rateLimitingFilter, 1)
	    		  							)
//	    		  					.uri("lb://ECOMMERCE-INVENTORY")
	    		  					.uri("http://localhost:8089")
	    		  )
	      .route("order-router", r -> r
					.path("/order")
					.filters(f -> f.filter(regularAuthFilter))
					.uri("http://localhost:8088")
	    		  )
	      .route("order-router", r -> r
					.path("/order/events")
//					.filters(f -> f.filter(regularAuthFilter))  //This is necessary as EventSource in javascript doesn't support passing headers
					.uri("http://localhost:8088")
	    		  )
	      .route("user-router", r -> r
					.path("/user/login")
					.uri("http://localhost:8086")
	    		  )
	      .route("user-router", r -> r
					.path("/user/*")
					.filters(f -> f.filter(nonRenewableAuthFilter))
					.uri("http://localhost:8086")
	    		  )
	      .route("catalogue-router", r -> r
					.path("/catalogue")
					.filters(f -> f
  							.filter(regularAuthFilter, 0)
//  							.filter(rateLimitingFilter, 1)
  							)
					.uri("http://localhost:8085")
	    		  )
	    .build();
	}
}
