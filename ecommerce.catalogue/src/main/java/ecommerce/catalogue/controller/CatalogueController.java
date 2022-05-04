package ecommerce.catalogue.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecommerce.catalogue.controller.Product.Builder;
import ecommerce.catalogue.service.CatalogueService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.publisher.Sinks.Many;

@RestController
public class CatalogueController {
	
	@Autowired
	private CatalogueService catalogueService;
	
	@Autowired
	private SolrClient solrClient;
	
	@GetMapping(value = "/catalogue")
	public Flux<CatalogueItem> searchCatalogue(@RequestParam(name = "filter") String raw) {
		
		Criteria criteria;
		try {
			criteria = new ObjectMapper().readValue(raw, Criteria.class);
			SolrQuery solrQuery = new SolrQuery();
			
			solrQuery.setQuery(criteria.getField() + ":" + criteria.getInput().getPattern() + "~" + criteria.getInput().getFuzzyLevel());
			solrQuery.set("wt","json");
			
			
			return Flux.create((FluxSink<CatalogueItem> sink) -> {
				QueryResponse res;
				try {
					res = solrClient.query("product_core", solrQuery, METHOD.GET);
					List<CatalogueItem> beans = res.getBeans(CatalogueItem.class);
					beans.forEach(sink::next);
					sink.complete();
				} catch (SolrServerException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (JsonProcessingException e1) {
			return Flux.error(e1);
		}
	}
	
	@GetMapping(value = "/catalogue/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<CatalogueItem>> searchCatalogueStream(@RequestBody Criteria criteria) {
		
		SolrQuery solrQuery = new SolrQuery();
		
		solrQuery.setQuery(criteria.getField() + ":" + criteria.getInput().getPattern() + "~" + criteria.getInput().getFuzzyLevel());
		solrQuery.set("wt","json");
		
		QueryResponse res;
		Many<ServerSentEvent<CatalogueItem>> sink = Sinks.many().multicast().onBackpressureBuffer();
		try {
			res = solrClient.query("product_core", solrQuery, METHOD.GET);
			List<CatalogueItem> beans = res.getBeans(CatalogueItem.class);
			
			beans.stream()
			.forEach(bean -> {
				ServerSentEvent<CatalogueItem> sse = ServerSentEvent
						.builder(bean)
						.event(bean.getCategory())
						.id(bean.getProductId())
						.build();
				EmitResult tryEmitNext = sink.tryEmitNext(sse);
				if (tryEmitNext.isFailure()) {
					System.out.println("Error emiting");
				}
			});
			sink.emitComplete(null);
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
		
		return sink.asFlux();
	}
	
	@PutMapping(value = "/catalogue")
	public Mono<String> createProduct(@RequestBody Product product) {
		return catalogueService.generateUUID()
		.map(uuid -> {
			Builder builder = new Product.Builder()
					.setProductId(uuid.toString())
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
			
			return builder.build();
		})
		.flatMap(prdt -> catalogueService.saveProduct(prdt));
	}
	
	@PostMapping(value = "/catalogue/{productId}")
	public Mono<Void> modifyProduct(@PathVariable("productId") String productId, @RequestBody Map<String, Object> modification) {
		return catalogueService.modifyProduct(productId, modification);
	}

}
