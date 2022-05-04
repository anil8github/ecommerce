package ecommerce.user.repository;

import ecommerce.user.dto.User;
import reactor.core.publisher.Mono;

public interface CustomUserRepository {
	Mono<User> findUserByUserName(String userName);
}
