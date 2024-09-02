package uk.gov.hmcts.opal.exception;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class ApiUnauthorisedExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    private static final int DB_HOST_PORT = 5432;
    private static final int DB_EXPOSED_PORT = 5432;
    private static final PortBinding DB_PORT_BINDING =
        new PortBinding(Ports.Binding.bindPort(DB_HOST_PORT), new ExposedPort(DB_EXPOSED_PORT));

    @Container
    public static PostgreSQLContainer<?> postgresContainer =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withCreateContainerCmdModifier(cmd ->
                                                cmd.withHostConfig(new HostConfig().withPortBindings(DB_PORT_BINDING)))
            .withExposedPorts(DB_EXPOSED_PORT)
            .withDatabaseName("opal-fines-db")
            .withUsername("opal-fines")
            .withPassword("opal-fines")
            .withReuse(true);

    @Test
    void shouldReturn401WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/draft-account/1"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                   {
                       "error": "Unauthorized",
                       "message": "Unauthorized: request could not be authorized"
                   }"""));
    }
}
