package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.annotation.JsonSchemaValidated;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.service.DefendantAccountPartyService;
import uk.gov.hmcts.opal.service.DefendantAccountPaymentTermsService;
import uk.gov.hmcts.opal.service.DefendantAccountService;

@RestController
@RequestMapping("/defendant-accounts")
@Slf4j(topic = "opal.DefendantAccountController")
@Tag(name = "Defendant Account Controller")
public class DefendantAccountController {

    private final DefendantAccountService defendantAccountService;
    private final DefendantAccountPaymentTermsService defendantAccountPaymentTermsService;
    private final DefendantAccountPartyService defendantAccountPartyService;

    public DefendantAccountController(DefendantAccountService defendantAccountService,
        DefendantAccountPaymentTermsService defendantAccountPaymentTermsService,
        DefendantAccountPartyService defendantAccountPartyService) {
        this.defendantAccountService = defendantAccountService;
        this.defendantAccountPaymentTermsService = defendantAccountPaymentTermsService;
        this.defendantAccountPartyService = defendantAccountPartyService;
    }

    @GetMapping(value = "/{defendantAccountId}/defendant-account-parties/{defendantAccountPartyId}")
    @Operation(summary = "Get details for a defendant account party")
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetDefendantAccountPartyResponse> getDefendantAccountParty(
        @PathVariable Long defendantAccountId,
        @PathVariable Long defendantAccountPartyId) {

        log.debug(":GET:getDefendantAccountParty: for accountId={}, partyId={}", defendantAccountId,
            defendantAccountPartyId);

        GetDefendantAccountPartyResponse response =
            defendantAccountPartyService.getDefendantAccountParty(defendantAccountId, defendantAccountPartyId);

        return buildResponse(response);
    }

    @PostMapping(value = "/{defendantAccountId}/payment-terms")
    @Operation(summary = "Add Payment Terms to a defendant account")
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetDefendantAccountPaymentTermsResponse> addPaymentTerms(
        @PathVariable Long defendantAccountId,
        @RequestHeader("Business-Unit-Id") String businessUnitId,
        @RequestHeader(value = "If-Match", required = false) String ifMatch,
        @JsonSchemaValidated(schemaPath = SchemaPaths.POST_DEFENDANT_ACCOUNT_ADD_PAYMENT_TERMS)
        @RequestBody AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {

        log.debug(":POST: :addPaymentTerms: for defendant id: {}", defendantAccountId);

        return buildResponse(
            defendantAccountPaymentTermsService.addPaymentTerms(defendantAccountId,
                businessUnitId,
                ifMatch,
                addPaymentTermsRequest));
    }

    @GetMapping(value = "/{defendantAccountId}/payment-terms/latest")
    @Operation(summary = "Get defendant account details by providing the defendant account summary")
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetDefendantAccountPaymentTermsResponse> defendantAccountPaymentTerms(
        @PathVariable Long defendantAccountId) {

        log.debug(":GET:DefendantAccountPaymentTerms: for defendant id: {}", defendantAccountId);

        return buildResponse(
            defendantAccountPaymentTermsService.getPaymentTerms(defendantAccountId));
    }


    @PostMapping(value = "/{defendantAccountId}/defendant-account-parties")
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetDefendantAccountPartyResponse> addDefendantAccountParty(
        @PathVariable Long defendantAccountId,
        @RequestHeader("Business-Unit-Id") String businessUnitId,
        @RequestHeader(value = "If-Match", required = false) String ifMatch,
        @JsonSchemaValidated(schemaPath = SchemaPaths.POST_DEFENDANT_ACCOUNT_ADD_PARTY)
        @RequestBody AddDefendantAccountPartyRequest request) {

        log.debug(
            ":POST:addDefendantAccountParty: for defendant id: {} and defendantAccountPartyId: {}",
            defendantAccountId
        );

        return buildResponse(
            defendantAccountPartyService.addDefendantAccountParty(
                defendantAccountId,
                ifMatch, businessUnitId, request
            ));
    }

    @PutMapping(value = "/{defendantAccountId}/defendant-account-parties/{defendantAccountPartyId}")
    @Operation(summary = "Get defendant account details by providing the defendant account summary")
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetDefendantAccountPartyResponse> replaceDefendantAccountParty(
        @PathVariable Long defendantAccountId,
        @PathVariable Long defendantAccountPartyId,
        @RequestHeader("Business-Unit-Id") String businessUnitId,
        @RequestHeader(value = "If-Match", required = false) String ifMatch,
        @RequestBody DefendantAccountParty request
    ) {

        log.debug(":PUT:replaceDefendantAccountParty: for defendant id: {} and defendantAccountPartyId: {}",
            defendantAccountId, defendantAccountPartyId);

        return buildResponse(
            defendantAccountPartyService.replaceDefendantAccountParty(defendantAccountId,
                defendantAccountPartyId, ifMatch, businessUnitId, request));
    }

}
