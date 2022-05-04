package ecommerce.catalogue.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "product")
public class Product {
	
	@Field(name = "productId")
	private String productId;
	
	private String category;
	
	private String brand;
	
	private Double price;
	
	private Long stock;
	
	private String colour;
	
	private String type;
	
	private Map<String, Object> customFieldValue = new HashMap<>();

	public Product(String productId, String category, String brand, String colour) {
		this.productId = productId;
		this.category = category;
		this.brand = brand;
		this.colour = colour;
	}

	public Product() {
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Long getStock() {
		return stock;
	}

	public void setStock(Long stock) {
		this.stock = stock;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProductId() {
		return productId;
	}

	public String getCategory() {
		return category;
	}

	public String getBrand() {
		return brand;
	}

	public String getColour() {
		return colour;
	}
	
	public void addCustomFieldValue(String field, Object value) {
		customFieldValue.put(field, value);
	}
	
	public Map<String, Object> getCustomFieldValue() {
		return customFieldValue;
	}
	
	public void setCustomFieldValue(Map<String, Object> customFieldValue) {
		this.customFieldValue= customFieldValue;
	}
	
	public static class Builder {
		
		private String productId;
		
		private String category;
		
		private String brand;
		
		private Double price;
		
		private Long stock;
		
		private String colour;
		
		private String type;
		
		private Map<String, Object> customFieldValue = new HashMap<>();

		public Builder setProductId(String productId) {
			this.productId = productId;
			return this;
		}

		public Builder setCategory(String category) {
			this.category = category;
			return this;
		}

		public Builder setBrand(String brand) {
			this.brand = brand;
			return this;
		}

		public Builder setPrice(Double price) {
			this.price = price;
			return this;
		}

		public Builder setStock(Long stock) {
			this.stock = stock;
			return this;
		}

		public Builder setColour(String colour) {
			this.colour = colour;
			return this;
		}

		public Builder setType(String type) {
			this.type = type;
			return this;
		}

		public Builder addCustomFieldValue(String field, Object value) {
			this.customFieldValue.put(field, value);
			return this;
		}
		
		public Product build() {
			Product product = new Product(productId, category, brand, colour);
			product.setType(type);
			product.setPrice(price);
			product.setStock(stock);
			product.setCustomFieldValue(customFieldValue);
			return product;
		}
	}
	
}
