package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.service.opal.history.HistoryItemOrderingService;
import uk.gov.hmcts.opal.service.opal.history.source.AmendmentHistorySourceService;
import uk.gov.hmcts.opal.service.opal.history.source.DefendantTransactionHistorySourceService;
import uk.gov.hmcts.opal.service.opal.history.source.EnforcementHistorySourceService;
import uk.gov.hmcts.opal.service.opal.history.source.NoteHistorySourceService;
import uk.gov.hmcts.opal.service.opal.history.source.PaymentTermsHistorySourceService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.OpalDefendantAccountHistoryService")
public class OpalDefendantAccountHistoryService {

    private static final String DEFENDANT_ACCOUNT_NOT_FOUND = "Defendant Account not found with id: ";

    private final DefendantAccountRepository defendantAccountRepository;

    private final HistoryItemOrderingService historyItemOrderingService;

    private final AmendmentHistorySourceService amendmentHistorySourceService;

    private final EnforcementHistorySourceService enforcementHistorySourceService;

    private final NoteHistorySourceService noteHistorySourceService;

    private final PaymentTermsHistorySourceService paymentTermsHistorySourceService;

    private final DefendantTransactionHistorySourceService defendantTransactionHistorySourceService;

    @Transactional(readOnly = true)
    public DefendantAccountHistoryResponse getHistory(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        log.debug(":getHistorySources: Opal mode - ID: {}", defendantAccountId);

        DefendantAccountEntity defendantAccount = defendantAccountRepository
            .findByDefendantAccountId(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(DEFENDANT_ACCOUNT_NOT_FOUND + defendantAccountId));

        DefendantAccountHistorySources sources = DefendantAccountHistorySources.builder()
            .amendments(amendmentHistorySourceService.fetch(defendantAccountId, filter))
            .enforcements(enforcementHistorySourceService.fetch(defendantAccountId, filter))
            .notes(noteHistorySourceService.fetch(defendantAccountId, filter))
            .paymentTerms(paymentTermsHistorySourceService.fetch(defendantAccountId, filter))
            .transactions(defendantTransactionHistorySourceService.fetch(defendantAccountId, filter))
            .build();

        return DefendantAccountHistoryResponse.builder()
            .version(defendantAccount.getVersion())
            .historyItems(toHistoryItems(sources))
            .build();
    }

    private List<DefendantAccountHistoryItem> toHistoryItems(DefendantAccountHistorySources sources) {
        return historyItemOrderingService.orderNewestFirst(List.of(
            sources.getAmendments(),
            sources.getEnforcements(),
            sources.getNotes(),
            sources.getPaymentTerms(),
            sources.getTransactions()
        ).stream().flatMap(List::stream).toList());
    }

    @Value
    @Builder
    public static class DefendantAccountHistorySources {

        List<DefendantAccountHistoryItem> amendments;

        List<DefendantAccountHistoryItem> enforcements;

        List<DefendantAccountHistoryItem> notes;

        List<DefendantAccountHistoryItem> paymentTerms;

        List<DefendantAccountHistoryItem> transactions;
    }
}
