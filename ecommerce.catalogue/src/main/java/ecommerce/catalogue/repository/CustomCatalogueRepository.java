package ecommerce.catalogue.repository;

import ecommerce.catalogue.controller.Product;
import reactor.core.publisher.Mono;

public interface CustomCatalogueRepository {

	Mono<Product> findProductByProductId(String productId);

	Mono<Product> deleteProductByProductId(String productId);

}
