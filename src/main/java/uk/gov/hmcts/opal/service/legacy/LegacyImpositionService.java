package uk.gov.hmcts.opal.service.legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.dto.legacy.CreditorSummaryLegacy;
import uk.gov.hmcts.opal.dto.legacy.IndividualNameLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyCourtReferenceCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetImpositionsRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyResultReferenceCommon;
import uk.gov.hmcts.opal.dto.legacy.OffenceReferenceLegacy;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.generated.model.CourtReferenceCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon;
import uk.gov.hmcts.opal.generated.model.OffenceReferenceCommon;
import uk.gov.hmcts.opal.generated.model.ResultReferenceCommon;
import uk.gov.hmcts.opal.service.iface.ImpositionServiceInterface;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyImpositionService")
public class LegacyImpositionService implements ImpositionServiceInterface {

    public static final String GET_IMPOSITIONS = "getDefendantAccountImpositions";

    /* ---- Services ---- */
    private final GatewayService gatewayService;

    @Override
    public GetDefendantAccountImpositionsResponse getImpositions(Long defendantAccountId) {
        log.debug(":getImpositions: id: {}", defendantAccountId);

        try {
            Response<LegacyDefendantAccountImpositionsResponseCommon> response = gatewayService.postToGateway(
                GET_IMPOSITIONS, LegacyDefendantAccountImpositionsResponseCommon.class,
                createGetDefendantAccountImpositionsRequest(defendantAccountId.toString()), null);

            checkResponseForError(response, "getImpositions");

            return toAccountImpositionsResponse(response.responseEntity);
        } catch (RuntimeException e) {
            log.error(":getImpositions: problem with call to Legacy: {}", e.getClass().getName());
            log.error(":getImpositions:", e);
            throw e;
        }
    }

    /* This maybe should move to the common response builders later */
    private GetDefendantAccountImpositionsResponse toAccountImpositionsResponse(
        LegacyDefendantAccountImpositionsResponseCommon legacyImpositionsResponse) {

        return Optional.ofNullable(legacyImpositionsResponse).map(imposition ->
            GetDefendantAccountImpositionsResponse.builder()
                .version(legacyImpositionsResponse.getVersion())
                .payload(buildImpositionList(legacyImpositionsResponse.getImpositions()))
                .build()).orElse(null);
    }

    private DefendantAccountImpositionsResponseCommon buildImpositionList(
        List<LegacyDefendantAccountImpositionCommon> impositions) {
        return Optional.ofNullable(impositions).map(content ->
            DefendantAccountImpositionsResponseCommon.builder()
                .impositions(buildImpositions(impositions))
                .build()
        ).orElse(null);
    }

    private List<DefendantAccountImpositionCommon> buildImpositions(
        List<LegacyDefendantAccountImpositionCommon> impositions) {

        List<DefendantAccountImpositionCommon> outcome = null;

        if (impositions != null) {
            for (LegacyDefendantAccountImpositionCommon imposition : impositions) {
                if (imposition != null) {
                    outcome = outcome == null ? new ArrayList<>() : outcome;

                    outcome.add(DefendantAccountImpositionCommon.builder()
                            .dateAdded(imposition.getDateAdded())
                            .imposition(buildImposition(imposition.getImposition()))
                            .creditor(buildCreditor(imposition.getCreditor()))
                            .imposedAmount(imposition.getImposedAmount())
                            .paidAmount(imposition.getPaidAmount())
                            .balance(imposition.getBalance())
                            .dateImposed(imposition.getDateImposed())
                            .offence(buildOffence(imposition.getOffence()))
                            .imposedBy(buildCourtReference(imposition.getImposedBy()))
                            .impositionId(imposition.getImpositionId())
                        .build());
                }
            }
        }

        return outcome;
    }

    private CourtReferenceCommon buildCourtReference(LegacyCourtReferenceCommon courtReference) {
        return Optional.ofNullable(courtReference).map(courtReferenceItem ->
            CourtReferenceCommon.builder()
                .courtId(courtReferenceItem.getCourtId())
                .courtCode(toShort(courtReferenceItem.getCourtCode()))
                .courtName(courtReferenceItem.getCourtName())
                .build()).orElse(null);
    }

    private Short toShort(Integer value) {
        return value == null ? null : value.shortValue();
    }

    private OffenceReferenceCommon buildOffence(OffenceReferenceLegacy offence) {
        return Optional.ofNullable(offence).map(offenceItem ->
            OffenceReferenceCommon.builder()
                .id(offenceItem.getOffenceId())
                .code(offenceItem.getCjsCode())
                .title(offenceItem.getOffenceTitle())
                .build()).orElse(null);
    }

    private ImpositionCreditorReferenceCommon buildCreditor(CreditorSummaryLegacy creditor) {
        return Optional.ofNullable(creditor).map(creditorItem ->
                ImpositionCreditorReferenceCommon.builder()
                    .creditorAccountId(creditorItem.getCreditorAccountId())
                    .accountType(buildAccountType(creditorItem))
                    .displayName(buildDisplayName(creditorItem))
                    .name(buildCreditorName(creditorItem))
                    .build()
            ).orElse(null);
    }

    private ImpositionCreditorReferenceCommon.AccountTypeEnum buildAccountType(
        CreditorSummaryLegacy creditor) {
        String accountType = getAccountType(creditor);
        return accountType == null ? null : ImpositionCreditorReferenceCommon.AccountTypeEnum.fromValue(accountType);
    }

    private ImpositionCreditorReferenceCommon.DisplayNameEnum buildDisplayName(
        CreditorSummaryLegacy creditor) {
        String displayName = CreditorAccountType.getDisplayName(getAccountType(creditor));
        return displayName == null
            ? null
            : ImpositionCreditorReferenceCommon.DisplayNameEnum.fromValue(displayName);
    }

    private String getAccountType(CreditorSummaryLegacy creditor) {
        return creditor.getCreditorAccountTypeReference() == null
            ? null
            : creditor.getCreditorAccountTypeReference().getCreditorAccountType();
    }

    private String buildCreditorName(CreditorSummaryLegacy creditor) {
        String individualName = buildIndividualName(creditor.getIndividualName());
        String companyName = creditor.getCompanyName() == null
            ? null
            : creditor.getCompanyName().getOrganisationName();
        return firstNonBlank(creditor.getMajorCreditorName(), individualName, companyName);
    }

    private String buildIndividualName(IndividualNameLegacy individualName) {
        return individualName == null
            ? null
            : firstNonBlank(String.join(" ", nullToEmpty(individualName.getForenames()),
                                         nullToEmpty(individualName.getSurname())));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private ResultReferenceCommon buildImposition(LegacyResultReferenceCommon imposition) {
        return Optional.ofNullable(imposition).map(impositionItem ->
            ResultReferenceCommon.builder()
                .resultId(impositionItem.getResultId())
                .resultTitle(impositionItem.getResultTitle())
                .build()
        ).orElse(null);
    }

    /* This is probably common code that will be needed across multiple Legacy requests to get
    Defendant Account details. */
    private LegacyGetImpositionsRequest createGetDefendantAccountImpositionsRequest(String defendantAccountId) {
        return LegacyGetImpositionsRequest.builder()
            .defendantAccountId(defendantAccountId)
            .build();
    }

    /* This looks like a candidate for communalization for several legacy services...*/
    private static <T> void checkResponseForError(Response<T> response, String method) {
        if (response.isError()) {
            log.error(":{}: legacy error HTTP {}", method, response.code);
            if (response.isException()) {
                log.error(":{}: exception:", method, response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":{}: legacy failure body:\n{}", method, response.body);
            }
        } else if (response.isSuccessful()) {
            log.info(":{}: legacy success.", method);
        }
    }

}
