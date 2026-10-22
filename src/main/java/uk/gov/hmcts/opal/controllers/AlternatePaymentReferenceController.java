package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.common.launchdarkly.FeatureFlags.RELEASE_1C_BANKING_INTERFACES;
import static uk.gov.hmcts.opal.common.launchdarkly.FeatureFlags.RELEASE_1C_BANKING_INTERFACES_ENABLED_PROPERTY;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.AlternatePaymentReferencesApi;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesItem;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesWriteRequest;
import uk.gov.hmcts.opal.service.AlternatePaymentReferenceService;

@RestController

@RequiredArgsConstructor
@Slf4j(topic = "opal.AlternatePaymentReferenceController")
@Tag(name = "Alternate Payment Reference Controller")
public class AlternatePaymentReferenceController implements AlternatePaymentReferencesApi {

    private final AlternatePaymentReferenceService alternatePaymentReferenceService;

    @Override
    @FeatureToggle(
        feature = RELEASE_1C_BANKING_INTERFACES,
        defaultValueProperty = RELEASE_1C_BANKING_INTERFACES_ENABLED_PROPERTY
    )
    public ResponseEntity<List<AlternatePaymentReferencesItem>> getAlternatePaymentReferences(Long defendantAccountId) {
        return ResponseEntity.ok()
            .body(alternatePaymentReferenceService.getAlternatePaymentReferences(defendantAccountId));
    }

    @Override
    @FeatureToggle(
        feature = RELEASE_1C_BANKING_INTERFACES,
        defaultValueProperty = RELEASE_1C_BANKING_INTERFACES_ENABLED_PROPERTY
    )
    public ResponseEntity<AlternatePaymentReferencesItem> deleteAlternatePaymentReference(
        Long defendantAccountId, Long alternatePaymentReferenceId, String expectedVersion) {

        AlternatePaymentReferencesItem apr = alternatePaymentReferenceService.deleteAlternatePaymentReferences(
            defendantAccountId, alternatePaymentReferenceId, expectedVersion);
        return ResponseEntity.ok()
            .eTag(String.valueOf(apr.getVersion()))
            .body(apr);
    }

    @Override
    @FeatureToggle(
        feature = RELEASE_1C_BANKING_INTERFACES,
        defaultValueProperty = RELEASE_1C_BANKING_INTERFACES_ENABLED_PROPERTY
    )
    public ResponseEntity<AlternatePaymentReferencesItem> createAlternatePaymentReference(
        Long defendantAccountId, String expectedVersion,
        AlternatePaymentReferencesWriteRequest alternatePaymentReferencesWriteRequest) {
        AlternatePaymentReferencesItem apr =
            alternatePaymentReferenceService.createAlternatePaymentReferences(
                defendantAccountId, alternatePaymentReferencesWriteRequest, expectedVersion
            );
        return ResponseEntity.status(HttpStatus.CREATED)
            .eTag(String.valueOf(apr.getVersion()))
            .body(apr);
    }

    @Override
    @FeatureToggle(
        feature = RELEASE_1C_BANKING_INTERFACES,
        defaultValueProperty = RELEASE_1C_BANKING_INTERFACES_ENABLED_PROPERTY
    )
    public ResponseEntity<AlternatePaymentReferencesItem> updateAlternatePaymentReference(
        Long defendantAccountId, Long alternatePaymentReferenceId, String expectedVersion,
        AlternatePaymentReferencesWriteRequest alternatePaymentReferencesUpdateRequest) {
        AlternatePaymentReferencesItem apr =
            alternatePaymentReferenceService.updateAlternatePaymentReferences(
                defendantAccountId, alternatePaymentReferenceId,
                alternatePaymentReferencesUpdateRequest, expectedVersion
            );
        return ResponseEntity.ok()
            .eTag(String.valueOf(apr.getVersion()))
            .body(apr);
    }
}
