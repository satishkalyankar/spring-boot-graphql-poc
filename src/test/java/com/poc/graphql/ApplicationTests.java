package com.poc.graphql;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.security.saml2.relyingparty.registration.sso.assertingparty.metadata-uri=classpath:saml/idp-metadata.xml")
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
