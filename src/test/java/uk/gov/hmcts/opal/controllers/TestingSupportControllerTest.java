package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.TestService;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.service.opal.DefendantAccountDeletionService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(
    classes =
        {
            TestingSupportController.class,
            DynamicConfigService.class,
            FeatureToggleApi.class,
            DefendantAccountDeletionService.class
        },
    properties = {
        "opal.testing-support-endpoints.enabled=true"
    }
)
@Isolated
class TestingSupportControllerTest {

    @Autowired
    private TestingSupportController controller;

    @MockitoBean
    private DynamicConfigService configService;

    @MockitoBean
    private FeatureToggleApi featureToggleApi;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean DefendantAccountDeletionService defendantAccountDeletionService;

    @MockitoBean
    private UserStateClientService userStateClientService;

    @MockitoBean
    private TestService testService;

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
        when(featureToggleApi.isFeatureEnabled("my-feature")).thenReturn(true);

        ResponseEntity<Boolean> response = controller.isFeatureEnabled("my-feature");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    void getFeatureFlagValue() {
        when(featureToggleApi.getFeatureValue("my-feature", "")).thenReturn("value");

        ResponseEntity<String> response = controller.getFeatureValue("my-feature");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("value", response.getBody());
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
