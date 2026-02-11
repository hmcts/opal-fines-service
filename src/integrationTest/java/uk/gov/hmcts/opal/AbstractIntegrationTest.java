package uk.gov.hmcts.opal;

import static uk.gov.hmcts.opal.TestContainerConfig.POSTGRES_CONTAINER;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("integration")
@ContextConfiguration(classes = {TestContainerConfig.class})
@AutoConfigureMockMvc
public class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // Dynamically register properties to configure the datasource
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);

        registry.add("legacy-gateway.url", () -> isPortAvailable(4553)
            ? "https://opal-legacy-db-stub.staging.platform.hmcts.net/opal"
            : "http://localhost:4553/opal"
        );
    }

    public static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;  // we successfully bound it
        } catch (IOException e) {
            return false; // something else is using it
        }
    }
}
