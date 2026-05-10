package umc.teumteum.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import umc.teumteum.server.support.RedisTestContainerSupport;

@SpringBootTest
@ActiveProfiles("test")
class ServerApplicationTests extends RedisTestContainerSupport {

	@Test
	void contextLoads() {
	}

}
