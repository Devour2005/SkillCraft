package com.skillcraft;

import org.springframework.boot.SpringApplication;

public class TestSkillCraftApplication {

	public static void main(String[] args) {
		SpringApplication.from(SkillCraftApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
