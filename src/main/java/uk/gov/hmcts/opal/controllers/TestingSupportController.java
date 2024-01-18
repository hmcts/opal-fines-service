package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.JwtService;

@RestController
@RequestMapping("/api/testing-support")
@RequiredArgsConstructor
@Tag(name = "Testing Support Controller")
public class TestingSupportController {

    private final DynamicConfigService dynamicConfigService;
    private final FeatureToggleService featureToggleService;
    private final JwtService jwtService;

    @GetMapping("/app-mode")
    @Operation(summary = "Retrieves the value for app mode.")
    public ResponseEntity<AppMode> getAppMode() {
        return ResponseEntity.ok(dynamicConfigService.getAppMode());
    }

    @PutMapping("/app-mode")
    @Operation(summary = "Updates the value for app mode.")
    public ResponseEntity<AppMode> updateMode(@RequestBody AppMode mode) {
        return ResponseEntity.accepted().body(this.dynamicConfigService.updateAppMode(mode));
    }

    @GetMapping("/launchdarkly/bool/{featureKey}")
    public ResponseEntity<Boolean> isFeatureEnabled(@PathVariable String featureKey) {
        return ResponseEntity.ok(this.featureToggleService.isFeatureEnabled(featureKey));
    }

    @GetMapping("/launchdarkly/string/{featureKey}")
    public ResponseEntity<String> getFeatureValue(@PathVariable String featureKey) {
        return ResponseEntity.ok(this.featureToggleService.getFeatureValue(featureKey));
    }

    @PostMapping("/handle-oauth-code")
    @Operation(summary = "Generates dummy JWT token for tests on PRs")
    public SecurityToken handleOauthCode(@RequestParam(value = "code", required = false) String code) {
        String userName = "opal-test";
        String accessToken = this.jwtService.generateJwtToken(userName);
        var securityTokenBuilder = SecurityToken.builder()
            .accessToken(accessToken);

        return securityTokenBuilder.build();
    }

}
