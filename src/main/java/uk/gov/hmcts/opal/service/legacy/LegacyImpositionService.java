package uk.gov.hmcts.opal.service.legacy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Creditor;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Imposition;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Offence;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.PostedDetails;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Result;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetImpositionsRequest;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon;
import uk.gov.hmcts.opal.generated.model.OffenceReferenceCommon;
import uk.gov.hmcts.opal.generated.model.ResultReferenceCommon;
import uk.gov.hmcts.opal.service.iface.ImpositionServiceInterface;

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
            Response<GetDefendantAccountImpositionsLegacyResponse> response = gatewayService.postToGateway(
                GET_IMPOSITIONS, GetDefendantAccountImpositionsLegacyResponse.class,
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
        GetDefendantAccountImpositionsLegacyResponse legacyImpositionsResponse) {

        return Optional.ofNullable(legacyImpositionsResponse).map(imposition ->
            GetDefendantAccountImpositionsResponse.builder()
                .version(legacyImpositionsResponse.getVersion())
                .payload(buildImpositionList(legacyImpositionsResponse.getImpositions()))
                .build()).orElse(null);
    }

    private DefendantAccountImpositionsResponseCommon buildImpositionList(
        List<Imposition> impositions) {
        return Optional.ofNullable(impositions).map(content ->
            DefendantAccountImpositionsResponseCommon.builder()
                .impositions(buildImpositions(impositions))
                .build()
        ).orElse(null);
    }

    private List<DefendantAccountImpositionCommon> buildImpositions(
        List<Imposition> impositions) {

        List<DefendantAccountImpositionCommon> outcome = null;

        if (impositions != null) {
            for (Imposition imposition : impositions) {
                if (imposition != null) {
                    outcome = outcome == null ? new ArrayList<>() : outcome;

                    outcome.add(DefendantAccountImpositionCommon.builder()
                            .dateAdded(buildDateAdded(imposition.getPostedDetails()))
                            .imposition(buildImposition(imposition.getResult()))
                            .creditor(buildCreditor(imposition.getCreditor()))
                            .imposedAmount(imposition.getImposedAmount())
                            .paidAmount(imposition.getPaidAmount())
                            .balance(imposition.getBalance())
                            .dateImposed(imposition.getDateImposed())
                            .offence(buildOffence(imposition.getOffence()))
                            .impositionId(imposition.getImpositionId())
                        .build());
                }
            }
        }

        return outcome;
    }

    private LocalDate buildDateAdded(PostedDetails postedDetails) {
        return postedDetails == null || postedDetails.getPostedDate() == null
            ? null
            : postedDetails.getPostedDate().toLocalDate();
    }

    private OffenceReferenceCommon buildOffence(Offence offence) {
        return Optional.ofNullable(offence).map(offenceItem ->
            OffenceReferenceCommon.builder()
                .id(offenceItem.getOffenceId())
                .code(offenceItem.getCjsCode())
                .title(offenceItem.getOffenceTitle())
                .build()).orElse(null);
    }

    private ImpositionCreditorReferenceCommon buildCreditor(Creditor creditor) {
        return Optional.ofNullable(creditor).map(creditorItem ->
                ImpositionCreditorReferenceCommon.builder()
                    .creditorAccountId(creditorItem.getCreditorAccountId())
                    .accountType(buildAccountType(creditorItem))
                    .displayName(buildDisplayName(creditorItem))
                    .name(creditorItem.getMajorCreditorName())
                    .build()
            ).orElse(null);
    }

    private ImpositionCreditorReferenceCommon.AccountTypeEnum buildAccountType(Creditor creditor) {
        String accountType = getAccountType(creditor);
        return accountType == null ? null : ImpositionCreditorReferenceCommon.AccountTypeEnum.fromValue(accountType);
    }

    private ImpositionCreditorReferenceCommon.DisplayNameEnum buildDisplayName(Creditor creditor) {
        String displayName = CreditorAccountType.getDisplayName(getAccountType(creditor));
        return displayName == null
            ? null
            : ImpositionCreditorReferenceCommon.DisplayNameEnum.fromValue(displayName);
    }

    private String getAccountType(Creditor creditor) {
        return creditor.getCreditorAccountType() == null
            ? null
            : creditor.getCreditorAccountType().getCreditorAccountType();
    }

    private ResultReferenceCommon buildImposition(Result imposition) {
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
