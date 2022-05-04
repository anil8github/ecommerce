package ecommerce.catalogue.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import ecommerce.catalogue.controller.Product;
import reactor.core.publisher.Mono;

public class CustomCatalogueRepositoryImpl implements CustomCatalogueRepository {

	private final ReactiveMongoTemplate template;
	
	@Autowired
	public CustomCatalogueRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate) {
		this.template = reactiveMongoTemplate;
	}
	
	@Override
	public Mono<Product> findProductByProductId(String productId) {
		return template.query(Product.class)
		.matching(query(
					where("productId").is(productId)
					)
				)
		.first();
	}
	
	@Override
	public Mono<Product> deleteProductByProductId(String productId) {
		return template.findAndRemove(query(
										where("productId").is(productId)
									)
									,Product.class);
	}
	
}
