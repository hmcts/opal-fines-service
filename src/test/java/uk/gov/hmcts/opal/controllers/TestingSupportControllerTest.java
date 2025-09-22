package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.client.UserClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.DefendantAccountDeletionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
    classes =
        {
            TestingSupportController.class,
            DynamicConfigService.class,
            FeatureToggleService.class,
            LegacyGatewayProperties.class,
            RestClient.class,
            DefendantAccountDeletionService.class
        },
    properties = {
        "opal.testing-support-endpoints.enabled=true"
    }
)
class TestingSupportControllerTest {

    private static final String TEST_USER_EMAIL = "test@example.com";
    private static final String TEST_TOKEN = "testToken";

    @Autowired
    private TestingSupportController controller;

    @MockitoBean
    private DynamicConfigService configService;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private AccessTokenService accessTokenService;


    @MockitoBean DefendantAccountDeletionService defendantAccountDeletionService;

    @MockitoBean
    private UserClient userClient;

    @Test
    void getAppMode() {
        AppMode mode = AppMode.builder().mode("opal").build();
        when(configService.getAppMode()).thenReturn(mode);

        ResponseEntity<AppMode> response = controller.getAppMode();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("opal", response.getBody().getMode());
    }

    @Test
    void isFeatureEnabled() {
        when(featureToggleService.isFeatureEnabled("my-feature")).thenReturn(true);

        ResponseEntity<Boolean> response = controller.isFeatureEnabled("my-feature");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    void getFeatureFlagValue() {
        when(featureToggleService.getFeatureValue("my-feature")).thenReturn("value");

        ResponseEntity<String> response = controller.getFeatureValue("my-feature");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("value", response.getBody());
    }

    @Test
    void getToken_shouldReturnResponse() {
        // Arrange
        SecurityToken securityToken = SecurityToken.builder().accessToken(TEST_TOKEN).build();
        when(userClient.getTestUserToken()).thenReturn(securityToken);

        // Call the controller method
        ResponseEntity<SecurityToken> responseEntity = controller.getToken();

        // Verify the response
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(securityToken, responseEntity.getBody());
    }

    @Test
    void getToken_shouldHandleExceptions() {
        // Arrange
        when(userClient.getTestUserToken())
            .thenThrow(new RuntimeException("Error!"));

        // Act and Assert
        assertThrows(
            RuntimeException.class,
            () -> controller.getToken()
        );
    }

    @Test
    void getTokenForUser_shouldReturnResponse() {
        // Arrange
        SecurityToken securityToken = SecurityToken.builder().accessToken(TEST_TOKEN).build();
        when(userClient.getTestUserToken(any())).thenReturn(securityToken);

        // Act
        ResponseEntity<SecurityToken> response = controller.getTokenForUser(TEST_USER_EMAIL);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(securityToken, response.getBody());
    }

    @Test
    void getTokenForUser_shouldHandleExceptions() {
        // Arrange
        when(userClient.getTestUserToken(TEST_USER_EMAIL))
            .thenThrow(new RuntimeException("Error!"));

        // Act and Assert
        assertThrows(
            RuntimeException.class,
            () -> controller.getTokenForUser(TEST_USER_EMAIL)
        );
    }

    @Test
    void parseToken_shouldReturnEmail() {
        String bearerToken = "Bearer token";
        when(accessTokenService.extractPreferredUsername(bearerToken)).thenReturn("my@email.com");

        ResponseEntity<String> response = controller.parseToken(bearerToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("my@email.com", response.getBody());
    }
}
