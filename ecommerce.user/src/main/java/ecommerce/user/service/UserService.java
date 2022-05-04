package ecommerce.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ecommerce.user.dto.User;
import ecommerce.user.repository.UserRepository;
import reactor.core.publisher.Mono;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	public Mono<User> findUserByUserName(String userName) {
		return userRepository.findUserByUserName(userName);
	}
	
}
