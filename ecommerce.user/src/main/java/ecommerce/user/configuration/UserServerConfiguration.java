package ecommerce.user.configuration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

@Configuration
public class UserServerConfiguration {
	
	@Value("${mongodb.host}")
	private String mongoDBHost;
	
	@Value("${mongodb.port}")
	private int mongoDBPort;
	
	@Value("${mongodb.databaseName}")
	private String mongoDBDataBaseName;
	
	@Bean(name = "reactiveMongoTemplate")
	public ReactiveMongoTemplate mongodbReactiveTemplate() {
		MongoClient mongoClient = MongoClients.create(
	            MongoClientSettings.builder()
	                    .applyToClusterSettings(builder ->
	                            builder.hosts(Arrays.asList(
	                                    new ServerAddress(mongoDBHost, mongoDBPort)
	                                    )))
	                    .build());
		
		return new ReactiveMongoTemplate(mongoClient, mongoDBDataBaseName);
	}

	@Bean(name = "privateKey")
	public PrivateKey retrievePrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, URISyntaxException {
		URI privateKeyURI = new ClassPathResource("/private_key.txt").getFile().toURI();
		
	    String rsaPrivateKey = new String(Files.readAllBytes(Paths.get(privateKeyURI)));
	    rsaPrivateKey = rsaPrivateKey.replace("-----BEGIN PRIVATE KEY-----", "");
	    rsaPrivateKey = rsaPrivateKey.replace("\n", "");
	    rsaPrivateKey = rsaPrivateKey.replace("-----END PRIVATE KEY-----", "");

	    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(rsaPrivateKey));
	    KeyFactory kf = KeyFactory.getInstance("RSA");
	    PrivateKey privKey = kf.generatePrivate(keySpec);
	    return privKey;
	}
	
	@Bean(name = "publicKey")
	public PublicKey retrieveJWTPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, URISyntaxException {
		URI privateKeyURI = new ClassPathResource("/public_key.txt").getFile().toURI();
		
	    String rsaPrivateKey = new String(Files.readAllBytes(Paths.get(privateKeyURI)));
	    rsaPrivateKey = rsaPrivateKey.replace("-----BEGIN PUBLIC KEY-----", "");
	    rsaPrivateKey = rsaPrivateKey.replace("\n", "");
	    rsaPrivateKey = rsaPrivateKey.replace("-----END PUBLIC KEY-----", "");

	    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(rsaPrivateKey));
	    KeyFactory kf = KeyFactory.getInstance("RSA");
	    PublicKey pubKey = kf.generatePublic(keySpec);
	    return pubKey;
	}
}
