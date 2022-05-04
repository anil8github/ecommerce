package ecommerce.user.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import ecommerce.user.dto.User;
import ecommerce.user.repository.CustomUserRepository;
import reactor.core.publisher.Mono;

public class CustomUserRepositoryImpl implements CustomUserRepository {
	
	private final ReactiveMongoTemplate template;
	
	@Autowired
	public CustomUserRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
		this.template = reactiveMongoTemplate;
	}

	@Override
	public Mono<User> findUserByUserName(String userName) {
		return template.query(User.class)
		.matching(query(
					where("userName").is(userName)
					)
				)
		.first();
	}

}
