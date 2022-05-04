package ecommerce.catalogue.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ecommerce.catalogue.controller.Product;
import ecommerce.catalogue.controller.Product.Builder;
import ecommerce.catalogue.repository.CatalogueRepository;
import reactor.core.publisher.Mono;

@Service
public class CatalogueService {
	
	@Autowired
	private CatalogueRepository catalogueRepository;
	
	public Mono<String> saveProduct(Product product) {
		return catalogueRepository
				.save(product)
				.map(prdt -> prdt.getProductId());
	}
	
	public Mono<Product> findProduct(String productId) {
		return catalogueRepository
				.findProductByProductId(productId);
	}
	
	public Mono<Product> deleteProduct(String productId) {
		return catalogueRepository
			.deleteProductByProductId(productId);
	}
	
	public Mono<Void> modifyProduct(String productId, Map<String, Object> modification) {

		findProduct(productId)
		.map(product -> {
			
			Builder builder = new Product.Builder()
					.setProductId(product.getProductId())
					.setCategory(product.getCategory())
					.setBrand(product.getBrand())
					.setPrice(product.getPrice())
					.setStock(product.getStock())
					.setType(product.getType())
					.setColour(product.getColour());
			product.getCustomFieldValue().entrySet()
			.stream()
			.forEach(entry -> {
				builder.addCustomFieldValue(entry.getKey(), entry.getValue());
			});
			return builder;
		})
		.map(builder -> {
			
			modification.entrySet().stream()
			
			.forEach(entry -> {
				ProductStandardProperty parseProperty = ProductStandardProperty.parseProperty(entry.getKey());
				
				if (parseProperty != null) {
					try {
						switch (parseProperty) {
						case STOCK:
							parseProperty.setProperty(builder, Long.valueOf(entry.getValue().toString()));
							break;
							
						case PRICE:
							parseProperty.setProperty(builder, Double.valueOf(entry.getValue().toString()));
							break;

						default:
							parseProperty.setProperty(builder, entry.getValue());
							break;
						}
						
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				} else {
					Map<String, Object> map = (Map<String, Object>) entry.getValue();
					
					map.entrySet().stream()
					.forEach(entry1 -> {
						builder.addCustomFieldValue(entry1.getKey(), entry1.getValue());
					});
					
				}
			});
			
			return builder.build();
		})
		.flatMap(product -> {
			return deleteProduct(productId)
			.filter(prdt -> prdt != null)
			.flatMap(prdt -> {
				return saveProduct(product);
			});
		})
		.subscribe();
		
		return Mono.empty();
	}
	
	public Mono<Void> addStockToProduct(String productId, Long deltaStock) {

		findProduct(productId)
		.map(product -> {
			product.setStock(product.getStock() + deltaStock);
			return product;
		})
		.flatMap(product -> {
			return deleteProduct(productId)
			.filter(prdt -> prdt != null)
			.flatMap(prdt -> {
				return saveProduct(product);
			});
		})
		.subscribe();
		
		return Mono.empty();
	}
	
	public Mono<UUID> generateUUID() {
		return Mono.just(UUID.randomUUID());
	}

}
