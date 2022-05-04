package ecommerce.catalogue.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import ecommerce.catalogue.controller.Product.Builder;

public enum ProductStandardProperty {
	
	PRODUCT_ID("productId"),
	CATEGORY("category"),
	BRAND("brand"),
	PRICE("price"),
	STOCK("stock"),
	TYPE("type"),
	COLOUR("colour");
	
	private static Map<String, ProductStandardProperty> NAME_TYPE_MAP = new HashMap<>();
	
	static {
		for (ProductStandardProperty type : ProductStandardProperty.values()) {
			NAME_TYPE_MAP.put(type.getName(), type);
		}
	}

	private String name;
	
	ProductStandardProperty(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public <T> void setProperty(Builder builder, T value) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		StringBuilder setterMethodName = new StringBuilder()
									.append("set")
									.append(name.substring(0, 1).toUpperCase())
									.append(name.substring(1));
		
		Method declaredMethod = builder.getClass().getDeclaredMethod(setterMethodName.toString(), value.getClass());
		declaredMethod.invoke(builder, value);
	}
	
	public static ProductStandardProperty parseProperty(String name) {
		return NAME_TYPE_MAP.get(name);
	}

}
