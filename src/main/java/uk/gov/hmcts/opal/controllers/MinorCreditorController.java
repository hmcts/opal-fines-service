package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import java.util.Optional;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.service.MinorCreditorService;
import uk.gov.hmcts.opal.service.opal.OpalCreditorAccountService;

@RestController
@RequestMapping("/minor-creditor-accounts")
@Slf4j(topic = "opal.MinorCreditorController")
@Tag(name = "Minor Creditor Controller")
public class MinorCreditorController {

    private final MinorCreditorService minorCreditorService;


    public MinorCreditorController(MinorCreditorService minorCreditorService) {
        this.minorCreditorService = minorCreditorService;
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches MinorCreditors based upon criteria in request body")
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<PostMinorCreditorAccountsSearchResponse> postMinorCreditorsSearch(
        @RequestBody MinorCreditorSearch criteria) {
        log.debug(":POST:postMinorCreditorsSearch: query: \n{}", criteria);

        PostMinorCreditorAccountsSearchResponse response = minorCreditorService
            .searchMinorCreditors(criteria);

        return buildResponse(response);
    }

    @GetMapping(value = "{minorCreditorId}/at-a-glance")
    @Operation(summary = "Get Minor Creditor Account At A Glance")
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetMinorCreditorAccountAtAGlanceResponse> getMinorCreditorsAtAGlance(
        @PathVariable Long minorCreditorId) {
        log.debug(":GET:getMinorCreditorsAtAGlance: query: \n{}", minorCreditorId);

        GetMinorCreditorAccountAtAGlanceResponse response = minorCreditorService
            .getMinorCreditorAtAGlance(minorCreditorId);

        return buildResponse(response);
    }

    @GetMapping(value = "/{minorCreditorId}/header-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Gets Minor Creditor account header summary for the given minorCreditorId")
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetMinorCreditorAccountHeaderSummaryResponse> getMinorCreditorAccountHeaderSummary(
        @PathVariable Long minorCreditorId) {

        log.debug(":GET:getMinorCreditorAccountHeaderSummary: minorCreditorId: {}", minorCreditorId);

        GetMinorCreditorAccountHeaderSummaryResponse response =
            minorCreditorService.getMinorCreditorAccountHeaderSummary(minorCreditorId);

        return buildResponse(response);
    }
}
