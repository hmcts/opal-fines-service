package uk.gov.hmcts.opal.dev;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfiguration {

    private static final int DB_HOST_PORT = 5432;
    private static final int DB_EXPOSED_PORT = 5432;
    private static final PortBinding DB_PORT_BINDING =
        new PortBinding(Ports.Binding.bindPort(DB_HOST_PORT), new ExposedPort(DB_EXPOSED_PORT));

    @Bean
    @ServiceConnection
    @RestartScope
    PostgreSQLContainer<?> databaseContainer() {
        return new PostgreSQLContainer<>("postgres:17.5")
            .withCreateContainerCmdModifier(cmd ->
                cmd.withHostConfig(new HostConfig().withPortBindings(DB_PORT_BINDING)))
            .withExposedPorts(DB_EXPOSED_PORT)
            .withDatabaseName("opal-fines-db")
            .withUsername("opal-fines")
            .withPassword("opal-fines")
            .withReuse(true);
    }
}
