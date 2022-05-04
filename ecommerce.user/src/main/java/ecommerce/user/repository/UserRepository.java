package ecommerce.user.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import ecommerce.user.dto.User;

public interface UserRepository extends ReactiveCrudRepository<User, String> , CustomUserRepository {
}
