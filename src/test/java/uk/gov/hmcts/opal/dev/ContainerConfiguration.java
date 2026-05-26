package uk.gov.hmcts.opal.dev;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfiguration {

    private static final String DEFAULT_POSTGRES_IMAGE = "postgres:17.5";
    private static final String POSTGRES_IMAGE =
        System.getenv().getOrDefault("OPAL_POSTGRES_IMAGE", DEFAULT_POSTGRES_IMAGE);
    private static final int DB_HOST_PORT = 5432;
    private static final int DB_EXPOSED_PORT = 5432;
    private static final PortBinding DB_PORT_BINDING =
        new PortBinding(Ports.Binding.bindPort(DB_HOST_PORT), new ExposedPort(DB_EXPOSED_PORT));
    private static final int REDIS_PORT = 6379;

    @Bean
    @ServiceConnection
    @RestartScope
    PostgreSQLContainer databaseContainer() {
        return new PostgreSQLContainer(POSTGRES_IMAGE)
            .withCreateContainerCmdModifier(cmd ->
                cmd.withHostConfig(new HostConfig().withPortBindings(DB_PORT_BINDING)))
            .withExposedPorts(DB_EXPOSED_PORT)
            .withDatabaseName("opal-fines-db")
            .withUsername("opal-db-user")
            .withPassword("opal-db-password")
            .withReuse(true);
    }

    @Bean
    @ServiceConnection
    @RestartScope
    RedisContainer redisContainer() {
        return new RedisContainer(DockerImageName.parse("redis:6.2.6"))
            .withExposedPorts(6379)
            .withCreateContainerCmdModifier(cmd -> {
                cmd.withName("testcontainers-redis");
                cmd.withHostConfig(
                    new HostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(REDIS_PORT), new ExposedPort(REDIS_PORT))
                    )
                );
            });
    }
}
