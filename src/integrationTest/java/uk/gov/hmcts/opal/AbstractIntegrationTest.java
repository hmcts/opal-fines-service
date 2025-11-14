package uk.gov.hmcts.opal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.TestContainerConfig.POSTGRES_CONTAINER;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@SpringBootTest
@ActiveProfiles("integration")
@ContextConfiguration(classes = {TestContainerConfig.class})
@AutoConfigureMockMvc
public class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected UserState userState;
    @MockitoBean
    protected UserStateService userStateService;

    @MockitoSpyBean
    protected JsonSchemaValidationService jsonSchemaValidationService;


    // Dynamically register properties to configure the datasource
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    }


    protected String getBearerTokenWithAllPermissions() {
        String authToken = "Bearer test-token";
        when(userState.anyBusinessUnitUserHasPermission(any())).thenReturn(true);
        when(userStateService.checkForAuthorisedUser(authToken)).thenReturn(allPermissionsUser());
        return authToken;
    }

    protected void validateJsonSchema(String body, String schemaLocation) {
        jsonSchemaValidationService.validateOrError(body, schemaLocation);
    }
}
