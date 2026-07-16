package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.entity.AssociatedRecordType.DEFENDANT_ACCOUNTS;
import static uk.gov.hmcts.opal.entity.AssociatedRecordType.SUSPENSE_ITEMS;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.SuspenseItemRepository;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;

@Component
@RequiredArgsConstructor
public class CashListPaymentLinkService {

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;
    private final SuspenseItemRepository suspenseItemRepository;

    public DefendantAccountEntity getDefendantAccount(PaymentInEntity payment) {
        Long defendantAccountId = parseAssociatedRecordId(payment, DEFENDANT_ACCOUNTS.getLabel());
        try {
            return defendantAccountRepositoryService.findById(defendantAccountId);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException(
                "Defendant account not found for associated_record_id: " + defendantAccountId, e);
        }
    }

    public SuspenseItemEntity getSuspenseItem(PaymentInEntity payment) {
        Long suspenseItemId = parseAssociatedRecordId(payment, SUSPENSE_ITEMS.getLabel());
        return suspenseItemRepository.findById(suspenseItemId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Suspense item not found for associated_record_id: " + suspenseItemId));
    }

    private static Long parseAssociatedRecordId(PaymentInEntity payment, String expectedRecordType) {
        String associatedRecordId = payment.getAssociatedRecordId();
        if (associatedRecordId == null || associatedRecordId.isBlank()) {
            throw new IllegalArgumentException(
                "Payment " + payment.getPaymentInId() + " is missing associated_record_id for " + expectedRecordType);
        }
        try {
            return Long.valueOf(associatedRecordId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Payment " + payment.getPaymentInId() + " has invalid associated_record_id: " + associatedRecordId,
                e);
        }
    }
}
