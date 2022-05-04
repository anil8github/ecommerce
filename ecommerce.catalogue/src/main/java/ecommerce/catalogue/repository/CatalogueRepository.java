package ecommerce.catalogue.repository;

import java.util.UUID;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import ecommerce.catalogue.controller.Product;
import reactor.core.publisher.Mono;

public interface CatalogueRepository extends ReactiveCrudRepository<Product, String>, CustomCatalogueRepository {
	
	@Query("select UUID() from dummy")
	public Mono<UUID> generateUUID();

}
