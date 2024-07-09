package com.company.SocialNetwork;

import org.springframework.boot.SpringApplication;

public class TestSocialNetworkApplication {

	public static void main(String[] args) {
		SpringApplication.from(SocialNetworkApplication::main).with(TestcontainersConfigurationPostgres.class).with(TestcontainersConfigurationRedis.class).run(args);
	}

}
