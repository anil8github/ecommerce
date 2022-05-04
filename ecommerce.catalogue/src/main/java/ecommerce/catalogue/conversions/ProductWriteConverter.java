package ecommerce.catalogue.conversions;

import java.util.Map;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import ecommerce.catalogue.controller.Product;
import ecommerce.catalogue.service.ProductStandardProperty;

@WritingConverter
public class ProductWriteConverter implements Converter<Product, Document> {

	@Override
	public Document convert(Product product) {
		
		Document dbObject = new Document();
		
		dbObject.put(ProductStandardProperty.PRODUCT_ID.getName(), product.getProductId());
		dbObject.put(ProductStandardProperty.CATEGORY.getName(), product.getCategory());
		dbObject.put(ProductStandardProperty.BRAND.getName(), product.getBrand());
		dbObject.put(ProductStandardProperty.PRICE.getName(), product.getPrice());
		dbObject.put(ProductStandardProperty.STOCK.getName(), product.getStock());
		dbObject.put(ProductStandardProperty.TYPE.getName(), product.getType());
		dbObject.put(ProductStandardProperty.COLOUR.getName(), product.getColour());
		
		
		Map<String, Object> customFieldValue = product.getCustomFieldValue();
		switch (product.getCategory()) {
			case "Automobile":
				dbObject.put("name", (String)customFieldValue.get("name"));
				dbObject.put("variant", (String)customFieldValue.get("variant"));
				dbObject.put("make", (Integer)customFieldValue.get("make"));
				break;
	
			case "Apperal":
				dbObject.put("subtype", (String)customFieldValue.get("subtype"));
				dbObject.put("size", (String)customFieldValue.get("size"));
				break;
				
			default:
				break;
		}
		
		return dbObject;
	}

}
