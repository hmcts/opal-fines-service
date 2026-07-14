package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.GetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountConsolidatedAccountsResult;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.generated.http.api.DefendantAccountApi;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlanceResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response;
import uk.gov.hmcts.opal.generated.model.ConsolidatedAccountDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponse;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.UpdateDefendantAccountRequestPayload;
import uk.gov.hmcts.opal.generated.model.UpdateDefendantAccountResponsePayload;
import uk.gov.hmcts.opal.mapper.history.DefendantAccountHistoryResponseMapper;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.ImpositionService;
import uk.gov.hmcts.opal.util.VersionUtils;

@RestController
@Slf4j(topic = "opal.DefendantAccountApiController")
@RequiredArgsConstructor
public class DefendantAccountApiController implements DefendantAccountApi {

    private final DefendantAccountService defendantAccountService;
    private final DefendantAccountHistoryResponseMapper defendantAccountHistoryResponseMapper;
    private final ImpositionService impositionService;

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetDefendantAccountHeaderSummary200Response> getDefendantAccountHeaderSummary(Long id) {
        log.debug(":GET:getDefendantAccountHeaderSummary: for defendant id: {}", id);

        DefendantAccountHeaderSummary summary = defendantAccountService.getHeaderSummary(id);

        return ResponseEntity.ok().eTag(VersionUtils.createETag(summary)).body(summary.getResponse());
    }

    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    @Override
    public ResponseEntity<DefendantAccountImpositionsResponseCommon> getImpositions(Long id) {
        log.debug(":GET:getImpositions: for defendant account id: {}", id);

        GetDefendantAccountImpositionsResponse response = impositionService.getImpositions(id);

        return ResponseEntity.ok().eTag(VersionUtils.createETag(response)).body(response.getPayload());
    }

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<List<ConsolidatedAccountDefendantAccount>> getConsolidatedAccounts(
        Long defendantAccountId) {
        log.debug(":GET:getConsolidatedAccounts: for defendant account id: {}", defendantAccountId);

        GetDefendantAccountConsolidatedAccountsResult response =
            defendantAccountService.getConsolidatedAccounts(defendantAccountId);

        return ResponseEntity.ok().eTag(VersionUtils.createETag(response)).body(response.getPayload());
    }

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<DefendantAccountAtAGlanceResponseDefendantAccount> getDefendantAccountAtAGlance(Long id) {
        log.debug(":GET:getDefendantAccountAtAGlance: for defendant account id: {}", id);

        GetDefendantAccountAtAGlanceResponse response = defendantAccountService.getAtAGlance(id);

        return ResponseEntity.ok()
            .eTag(VersionUtils.createETag(response))
            .body(response.getPayload());
    }

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetEnforcementStatusResponse> getEnforcementStatus(Long id) {
        log.debug(":GET:getDefendantAccountEnforcementStatus: for defendant id: {}", id);

        return buildResponse(defendantAccountService.getEnforcementStatus(id));
    }

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<PostDefendantAccountSearchResponseDefendantAccount> postDefendantAccountSearch(
        PostDefendantAccountSearchRequestDefendantAccount request) {
        log.debug(":POST:postDefendantAccountSearch");

        return buildResponse(defendantAccountService.searchDefendantAccounts(request));
    }

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<UpdateDefendantAccountResponsePayload> updateDefendantAccount(Long defendantAccountId,
        String businessUnitId, UpdateDefendantAccountRequestPayload request, String ifMatch) {
        log.debug(":PATCH:updateDefendantAccount: id={}", defendantAccountId);

        UpdateDefendantAccountResponse response =
            defendantAccountService.updateDefendantAccount(defendantAccountId, businessUnitId, request, ifMatch);

        return ResponseEntity.ok().eTag(VersionUtils.createETag(response)).body(response.getPayload());
    }

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetDefendantAccountHistoryResponse> getDefendantAccountHistory(
        Long id,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<String> itemTypes) {

        log.debug(":GET:getDefendantAccountHistory: for defendant id: {}", id);

        DefendantAccountHistoryResponse response =
            defendantAccountService.getHistory(id, dateFrom, dateTo, itemTypes);

        GetDefendantAccountHistoryResponse generatedResponse =
            defendantAccountHistoryResponseMapper.toGeneratedResponse(response);

        return ResponseEntity.ok()
            .eTag(VersionUtils.createETag(response))
            .body(generatedResponse);
    }

}
