package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;
import static uk.gov.hmcts.opal.util.VersionUtils.createETag;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.dto.CentralFundResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.reference.MajorCreditorReferenceData;
import uk.gov.hmcts.opal.dto.reference.MajorCreditorReferenceDataResults;
import uk.gov.hmcts.opal.generated.http.api.MajorCreditorApi;
import uk.gov.hmcts.opal.generated.model.GetCentralFundResponse;
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorAccountAtAGlance200Response;
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorAccountHeaderSummary200Response;
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorRefData200Response;
import uk.gov.hmcts.opal.service.CentralFundService;
import uk.gov.hmcts.opal.service.MajorCreditorAccountService;
import uk.gov.hmcts.opal.service.opal.MajorCreditorService;
import uk.gov.hmcts.opal.util.FeatureFlags;

@RestController
@Slf4j(topic = "opal.MajorCreditorApiController")
@RequiredArgsConstructor
public class MajorCreditorApiController implements MajorCreditorApi {

    private final CentralFundService centralFundService;
    private final MajorCreditorAccountService majorCreditorAccountService;
    private final MajorCreditorService majorCreditorService;

    @Override
    @FeatureToggle(
        feature = FeatureFlags.RELEASE_1B,
        defaultValueProperty = FeatureFlags.RELEASE_1B_ENABLED_PROPERTY
    )
    public ResponseEntity<GetCentralFundResponse> getCentralFundByBusinessUnit(
        Integer id) {

        log.debug(":GET:getCentralFundByBusinessUnit: businessUnitId={}", id);

        CentralFundResponse response = centralFundService.getCentralFundByBusinessUnit(id);

        return ResponseEntity.ok()
            .eTag(createETag(response))
            .body(response.getPayload());
    }

    @Override
    @FeatureToggle(
        feature = FeatureFlags.RELEASE_1B,
        defaultValueProperty = FeatureFlags.RELEASE_1B_ENABLED_PROPERTY
    )
    public ResponseEntity<GetMajorCreditorAccountAtAGlance200Response> getMajorCreditorAccountAtAGlance(Long id) {
        log.debug(":GET:getMajorCreditorAccountAtAGlance: id={}", id);

        GetMajorCreditorAccountAtAGlanceResponse response = majorCreditorAccountService.getAtAGlance(id);

        return buildResponse(response);
    }

    @Override
    @FeatureToggle(
        feature = FeatureFlags.RELEASE_1B,
        defaultValueProperty = FeatureFlags.RELEASE_1B_ENABLED_PROPERTY
    )
    public ResponseEntity<GetMajorCreditorAccountHeaderSummary200Response> getMajorCreditorAccountHeaderSummary(
        Long id) {

        log.debug(":GET:getMajorCreditorAccountHeaderSummary: id={}", id);

        GetMajorCreditorAccountHeaderSummaryResponse response =
            majorCreditorAccountService.getHeaderSummary(id);

        return buildResponse(response);
    }

    @Override
    @FeatureToggle(
        feature = FeatureFlags.RELEASE_1A,
        defaultValueProperty = FeatureFlags.RELEASE_1A_ENABLED_PROPERTY
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<GetMajorCreditorRefData200Response> getMajorCreditorRefData(
        @Nullable String q,
        @Nullable Integer businessUnit) {

        Optional<String> filter = Optional.ofNullable(q);
        Optional<Short> businessUnitId = Optional.ofNullable(businessUnit).map(Integer::shortValue);

        log.debug(":GET:getMajorCreditorRefData: business unit: {}, filter string: {}", businessUnitId, filter);

        List<MajorCreditorReferenceData> refData = majorCreditorService.getReferenceData(filter, businessUnitId);

        log.debug(":GET:getMajorCreditorRefData: major creditor reference data count: {}", refData.size());
        return (ResponseEntity) ResponseEntity.ok(MajorCreditorReferenceDataResults.builder().refData(refData).build());
    }
}
