package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.entity.AssociatedRecordType.DEFENDANT_ACCOUNTS;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.SuspenseItemRepository;

@ExtendWith(MockitoExtension.class)
class CashListPaymentLinkServiceTest {

    private static final Long DEFENDANT_ACCOUNT_ID = 99000000020000L;
    private static final Long SUSPENSE_ITEM_ID = 99000000030000L;

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private SuspenseItemRepository suspenseItemRepository;

    private CashListPaymentLinkService service;

    @BeforeEach
    void setUp() {
        service = new CashListPaymentLinkService(defendantAccountRepository, suspenseItemRepository);
    }

    @Test
    void getDefendantAccount_loadsDefendantAccountByAssociatedRecordId() {
        PaymentInEntity payment = payment(1L, String.valueOf(DEFENDANT_ACCOUNT_ID));
        DefendantAccountEntity defendantAccount = DefendantAccountEntity.builder()
            .defendantAccountId(DEFENDANT_ACCOUNT_ID)
            .build();
        when(defendantAccountRepository.findByDefendantAccountId(DEFENDANT_ACCOUNT_ID))
            .thenReturn(Optional.of(defendantAccount));

        DefendantAccountEntity result = service.getDefendantAccount(payment);

        assertThat(result).isSameAs(defendantAccount);
    }

    @Test
    void getSuspenseItem_loadsSuspenseItemByAssociatedRecordId() {
        PaymentInEntity payment = payment(1L, String.valueOf(SUSPENSE_ITEM_ID));
        SuspenseItemEntity suspenseItem = SuspenseItemEntity.builder()
            .suspenseItemId(SUSPENSE_ITEM_ID)
            .build();
        when(suspenseItemRepository.findById(SUSPENSE_ITEM_ID)).thenReturn(Optional.of(suspenseItem));

        SuspenseItemEntity result = service.getSuspenseItem(payment);

        assertThat(result).isSameAs(suspenseItem);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void getDefendantAccount_throwsWhenAssociatedRecordIdIsMissing(String associatedRecordId) {
        PaymentInEntity payment = payment(1L, associatedRecordId);

        assertThatThrownBy(() -> service.getDefendantAccount(payment))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Payment 1 is missing associated_record_id for " + DEFENDANT_ACCOUNTS.getLabel());
        verifyNoInteractions(defendantAccountRepository, suspenseItemRepository);
    }

    @Test
    void getSuspenseItem_throwsWhenAssociatedRecordIdIsInvalid() {
        PaymentInEntity payment = payment(1L, "not-a-number");

        assertThatThrownBy(() -> service.getSuspenseItem(payment))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Payment 1 has invalid associated_record_id: not-a-number");
        verifyNoInteractions(defendantAccountRepository, suspenseItemRepository);
    }

    @Test
    void getDefendantAccount_throwsWhenDefendantAccountDoesNotExist() {
        PaymentInEntity payment = payment(1L, String.valueOf(DEFENDANT_ACCOUNT_ID));
        when(defendantAccountRepository.findByDefendantAccountId(DEFENDANT_ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDefendantAccount(payment))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Defendant account not found for associated_record_id: " + DEFENDANT_ACCOUNT_ID);
    }

    @Test
    void getSuspenseItem_throwsWhenSuspenseItemDoesNotExist() {
        PaymentInEntity payment = payment(1L, String.valueOf(SUSPENSE_ITEM_ID));
        when(suspenseItemRepository.findById(SUSPENSE_ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSuspenseItem(payment))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Suspense item not found for associated_record_id: " + SUSPENSE_ITEM_ID);
    }

    private static PaymentInEntity payment(Long paymentInId, String associatedRecordId) {
        return PaymentInEntity.builder()
            .paymentInId(paymentInId)
            .associatedRecordId(associatedRecordId)
            .build();
    }
}
