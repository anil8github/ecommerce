package ecommerce.gateway.configuration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class GatewayConfiguration {
	
	@Bean(name = "jwtPublicKey")
	public PublicKey retrieveJWTPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, URISyntaxException {
		URI privateKeyURI = new ClassPathResource("/jwt_public_key.txt").getFile().toURI();
		
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
