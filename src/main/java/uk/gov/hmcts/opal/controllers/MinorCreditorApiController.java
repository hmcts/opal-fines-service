package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;
import static uk.gov.hmcts.opal.util.VersionUtils.extractOptionalBigInteger;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.generated.http.api.MinorCreditorApi;
import uk.gov.hmcts.opal.generated.model.GetMinorCreditorHistory200Response;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditor;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.generated.model.MinorCreditorSearchRequest;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.service.MinorCreditorService;
import uk.gov.hmcts.opal.util.VersionUtils;

@RestController
@Slf4j(topic = "opal.MinorCreditorApiController")
@RequiredArgsConstructor
public class MinorCreditorApiController implements MinorCreditorApi {

    private final MinorCreditorService minorCreditorService;

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<MinorCreditorAccountResponseMinorCreditor> getMinorCreditorAccount(Long id) {
        log.debug(":GET:getMinorCreditorAccount: id={}", id);

        MinorCreditorAccountResponse result = minorCreditorService.getMinorCreditorAccount(id);

        return buildResponse(result);
    }

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<MinorCreditorAccountAtAGlanceResponse> getMinorCreditorAccountAtAGlance(Long id) {
        log.debug(":GET:getMinorCreditorAccountAtAGlance: id={}", id);

        MinorCreditorAccountAtAGlanceResponse response = minorCreditorService.getMinorCreditorAtAGlance(id);

        return ResponseEntity.ok()
            .eTag(VersionUtils.createETag(response))
            .body(response);
    }

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetMinorCreditorHistory200Response> getMinorCreditorHistory(
        Long id,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<String> itemTypes) {
        log.debug(":GET:getMinorCreditorHistory: id={}", id);

        GetMinorCreditorHistoryResponse response =
            minorCreditorService.getMinorCreditorHistory(id, dateFrom, dateTo, itemTypes);

        return ResponseEntity.ok().eTag(VersionUtils.createETag(response)).body(response.getPayload());
    }

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<MinorCreditorAccountResponseMinorCreditor> patchMinorCreditorAccount(
        Long id,
        String businessUnitId,
        String ifMatch,
        PatchMinorCreditorAccountRequest patchMinorCreditorAccountRequest) {

        log.debug(":PATCH:patchMinorCreditorAccount: id={}", id);

        MinorCreditorAccountResponse result =
            minorCreditorService.updateMinorCreditorAccount(id, patchMinorCreditorAccountRequest,
                extractOptionalBigInteger(ifMatch).orElse(null),
                businessUnitId);

        return buildResponse(result);
    }

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<MinorCreditorAccountsSearchResponse> postMinorCreditorSearch(
        MinorCreditorSearchRequest minorCreditorSearchRequest) {
        log.debug(":POST:postMinorCreditorSearch: query: \n{}", minorCreditorSearchRequest);

        MinorCreditorAccountsSearchResponse response = minorCreditorService
            .searchMinorCreditors(minorCreditorSearchRequest);

        return buildResponse(response);
    }

    @Override
    public ResponseEntity<MinorCreditorAccountHeaderSummaryResponse> getMinorCreditorAccountHeaderSummary(Long id) {
        log.debug(":GET:getMinorCreditorAccountHeaderSummary: minorCreditorId: {}", id);

        GetMinorCreditorAccountHeaderSummaryResponse response =
            minorCreditorService.getMinorCreditorAccountHeaderSummary(id);

        return buildResponse(response);
    }
}
