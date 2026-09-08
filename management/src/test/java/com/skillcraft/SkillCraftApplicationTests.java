package com.skillcraft;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = "jwt.secret=test-only-secret-not-used-outside-the-test-suite-0123456789")
class SkillCraftApplicationTests {

	@Test
	void contextLoads() {
	}

}
