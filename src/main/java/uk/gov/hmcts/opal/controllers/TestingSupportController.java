package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.mapper.UserStateMapper;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity;
import uk.gov.hmcts.opal.service.DraftAccountService;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;
import uk.gov.hmcts.opal.service.opal.DefendantAccountDeletionService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;
import uk.gov.hmcts.opal.service.opal.MajorCreditorService;
import uk.gov.hmcts.opal.service.opal.OpalCreditorAccountService;

/**
 * Endpoints used for testing purposes. I've moved single endpoints from other controller here (and kept all paths the
 * same) where the methods were annotated with ConditionalOnProperty. This annotation unfortunately only works at class
 * level. I have also included the controllers where they came from.
 */
@RestController
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
    private final MajorCreditorService majorCreditorService;
    private final BusinessUnitService businessUnitService;
    private final OpalCreditorAccountService opalCreditorAccountService;
    private final DraftAccountService draftAccountService;
    private final LocalJusticeAreaService opalLocalJusticeAreaService;

    @GetMapping("/testing-support/is-legacy-mode")
    @Operation(summary = "Retrieves whether legacy mode is enabled.")
    public ResponseEntity<Boolean> isLegacyMode() {
        return ResponseEntity.ok(dynamicConfigService.isLegacyMode());
    }

    @GetMapping("/testing-support/launchdarkly/bool/{featureKey}")
    public ResponseEntity<Boolean> isFeatureEnabled(@PathVariable String featureKey) {
        return ResponseEntity.ok(this.featureToggleApi.isFeatureEnabled(featureKey));
    }

    @GetMapping("/testing-support/launchdarkly/string/{featureKey}")
    public ResponseEntity<String> getFeatureValue(@PathVariable String featureKey) {
        return ResponseEntity.ok(this.featureToggleApi.getFeatureValue(featureKey, ""));
    }

    @GetMapping("/testing-support/token/parse")
    public ResponseEntity<String> parseToken(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(this.accessTokenService.extractPreferredUsername(authorization));
    }

    @GetMapping("/testing-support/user-client/{userId}")
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

    @DeleteMapping("/testing-support/defendant-accounts/{defendantAccountId}")
    @Operation(summary = "Deletes a defendant account and ALL associated data. FOR TESTING ONLY!")
    public ResponseEntity<Void> deleteDefendantAccountWithAllData(@PathVariable Long defendantAccountId) {
        log.warn("TEST ENDPOINT: Request to delete defendant account {} and all associated data", defendantAccountId);

        defendantAccountDeletionService.deleteDefendantAccountAndAssociatedData(defendantAccountId);

        return ResponseEntity.noContent().build();
    }

    @Hidden
    @FeatureToggle(
        feature = RELEASE_1C_PAYMENT,
        defaultValueProperty = RELEASE_1C_PAYMENT_ENABLED_PROPERTY
    )
    @DeleteMapping("/testing-support/interface-jobs")
    @Operation(summary = "Deletes a list of Interface jobs. FOR TESTING ONLY!")
    public ResponseEntity<Void> deleteInterfaceJobs(
            @RequestParam(value = "ids") List<Long> interfaceJobIds) {
        log.warn("TEST ENDPOINT: Request to delete interface jobs with ids: {}", interfaceJobIds);

        interfaceJobService.deleteInterfaceJobs(interfaceJobIds);

        return ResponseEntity.ok().build();
    }

    /**
     * From {@link MajorCreditorController}.
     * @param criteria search criteria
     * @return list of MajorCreditorEntities
     */
    @PostMapping(value = "/major-creditors/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches MajorCreditors based upon criteria in request body")
    public ResponseEntity<List<MajorCreditorEntity>> postMajorCreditorsSearch(
        @RequestBody MajorCreditorSearchDto criteria) {
        log.debug(":POST:postMajorCreditorsSearch: query: \n{}", criteria);

        List<MajorCreditorEntity> response = majorCreditorService.searchMajorCreditors(criteria);

        return buildResponse(response);
    }

    /**
     * From {@link BusinessUnitController}.
     * @param criteria search criteria
     * @return a list of BusinessUnitEntities
     */
    @PostMapping(value = "/business-units/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches BusinessUnits based upon criteria in request body")
    public ResponseEntity<List<BusinessUnitEntity>> postBusinessUnitsSearch(
        @RequestBody BusinessUnitSearchDto criteria) {
        log.debug(":POST:postBusinessUnitsSearch: query: \n{}", criteria);

        List<BusinessUnitEntity> response = businessUnitService.searchBusinessUnits(criteria);

        return buildResponse(response);
    }

    /**
     * From {@link MinorCreditorApiController}.
     * @param minorCreditorId path param
     * @param ifMatch header
     * @param ignoreMissing query param
     * @return json string stating what was deleted
     */
    @Hidden
    @DeleteMapping(value = "/minor-creditor-accounts/{minorCreditorId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Deletes the Minor Creditor for the given minorCreditorId.")
    public ResponseEntity<String> deleteMinorCreditorById(
        @PathVariable Long minorCreditorId,
        @RequestHeader(value = "If-Match") String ifMatch,
        @RequestParam("ignore_missing") Optional<Boolean> ignoreMissing) {
        log.warn("TEST ENDPOINT: Request to delete creditor account {} and all associated data", minorCreditorId);

        // Note: This endpoint is used for testing only, so the 'If-Match' check is not actually used.
        boolean checkExisted = !(ignoreMissing.orElse(false));
        log.debug(":DELETE:deleteMinorCreditorById: Delete Draft Account: {}{}", minorCreditorId,
            checkExisted ? "" : ", ignore if missing");

        return buildResponse(opalCreditorAccountService
            .deleteCreditorAccount((minorCreditorId), checkExisted));
    }

    /**
     * From {@link DraftAccountController}.
     * @param criteria search criteria
     * @return a list of DraftAccountResponseDtos
     */
    @PostMapping(value = "/draft-accounts/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Draft Accounts based upon criteria in request body")
    public ResponseEntity<List<DraftAccountResponseDto>> postDraftAccountsSearch(
        @RequestBody DraftAccountSearchDto criteria) {

        log.debug(":POST:postDraftAccountsSearch: query: \n{}", criteria);

        return buildResponse(draftAccountService.searchDraftAccounts(criteria));
    }

    /**
     * From {@link DraftAccountController}.
     * @param draftAccountId id path param
     * @param ifMatch header (not used)
     * @param ignoreMissing query param
     * @return json string stating what was deleted
     */
    @Hidden
    @DeleteMapping(value = "/draft-accounts/{draftAccountId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Deletes the Draft Account for the given draftAccountId.")
    public ResponseEntity<String> deleteDraftAccountById(
        @PathVariable Long draftAccountId,
        @RequestHeader(value = "If-Match", required = false) String ifMatch,
        @RequestParam("ignore_missing") Optional<Boolean> ignoreMissing) {

        // Note: This endpoint is used for testing only, so the 'If-Match' check is not actually used.
        boolean checkExisted = !(ignoreMissing.orElse(false));

        log.debug(":DELETE:deleteDraftAccountById: Delete Draft Account: {}{}", draftAccountId,
            checkExisted ? "" : ", ignore if missing");

        return buildResponse(draftAccountService.deleteDraftAccount((draftAccountId), checkExisted));

    }

    /**
     * From {@link LocalJusticeAreaController}.
     * @param criteria search criteria
     * @return a list of LocalJusticeAreaEntities
     */
    @PostMapping(value = "/local-justice-areas/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches LocalJusticeAreas based upon criteria in request body")
    public ResponseEntity<List<LocalJusticeAreaEntity>> postLocalJusticeAreasSearch(
        @RequestBody LocalJusticeAreaSearchDto criteria) {
        log.debug(":POST:postLocalJusticeAreasSearch: query: \n{}", criteria);

        List<LocalJusticeAreaEntity> response = opalLocalJusticeAreaService.searchLocalJusticeAreas(criteria);

        return buildResponse(response);
    }
}
