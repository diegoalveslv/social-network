package com.company.SocialNetwork;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfigurationPostgres.class)
@SpringBootTest
class SocialNetworkApplicationTests {

	@Test
	void contextLoads() {
	}

}
