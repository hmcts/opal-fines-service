package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;

class PaymentTermsServiceTest {

    @Mock
    private DefendantAccountPaymentTermsRepository paymentTermsRepository;

    @InjectMocks
    private PaymentTermsService paymentTermsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addPaymentTerm_shouldSetActiveAndPostedDateAndSave() {
        PaymentTermsEntity entity = new PaymentTermsEntity();
        entity.setPostedBy("user1");
        entity.setPostedByUsername("username1");

        PaymentTermsEntity savedEntity = new PaymentTermsEntity();
        savedEntity.setActive(true);
        savedEntity.setPostedBy("user1");
        savedEntity.setPostedByUsername("username1");
        savedEntity.setPostedDate(LocalDateTime.now());

        when(paymentTermsRepository.save(any(PaymentTermsEntity.class))).thenReturn(savedEntity);

        PaymentTermsEntity result = paymentTermsService.addPaymentTerm(entity);

        assertNotNull(result);
        assertEquals(true, result.getActive());
        assertEquals("user1", result.getPostedBy());
        assertEquals("username1", result.getPostedByUsername());
        assertNotNull(result.getPostedDate());
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

        PaymentTermsEntity savedEntity = new PaymentTermsEntity();
        savedEntity.setActive(true);
        savedEntity.setPostedBy("user2");
        savedEntity.setPostedByUsername("username2");
        savedEntity.setPostedDate(LocalDateTime.now());

        when(paymentTermsRepository.save(any(PaymentTermsEntity.class))).thenReturn(savedEntity);

        PaymentTermsEntity result = paymentTermsService.addPaymentTerm(entity);

        assertNotNull(result.getPostedDate());
        verify(paymentTermsRepository).save(any(PaymentTermsEntity.class));
    }
}