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

    private static final String JSON_DEFENDANT_TYPE = "$.defendant_type";
    private static final String JSON_MINOR_CREDITOR = "$..minor_creditor";

    private final LoggingService loggingService;
    private final Clock clock;

    public void pdplForSubmitDraftAccount(DraftAccountEntity entity) {
        pdplForDraftAccount(entity, Action.SUBMIT);
    }

    public void pdplForUpdateDraftAccount(DraftAccountEntity entity) {
        pdplForDraftAccount(entity, Action.RESUBMIT);
    }

    private void pdplForDraftAccount(DraftAccountEntity entity, Action action) {
        JsonPathUtil.DocContext docContext = createDocContext(entity.getAccount(),
            "AddDraftAccountRequestDto.account");

        Object dtRaw = docContext.read(JSON_DEFENDANT_TYPE);
        String defendantType = dtRaw == null ? "" : dtRaw.toString();

        if (defendantType.equalsIgnoreCase("company")) {
            return;
        }

        switch (defendantType) {
            case "adultOrYouthOnly" -> logForRole(entity, action, Role.DEFENDANT);
            case "pgToPay" -> {
                logForRole(entity, action, Role.PARENT_OR_GUARDIAN);
                logForRole(entity, action, Role.DEFENDANT);
            }
            default -> {
                log.debug("Unknown defendant_type '{}', skipping defendant/pg logs", defendantType);
            }
        }

        if (hasAnyIndividualMinor(docContext)) {
            logForRole(entity, action, Role.MINOR_CREDITOR);
        }
    }

    private void logForRole(DraftAccountEntity entity, Action action, Role role) {
        String businessIdentifier = action.formatFor(role);
        logDraftAccountPersonalDataProcessing(entity, businessIdentifier);
    }

    private boolean hasAnyIndividualMinor(JsonPathUtil.DocContext docContext) {
        List<Map<String, Object>> minorCreditors = docContext.read(JSON_MINOR_CREDITOR);
        if (minorCreditors == null || minorCreditors.isEmpty()) {
            return false;
        }

        return minorCreditors.stream()
            .filter(Objects::nonNull)
            .map(m -> m.get("company_flag"))
            .anyMatch(flag -> !Boolean.TRUE.equals(flag));
    }

    private void logDraftAccountPersonalDataProcessing(DraftAccountEntity entity,
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

    private enum Role {
        DEFENDANT("Defendant"),
        PARENT_OR_GUARDIAN("Parent or Guardian"),
        MINOR_CREDITOR("Minor Creditor");

        private final String label;

        Role(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    private enum Action {
        SUBMIT("Submit Draft Account - %s"),
        RESUBMIT("Re-submit Draft Account - %s");

        private final String template;

        Action(String template) {
            this.template = template;
        }

        public String formatFor(Role role) {
            return String.format(template, role.label());
        }
    }
}
