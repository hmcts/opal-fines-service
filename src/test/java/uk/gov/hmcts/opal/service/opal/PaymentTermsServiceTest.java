package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;

class PaymentTermsServiceTest {

    @Mock
    private DefendantAccountPaymentTermsRepository paymentTermsRepository;

    private PaymentTermsService paymentTermsService;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-05-07T10:15:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentTermsService = new PaymentTermsService(paymentTermsRepository, fixedClock);
    }

    @Test
    void addPaymentTerm_shouldSetActiveAndPostedDateAndSave() {
        PaymentTermsEntity entity = new PaymentTermsEntity();
        entity.setPostedBy("user1");
        entity.setPostedByUsername("username1");
        entity.setPostedDate(LocalDateTime.of(2026, 5, 1, 9, 0));

        when(paymentTermsRepository.save(any(PaymentTermsEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentTermsEntity result = paymentTermsService.addPaymentTerm(entity);

        assertNotNull(result);
        assertEquals(true, result.getActive());
        assertEquals("user1", result.getPostedBy());
        assertEquals("username1", result.getPostedByUsername());
        assertEquals(LocalDateTime.of(2026, 5, 1, 9, 0), result.getPostedDate());
        verify(paymentTermsRepository).save(any(PaymentTermsEntity.class));
    }

    @Test
    void deactivateExistingActivePaymentTerms_shouldCallRepository() {
        Long defendantAccountId = 123L;
        paymentTermsService.deactivateExistingActivePaymentTerms(defendantAccountId);
        verify(paymentTermsRepository).deactivateActiveByDefendantAccountId(defendantAccountId);
    }

    @Test
    void addPaymentTerm_shouldSetPostedDateIfNull() {
        PaymentTermsEntity entity = new PaymentTermsEntity();
        entity.setPostedBy("user2");
        entity.setPostedByUsername("username2");
        entity.setPostedDate(null);

        when(paymentTermsRepository.save(any(PaymentTermsEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentTermsEntity result = paymentTermsService.addPaymentTerm(entity);

        assertEquals(LocalDateTime.of(2026, 5, 7, 10, 15), result.getPostedDate());
        verify(paymentTermsRepository).save(any(PaymentTermsEntity.class));
    }
}
