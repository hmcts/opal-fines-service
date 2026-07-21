package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.mapper.UserStateMapper;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.opal.DefendantAccountDeletionService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService;

@RestController
@RequestMapping("/testing-support")
@RequiredArgsConstructor
@Slf4j(topic = "opal.TestingSupportController")
@Tag(name = "Testing Support Controller")
@ConditionalOnProperty(prefix = "opal.testing-support-endpoints", name = "enabled", havingValue = "true")
@SuppressWarnings("java:S1874")
public class TestingSupportController {
    private static final long CURRENT_USER_ID = 0L;

    private final DynamicConfigService dynamicConfigService;
    private final FeatureToggleApi featureToggleApi;
    private final AccessTokenService accessTokenService;
    private final DefendantAccountDeletionService defendantAccountDeletionService;
    private final InterfaceJobService interfaceJobService;
    private final UserStateClientService userStateClientService;
    private final UserStateMapper userStateMapper;

    @GetMapping("/is-legacy-mode")
    @Operation(summary = "Retrieves whether legacy mode is enabled.")
    public ResponseEntity<Boolean> isLegacyMode() {
        return ResponseEntity.ok(dynamicConfigService.isLegacyMode());
    }

    @GetMapping("/launchdarkly/bool/{featureKey}")
    public ResponseEntity<Boolean> isFeatureEnabled(@PathVariable String featureKey) {
        return ResponseEntity.ok(this.featureToggleApi.isFeatureEnabled(featureKey));
    }

    @GetMapping("/launchdarkly/string/{featureKey}")
    public ResponseEntity<String> getFeatureValue(@PathVariable String featureKey) {
        return ResponseEntity.ok(this.featureToggleApi.getFeatureValue(featureKey, ""));
    }

    @GetMapping("/token/parse")
    public ResponseEntity<String> parseToken(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(this.accessTokenService.extractPreferredUsername(authorization));
    }

    @GetMapping("/user-client/{userId}")
    @Operation(summary = "Retrieves user state via User Service client")
    public ResponseEntity<UserState> getUserState(@PathVariable Long userId) {
        if (!Long.valueOf(CURRENT_USER_ID).equals(userId)) {
            return ResponseEntity.notFound().build();
        }

        return userStateClientService.getUserStateByAuthenticatedUser()
            .map(userStateV2 -> userStateMapper.toUserState(userStateV2, Domain.FINES))
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/defendant-accounts/{defendantAccountId}")
    @Operation(summary = "Deletes a defendant account and ALL associated data. FOR TESTING ONLY!")
    public ResponseEntity<Void> deleteDefendantAccountWithAllData(@PathVariable Long defendantAccountId) {
        log.warn("TEST ENDPOINT: Request to delete defendant account {} and all associated data", defendantAccountId);

        defendantAccountDeletionService.deleteDefendantAccountAndAssociatedData(defendantAccountId);

        return ResponseEntity.noContent().build();
    }

    @FeatureToggle(
        feature = RELEASE_1C_PAYMENT,
        defaultValueProperty = RELEASE_1C_PAYMENT_ENABLED_PROPERTY
    )
    @DeleteMapping("/interface-jobs")
    @Operation(summary = "Deletes a list of Interface jobs. FOR TESTING ONLY!")
    public ResponseEntity<Void> deleteInterfaceJobs(@RequestParam("ids") List<Long> interfaceJobIds) {
        log.warn("TEST ENDPOINT: Request to delete interface jobs with ids: {}", interfaceJobIds);

        interfaceJobService.deleteInterfaceJobs(interfaceJobIds);

        return ResponseEntity.noContent().build();
    }
}
