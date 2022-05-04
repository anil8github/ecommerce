package ecommerce.user.controller;

import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@FunctionalInterface
public interface UserFunction<T extends ServerResponse> extends HandlerFunction<T> {
	
}
