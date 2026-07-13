package uk.gov.hmcts.opal.service.legacy;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.opal.dto.GetDefendantAccountConsolidatedAccountsResult;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.legacy.LegacyConsolidatedAccount;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountConsolidatedAccountsResponse;
import uk.gov.hmcts.opal.generated.model.ConsolidatedAccountDefendantAccount;
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
            .version(new BigInteger(legacy.getVersion()))
            .build();
    }

    static GetDefendantAccountConsolidatedAccountsResult toConsolidatedAccountsResponse(
        LegacyGetDefendantAccountConsolidatedAccountsResponse legacy) {

        return GetDefendantAccountConsolidatedAccountsResult.builder()
            .version(BigInteger.valueOf(legacy.getVersion()))
            .payload(toConsolidatedAccounts(legacy.getConsolidatedAccounts()))
            .build();
    }

    private static List<ConsolidatedAccountDefendantAccount> toConsolidatedAccounts(
        List<LegacyConsolidatedAccount> consolidatedAccounts) {

        return Optional.ofNullable(consolidatedAccounts)
            .orElse(Collections.emptyList())
            .stream()
            .filter(account -> account != null)
            .map(LegacyDefendantAccountBuilders::toConsolidatedAccount)
            .sorted(Comparator.comparing(ConsolidatedAccountDefendantAccount::getAccountId,
                                         Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    }

    private static ConsolidatedAccountDefendantAccount toConsolidatedAccount(LegacyConsolidatedAccount account) {
        return ConsolidatedAccountDefendantAccount.builder()
            .accountId(account.getAccountId())
            .accountNumber(account.getAccountNumber())
            .firstName(account.getFirstName())
            .lastName(account.getLastName())
            .dateImposed(account.getDateImposed())
            .imposedBy(account.getImposedBy())
            .reference(account.getReference())
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

    static EnforcerReferenceCommon buildEnforcerReference(EnforcerReference enforcerRef) {
        return EnforcerReferenceCommon.builder()
            .enforcerId(enforcerRef.getEnforcerId())
            .enforcerName(enforcerRef.getEnforcerName())
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

        return Optional.ofNullable(enforcementAction).map(action ->
            EnforcementActionDefendantAccount.builder()
                .warrantNumber(action.getWarrantNumber())
                .reason(action.getReason())
                .dateAdded(LocalDateTime.parse(action.getDateAdded()))
                .enforcer(buildEnforcerReference(action.getEnforcer()))
                .enforcementAction(buildResultReferenceCommon(action.getResultReference()))
                .resultResponses(buildResultResponses(action.getResultResponses()))
                .build()).orElse(null);
    }

    static List<ResultResponsesCommon> buildResultResponses(ResultResponses responses) {
        return Optional.ofNullable(responses)
            .map(LegacyDefendantAccountBuilders::buildResultResponse)
            .map(List::of)
            .orElse(null);
    }

    static ResultResponsesCommon buildResultResponse(ResultResponses responses) {
        return ResultResponsesCommon.builder()
            .parameterName(responses.getParameterName())
            .response(responses.getResponse())
            .build();
    }

    static ResultReferenceCommon buildResultReferenceCommon(ResultReference resultRef) {
        return ResultReferenceCommon.builder()
            .resultId(String.valueOf(resultRef.getResultId()))
            .resultTitle(resultRef.getResultTitle())
            .build();
    }

}
