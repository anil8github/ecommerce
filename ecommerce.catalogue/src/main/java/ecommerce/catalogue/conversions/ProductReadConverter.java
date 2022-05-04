package ecommerce.catalogue.conversions;

import java.lang.reflect.InvocationTargetException;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import ecommerce.catalogue.controller.Product;
import ecommerce.catalogue.controller.Product.Builder;
import ecommerce.catalogue.service.ProductStandardProperty;

@ReadingConverter
public class ProductReadConverter implements Converter<Document, Product> {
	
	@Override
	public Product convert(Document source) {
		
		Builder builder = new Product.Builder();
		
		source.keySet()
		.stream()
		.map(property -> ProductStandardProperty.parseProperty(property))
		.filter(propertyEnum -> propertyEnum != null)
		.forEach(propertyEnum -> {
			try {
				propertyEnum.setProperty(builder, source.get(propertyEnum.getName()));
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
		});
		
		switch ((String)source.get("category")) {
			case "Automobile":
				builder.addCustomFieldValue("name", (String)source.get("name"));
				builder.addCustomFieldValue("variant", (String)source.get("variant"));
				builder.addCustomFieldValue("make", (Integer)source.get("make"));
				break;
				
			case "Apperal":
				builder.addCustomFieldValue("subtype", (String)source.get("subtype"));
				builder.addCustomFieldValue("size", (String)source.get("size"));
				break;

		default:
			break;
		}
		
		return builder.build();
	}

}
