package uk.gov.hmcts.opal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static java.lang.reflect.Array.get;
import static uk.gov.hmcts.opal.TestContainerConfig.POSTGRES_CONTAINER;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.client.dto.BusinessUnitUserDto;
import uk.gov.hmcts.opal.common.user.authorisation.client.dto.PermissionDto;
import uk.gov.hmcts.opal.common.user.authorisation.client.dto.UserStateDto;
import uk.hmcts.zephyr.automation.junit5.extension.ZephyrAutomationExtension;

@SpringBootTest
@ActiveProfiles("integration")
@ContextConfiguration(classes = {TestContainerConfig.class})
@AutoConfigureMockMvc
@Import(IntegrationSecurityConfiguration.class)
@ExtendWith(ZephyrAutomationExtension.class)
@SuppressWarnings({"java:S6813", "SpringJavaInjectionPointsAutowiringInspection"})

@EnableWireMock({
    @ConfigureWireMock(
        name = "user-service",
        port = 3555,
        baseUrlProperties = "user.service.url"
    )
})


public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // Limit JdbcTemplate use to narrow test setup or persistence-side-effect checks.
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    // Dynamically register properties to configure the datasource
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("legacy-gateway.url", TestContainerConfig::legacyGatewayUrl);
    }
    public void clearWireMock(){
        WireMock.reset();
    }

    @SneakyThrows
    public void setupUserStateClient(UserStateDto userStateDto) {
        WireMock.stubFor(get("/users/0/state")
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(userStateDto))));
    }

    public UserStateDto getUserStateDtoWithAllPermissions() {
        List<PermissionDto> permissionDtos = Arrays.stream(FinesPermission.values())
            .map(finesPermission -> new PermissionDto(finesPermission.getId(), finesPermission.getDescription()))
            .toList();

        return UserStateDto.builder()
            .userId(1L)
            .username("some-name")
            .businessUnitUsers(List.of(
                new BusinessUnitUserDto("BU1", (short) 1, permissionDtos)
            ))
            .build();
    }
    public UserStateDto getUserStateDtoWithNoPermissions() {
        return UserStateDto.builder()
            .userId(1L)
            .username("some-name")
            .businessUnitUsers(List.of(
                new BusinessUnitUserDto("BU1", (short) 1, List.of())
            ))
            .build();
    }

}
