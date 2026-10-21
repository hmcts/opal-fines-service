package uk.gov.hmcts.opal.service.opal.till;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;

@ExtendWith(MockitoExtension.class)
class TestingSupportTillServiceTest {

    @Mock
    private TillRepository tillRepository;

    @Mock
    private PaymentInRepository paymentInRepository;

    @InjectMocks
    private TestingSupportTillService service;

    @Test
    void deleteManualTills_deletesOnlyExistingManualTillsAndPayments() {
        TillEntity manualTill = TillEntity.builder()
            .tillId(1L)
            .autoPayment(false)
            .build();

        TillEntity autoPaymentTill = TillEntity.builder()
            .tillId(2L)
            .autoPayment(true)
            .build();

        when(tillRepository.findAllById(List.of(1L, 2L, 999L)))
            .thenReturn(List.of(manualTill, autoPaymentTill));

        service.deleteManualTills(List.of(1L, 1L, 2L, 999L));

        verify(paymentInRepository).deleteByTillEntity_TillIdIn(List.of(1L));
        verify(tillRepository).deleteAllById(List.of(1L));
    }

    @Test
    void deleteManualTills_whenNoManualTillsExist_doesNotDeleteData() {
        when(tillRepository.findAllById(List.of(999L))).thenReturn(List.of());

        service.deleteManualTills(List.of(999L));

        verifyNoInteractions(paymentInRepository);
    }
}