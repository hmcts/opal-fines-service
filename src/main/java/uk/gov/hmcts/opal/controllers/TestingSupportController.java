package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.authentication.model.AccessTokenResponse;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.service.AuthorisationService;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyTestingSupportService;
import uk.gov.hmcts.opal.service.opal.DefendantAccountDeletionService;

@RestController
@RequestMapping("/testing-support")
@RequiredArgsConstructor
@Slf4j(topic = "opal.TestingSupportController")
@Tag(name = "Testing Support Controller")
@ConditionalOnProperty(prefix = "opal.testing-support-endpoints", name = "enabled", havingValue = "true")
public class TestingSupportController {

    private static final String X_USER_EMAIL = "X-User-Email";

    private final DynamicConfigService dynamicConfigService;
    private final FeatureToggleService featureToggleService;
    private final AccessTokenService accessTokenService;
    private final AuthorisationService authorisationService;
    private final LegacyTestingSupportService legacyTestingSupportService;
    private final DefendantAccountDeletionService defendantAccountDeletionService;

    @GetMapping("/app-mode")
    @Operation(summary = "Retrieves the value for app mode.")
    public ResponseEntity<AppMode> getAppMode() {
        return ResponseEntity.ok(dynamicConfigService.getAppMode());
    }

    @GetMapping("/launchdarkly/bool/{featureKey}")
    public ResponseEntity<Boolean> isFeatureEnabled(@PathVariable String featureKey) {
        return ResponseEntity.ok(this.featureToggleService.isFeatureEnabled(featureKey));
    }

    @GetMapping("/launchdarkly/string/{featureKey}")
    public ResponseEntity<String> getFeatureValue(@PathVariable String featureKey) {
        return ResponseEntity.ok(this.featureToggleService.getFeatureValue(featureKey));
    }

    @GetMapping("/token/test-user")
    @Operation(summary = "Retrieves the token for default test user")
    public ResponseEntity<SecurityToken> getToken() {
        var accessTokenResponse = this.accessTokenService.getTestUserToken();
        var securityToken = authorisationService.getSecurityToken(accessTokenResponse.getAccessToken());
        return ResponseEntity.ok(securityToken);
    }

    @GetMapping("/token/user")
    @Operation(summary = "Retrieves the token for a given user")
    public ResponseEntity<SecurityToken> getTokenForUser(@RequestHeader(value = X_USER_EMAIL) String userEmail) {
        AccessTokenResponse accessTokenResponse = this.accessTokenService.getTestUserToken(userEmail);
        SecurityToken securityToken = authorisationService.getSecurityToken(accessTokenResponse.getAccessToken());
        return ResponseEntity.ok(securityToken);
    }

    @GetMapping("/token/parse")
    public ResponseEntity<String> parseToken(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(this.accessTokenService.extractPreferredUsername(authorization));
    }

    @PostMapping("/legacy/test-function/{functionName}")
    @Operation(summary = "Posts to the legacy gateway for testing purposes.")
    public ResponseEntity<String> legacyTestFunction(@PathVariable String functionName, @RequestBody String body) {
        String response = legacyTestingSupportService.postLegacyFunction(functionName, body).getBody();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/defendant-accounts/{defendantAccountId}")
    @Operation(summary = "Deletes a defendant account and ALL associated data. FOR TESTING ONLY!")
    public ResponseEntity<Void> deleteDefendantAccountWithAllData(@PathVariable Long defendantAccountId) {
        log.warn("TEST ENDPOINT: Request to delete defendant account {} and all associated data", defendantAccountId);

        defendantAccountDeletionService.deleteDefendantAccountAndAssociatedData(defendantAccountId);

        return ResponseEntity.noContent().build();
    }
}
