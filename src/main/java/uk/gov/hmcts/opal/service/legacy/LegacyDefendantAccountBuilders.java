package uk.gov.hmcts.opal.service.legacy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse.EnforcementAction;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse.EnforcementOverview;
import uk.gov.hmcts.opal.dto.legacy.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder;
import uk.gov.hmcts.opal.dto.legacy.common.CourtReference;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverrideResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcerReference;
import uk.gov.hmcts.opal.dto.legacy.common.LjaReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.ResultResponses;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon.AccountStatusCodeEnum;
import uk.gov.hmcts.opal.generated.model.CollectionOrderCommon;
import uk.gov.hmcts.opal.generated.model.CourtReferenceCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementActionDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideResultReferenceCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementOverviewDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcerReferenceCommon;
import uk.gov.hmcts.opal.generated.model.LjaReferenceCommon;
import uk.gov.hmcts.opal.generated.model.ResultReferenceCommon;
import uk.gov.hmcts.opal.generated.model.ResultResponsesCommon;

public class LegacyDefendantAccountBuilders {

    private LegacyDefendantAccountBuilders() {
    }

    static EnforcementStatus toEnforcementStatusResponse(
        LegacyGetDefendantAccountEnforcementStatusResponse legacy) {

        if (legacy == null) {
            return null;
        }

        return EnforcementStatus.builder()
            .employerFlag(Boolean.valueOf(legacy.getEmployerFlag())) // Legacy response is true/false
            .accountStatusReference(buildAccountStatusReferenceCommon(legacy.getAccountStatusReference()))
            .defendantAccountType(null) // Not returned from Legacy
            .enforcementOverride(buildEnforcementOverride(legacy.getEnforcementOverride()))
            .enforcementOverview(buildEnforcementOverviewDefendantAccount(legacy.getEnforcementOverview()))
            .isHmrcCheckEligible(false)  // Always 'false' for Legacy responses
            .lastEnforcementAction(buildEnforcementActionDefendantAccount(legacy.getLastEnforcementAction()))
            .nextEnforcementActionData(null) // Not returned from Legacy
            .version(legacy.getVersion())
            .build();
    }

    static EnforcementOverrideCommon buildEnforcementOverride(EnforcementOverride enforcementOverride) {
        return Optional.ofNullable(enforcementOverride).map(override ->
            EnforcementOverrideCommon.builder()
                .lja(buildLja(override.getLja()))
                .enforcer(buildEnforcerReference(override.getEnforcer()))
                .enforcementOverrideResult(buildEnforcementOverrideResultRef(override.getEnforcementOverrideResult()))
                .build()).orElse(null);
    }

    static LjaReferenceCommon buildLja(LjaReference lja) {
        return LjaReferenceCommon.builder()
            .ljaId(lja.getLjaId())
            .ljaCode(lja.getLjaCode())
            .ljaName(lja.getLjaName())
            .build();
    }

    static EnforcementOverrideResultReferenceCommon buildEnforcementOverrideResultRef(
        EnforcementOverrideResultReference resultRef) {

        return EnforcementOverrideResultReferenceCommon.builder()
            .enforcementOverrideResultId(resultRef.getEnforcementOverrideResultId())
            .enforcementOverrideResultName(resultRef.getEnforcementOverrideResultName())
            .build();
    }

    static EnforcementOverviewDefendantAccount buildEnforcementOverviewDefendantAccount(EnforcementOverview overview) {
        return EnforcementOverviewDefendantAccount.builder()
            .daysInDefault(overview.getDaysInDefault())
            .enforcementCourt(buildCourtReference(overview.getEnforcementCourt()))
            .collectionOrder(buildCollectionOrder(overview.getCollectionOrder()))
            .build();
    }

    static CourtReferenceCommon buildCourtReference(CourtReference courtRef) {
        return CourtReferenceCommon.builder()
            .courtId(courtRef.getCourtId())
            .courtCode(courtRef.getCourtCode())
            .courtName(courtRef.getCourtName())
            .build();
    }

    static CollectionOrderCommon buildCollectionOrder(CollectionOrder collectionOrder) {
        return CollectionOrderCommon.builder()
            .collectionOrderFlag(collectionOrder.getCollectionOrderFlag())
            .collectionOrderDate(collectionOrder.getCollectionOrderDate())
            .build();
    }

    static AccountStatusReferenceCommon buildAccountStatusReferenceCommon(AccountStatusReference statusRef) {
        return AccountStatusReferenceCommon.builder()
            .accountStatusCode(AccountStatusCodeEnum.valueOf(statusRef.getAccountStatusCode()))
            .accountStatusDisplayName(statusRef.getAccountStatusDisplayName())
            .build();
    }

    static EnforcementActionDefendantAccount buildEnforcementActionDefendantAccount(
        EnforcementAction enforcementAction) {

        return Optional.ofNullable(enforcementAction)
            .filter(LegacyDefendantAccountBuilders::hasEnforcementActionData)
            .map(action -> EnforcementActionDefendantAccount.builder()
                .warrantNumber(nullifyBlank(action.getWarrantNumber()))
                .reason(nullifyBlank(action.getReason()))
                .dateAdded(parseLegacyDateAdded(action.getDateAdded()))
                .enforcer(buildEnforcerReference(action.getEnforcer()))
                .enforcementAction(buildResultReferenceCommon(action.getResultReference()))
                .resultResponses(buildResultResponses(action.getResultResponses()))
                .build())
            .orElse(null);
    }

    static List<ResultResponsesCommon> buildResultResponses(ResultResponses responses) {
        ResultResponsesCommon response = buildResultResponse(responses);
        return response == null ? null : List.of(response);
    }

    static ResultResponsesCommon buildResultResponse(ResultResponses responses) {
        return Optional.ofNullable(responses)
            .filter(LegacyDefendantAccountBuilders::hasResultResponseData)
            .map(response -> ResultResponsesCommon.builder()
                .parameterName(nullifyBlank(response.getParameterName()))
                .response(nullifyBlank(response.getResponse()))
                .build())
            .orElse(null);
    }

    static ResultReferenceCommon buildResultReferenceCommon(ResultReference resultRef) {
        return Optional.ofNullable(resultRef)
            .filter(LegacyDefendantAccountBuilders::hasResultReferenceData)
            .map(reference -> ResultReferenceCommon.builder()
                .resultId(nullifyBlank(reference.getResultId()))
                .resultTitle(nullifyBlank(reference.getResultTitle()))
                .build())
            .orElse(null);
    }

    static EnforcerReferenceCommon buildEnforcerReference(EnforcerReference enforcerRef) {
        return Optional.ofNullable(enforcerRef)
            .filter(LegacyDefendantAccountBuilders::hasEnforcerReferenceData)
            .map(enforcer -> EnforcerReferenceCommon.builder()
                .enforcerId(enforcer.getEnforcerId())
                .enforcerName(nullifyBlank(enforcer.getEnforcerName()))
                .build())
            .orElse(null);
    }

    private static LocalDateTime parseLegacyDateAdded(String dateAdded) {
        return isBlank(dateAdded) ? null : LocalDateTime.parse(dateAdded);
    }

    private static boolean hasEnforcementActionData(EnforcementAction action) {
        return !isBlank(action.getWarrantNumber())
            || !isBlank(action.getReason())
            || !isBlank(action.getDateAdded())
            || hasEnforcerReferenceData(action.getEnforcer())
            || hasResultReferenceData(action.getResultReference())
            || hasResultResponseData(action.getResultResponses());
    }

    private static boolean hasEnforcerReferenceData(EnforcerReference enforcerRef) {
        return enforcerRef != null
            && (enforcerRef.getEnforcerId() != null || !isBlank(enforcerRef.getEnforcerName()));
    }

    private static boolean hasResultReferenceData(ResultReference resultRef) {
        return resultRef != null
            && (!isBlank(resultRef.getResultId()) || !isBlank(resultRef.getResultTitle()));
    }

    private static boolean hasResultResponseData(ResultResponses responses) {
        return responses != null
            && (!isBlank(responses.getParameterName()) || !isBlank(responses.getResponse()));
    }

    private static String nullifyBlank(String value) {
        return isBlank(value) ? null : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
