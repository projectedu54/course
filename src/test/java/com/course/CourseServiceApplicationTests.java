package com.course;

import com.course.client.CatalogClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class CourseServiceApplicationTests {

	@MockBean
	private CatalogClient catalogClient;

	@Test
	void contextLoads() {
		// The context loads, and catalogClient is mocked
	}
}
