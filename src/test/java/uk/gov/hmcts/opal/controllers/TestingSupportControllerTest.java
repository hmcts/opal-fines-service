package uk.gov.hmcts.opal.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.authentication.model.AccessTokenResponse;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authentication.service.AzureDummyTokenService;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.exception.OpalApiException;
import uk.gov.hmcts.opal.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.opal.service.DynamicConfigService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {TestingSupportController.class, DynamicConfigService.class, FeatureToggleService.class})
class TestingSupportControllerTest {

    @Autowired
    private TestingSupportController controller;

    @MockBean
    private DynamicConfigService configService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private AzureDummyTokenService azureDummyTokenService;

    @MockBean
    private AccessTokenService accessTokenService;

    @Test
    void getAppMode() {
        AppMode mode = AppMode.builder().mode("opal").build();
        when(configService.getAppMode()).thenReturn(mode);

        ResponseEntity<AppMode> response = controller.getAppMode();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("opal", response.getBody().getMode());
    }

    @Test
    void updateMode() {
        AppMode mode = AppMode.builder().mode("legacy").build();
        when(configService.updateAppMode(any())).thenReturn(mode);

        ResponseEntity<AppMode> response = controller.updateMode(mode);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("legacy", response.getBody().getMode());

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

    @SneakyThrows
    @Test
    void testHandleOauthCode() {
        String username = "opal-test";
        String token = "abc123";

        when(azureDummyTokenService.generateAzureJwtToken(anyString()))
            .thenReturn(token);

        SecurityToken result = controller.handleOauthCode(username);

        assertEquals(token, result.getAccessToken());
        verify(azureDummyTokenService).generateAzureJwtToken(username);
    }

    @SneakyThrows
    @Test
    void testHandleOauthCode_error() {
        when(azureDummyTokenService.generateAzureJwtToken(anyString()))
            .thenThrow(new RuntimeException("Error!"));

        assertThrows(OpalApiException.class, () -> {
            controller.handleOauthCode(null);
        });

        verify(azureDummyTokenService).generateAzureJwtToken(anyString());
    }

    @Test
    public void getToken_shouldReturnResponse() {
        // Arrange
        AccessTokenResponse expectedResponse = AccessTokenResponse.builder().build();
        when(accessTokenService.getTestUserToken())
            .thenReturn(expectedResponse);

        // Act
        ResponseEntity<AccessTokenResponse> response = controller.getToken();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void getToken_shouldHandleExceptions() {
        // Arrange
        when(accessTokenService.getTestUserToken())
            .thenThrow(new RuntimeException("Error!"));

        // Act and Assert
        assertThrows(
            RuntimeException.class,
            () -> controller.getToken()
        );
    }
}
