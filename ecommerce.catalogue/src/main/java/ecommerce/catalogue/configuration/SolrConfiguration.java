package ecommerce.catalogue.configuration;

import java.util.List;
import java.util.Optional;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrConfiguration {
	
	@Autowired
	private List<String> zkNodes;

	@Bean(name = "solrClient")
	public SolrClient cloudSolrClient() {
		zkNodes.stream()
		.reduce("", (accumulated, current) -> accumulated.concat(current));
		
		CloudSolrClient.Builder builder = new CloudSolrClient.Builder(zkNodes, Optional.empty());

		return builder.build();
	}
	
}
