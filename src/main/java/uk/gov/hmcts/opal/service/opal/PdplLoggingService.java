package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.util.JsonPathUtil;
import uk.gov.hmcts.opal.util.LogUtil;

@Service
@Slf4j(topic = "opal.pdplLoggingService")
@RequiredArgsConstructor
public class PdplLoggingService {

    private final LoggingService loggingService;
    private final Clock clock;

    public void pdplForSubmitDraftAccount(DraftAccountEntity entity) {
        JsonPathUtil.DocContext docContext = createDocContext(entity.getAccount(),
            "AddDraftAccountRequestDto.account");

        Object dtRaw = docContext.read("$.defendant_type");
        String defendantType = dtRaw == null ? "" : dtRaw.toString();

        if (!defendantType.equalsIgnoreCase("company")) {

            switch (defendantType) {
                case "adultOrYouthOnly" -> logSubmitDraftAccountDefendantInfo(entity);
                case "pgToPay" -> {
                    logSubmitDraftAccountParentGuardianInfo(entity);
                    logSubmitDraftAccountDefendantInfo(entity);
                }
            }

            logSubmitDraftAccountMinorCreditorInfo(docContext, entity);
        }
    }

    private void logSubmitDraftAccountPersonalDataProcessing(DraftAccountEntity entity,
        String businessIdentifier) {

        ParticipantIdentifier individuals = ParticipantIdentifier.builder()
            .identifier(entity.getDraftAccountId().toString())
            .type(PdplIdentifierType.DRAFT_ACCOUNT)
            .build();

        ParticipantIdentifier createdBy = ParticipantIdentifier.builder()
            .identifier(entity.getSubmittedBy())
            .type(PdplIdentifierType.OPAL_USER_ID)
            .build();

        PersonalDataProcessingLogDetails logDetails = PersonalDataProcessingLogDetails.builder()
            .recipient(null)
            .businessIdentifier(businessIdentifier)
            .category(PersonalDataProcessingCategory.COLLECTION)
            .ipAddress(LogUtil.getIpAddress())
            .createdAt(LogUtil.getCurrentDateTime(clock))
            .createdBy(createdBy)
            .individuals(List.of(individuals))
            .build();

        loggingService.personalDataAccessLogAsync(logDetails);
    }

    void logSubmitDraftAccountDefendantInfo(DraftAccountEntity entity) {
        logSubmitDraftAccountPersonalDataProcessing(entity, "Submit Draft Account - Defendant");
    }

    void logSubmitDraftAccountParentGuardianInfo(DraftAccountEntity entity) {
        logSubmitDraftAccountPersonalDataProcessing(entity, "Submit Draft Account - Parent or Guardian");
    }

    void logSubmitDraftAccountMinorCreditorInfo(JsonPathUtil.DocContext docContext, DraftAccountEntity entity) {

        List<Map<String, Object>> minorCreditors = docContext.read("$..minor_creditor");

        if (minorCreditors == null) {
            return;
        }

        boolean anyIndividualMinor = minorCreditors.stream()
            .filter(Objects::nonNull)
            .map(m -> m.get("company_flag"))
            .anyMatch(flag -> !Boolean.TRUE.equals(flag));

        if (anyIndividualMinor) {
            logSubmitDraftAccountPersonalDataProcessing(entity, "Submit Draft Account - Minor Creditor");
        }
    }


}
