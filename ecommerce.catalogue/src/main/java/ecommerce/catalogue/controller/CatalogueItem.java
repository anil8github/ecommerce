package ecommerce.catalogue.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.beans.Field;

public class CatalogueItem {
	
	@Field
	private String productId;
	
	@Field
	private String category;
	
	@Field
	private String brand;
	
	@Field
	private Double price;
	
	@Field
	private Long stock;
	
	@Field
	private String colour;
	
	@Field
	private String type;
	
	private Map<String, Object> customFieldValue = new HashMap<>();

	public CatalogueItem(String productId, String category, String brand, String colour) {
		this.productId = productId;
		this.category = category;
		this.brand = brand;
		this.colour = colour;
	}

	public CatalogueItem() {
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
}
