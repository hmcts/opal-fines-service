package uk.gov.hmcts.opal.service.opal.till;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS;
import static uk.gov.hmcts.opal.entity.AssociatedRecordType.DEFENDANT_ACCOUNTS;
import static uk.gov.hmcts.opal.entity.DestinationType.F;
import static uk.gov.hmcts.opal.entity.DestinationType.S;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.DestinationType;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.TillsGetResponse;
import uk.gov.hmcts.opal.mapper.till.GetTillMapper;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@ExtendWith(MockitoExtension.class)
class TillsServiceTest {

    private static final Long TILL_ID = 123L;
    private static final Short BUSINESS_UNIT_ID = (short) 77;
    private static final Long DEFENDANT_ACCOUNT_ID = 456L;

    @Mock
    private TillRepository tillRepository;

    @Mock
    private PaymentInRepository paymentInRepository;

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private UserStateService userStateService;

    @Mock
    private GetTillMapper getTillMapper;

    @InjectMocks
    private TillsService service;

    private TillEntity till;

    @BeforeEach
    void setUp() {
        till = TillEntity.builder()
            .tillId(TILL_ID)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId(BUSINESS_UNIT_ID).build())
            .build();
    }

    @Nested
    class GetTill {

        @Test
        void whenTillExists_returnsMappedResponse_happyPath() {
            PaymentInEntity finePayment = payment(1L, F, DEFENDANT_ACCOUNTS,
                String.valueOf(DEFENDANT_ACCOUNT_ID));
            PaymentInEntity suspensePayment = payment(2L, S, AssociatedRecordType.SUSPENSE_ITEMS, "789");
            DefendantAccountEntity defendantAccount = DefendantAccountEntity.builder()
                .defendantAccountId(DEFENDANT_ACCOUNT_ID)
                .build();
            TillsGetResponse expected = new TillsGetResponse();
            when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(till));
            when(userStateService.getPermittedBusinessUnitIds(
                List.of(BUSINESS_UNIT_ID), PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of(BUSINESS_UNIT_ID));
            when(paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(TILL_ID))
                .thenReturn(List.of(finePayment, suspensePayment));
            when(defendantAccountRepository.findAllByDefendantAccountIdIn(List.of(DEFENDANT_ACCOUNT_ID)))
                .thenReturn(List.of(defendantAccount));
            when(getTillMapper.toResponse(till, List.of(finePayment, suspensePayment),
                Map.of(DEFENDANT_ACCOUNT_ID, defendantAccount))).thenReturn(expected);

            TillsGetResponse actual = service.getTill(TILL_ID);

            assertAll(
                () -> assertSame(expected, actual),
                () -> verify(getTillMapper).toResponse(
                    eq(till), eq(List.of(finePayment, suspensePayment)),
                    eq(Map.of(DEFENDANT_ACCOUNT_ID, defendantAccount)))
            );
        }

        @Test
        void whenTillDoesNotExist_returnsNotFound_sadPath() {
            when(tillRepository.findById(TILL_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getTill(TILL_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Till not found with id: " + TILL_ID);

            verifyNoInteractions(paymentInRepository, defendantAccountRepository, userStateService, getTillMapper);
        }

        @Test
        void whenBusinessUnitIsNotPermitted_rejectsRequest_sadPath() {
            when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(till));
            when(userStateService.getPermittedBusinessUnitIds(
                List.of(BUSINESS_UNIT_ID), PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of());

            assertThatThrownBy(() -> service.getTill(TILL_ID))
                .isInstanceOf(PermissionNotAllowedException.class);

            assertAll(
                () -> verifyNoInteractions(paymentInRepository, defendantAccountRepository, getTillMapper),
                () -> verify(userStateService).getPermittedBusinessUnitIds(
                    List.of(BUSINESS_UNIT_ID), PROCESS_AND_ALLOCATE_PAYMENTS)
            );
        }

        @Test
        void whenTillHasNoPayments_rejectsInvalidState_sadPath() {
            mockPermittedTill();
            when(paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(TILL_ID))
                .thenReturn(List.of());

            assertThatThrownBy(() -> service.getTill(TILL_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Till " + TILL_ID + " has no payments in");

            verifyNoInteractions(defendantAccountRepository, getTillMapper);
        }

        @Test
        void whenFinePaymentDoesNotReferenceDefendantAccount_rejectsInvalidState_sadPath() {
            PaymentInEntity payment = payment(1L, F, AssociatedRecordType.SUSPENSE_ITEMS, "789");
            mockPermittedTill();
            when(paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(TILL_ID))
                .thenReturn(List.of(payment));

            assertThatThrownBy(() -> service.getTill(TILL_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Fine payment 1 does not reference a defendant account");

            verifyNoInteractions(defendantAccountRepository, getTillMapper);
        }

        private void mockPermittedTill() {
            when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(till));
            when(userStateService.getPermittedBusinessUnitIds(
                List.of(BUSINESS_UNIT_ID), PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of(BUSINESS_UNIT_ID));
        }
    }

    private PaymentInEntity payment(Long id, DestinationType destinationType,
        AssociatedRecordType recordType, String recordId) {

        return PaymentInEntity.builder()
            .paymentInId(id)
            .destinationType(destinationType)
            .associatedRecordType(recordType)
            .associatedRecordId(recordId)
            .build();
    }
}
