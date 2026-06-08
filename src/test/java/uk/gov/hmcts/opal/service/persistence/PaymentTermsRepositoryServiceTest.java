package uk.gov.hmcts.opal.service.persistence;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.paymentterms.TermsTypeCode;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;

@ExtendWith(MockitoExtension.class)
class PaymentTermsRepositoryServiceTest {

    @Mock
    private PaymentTermsRepository paymentTermsRepository;
    @InjectMocks
    private PaymentTermsRepositoryService paymentTermsRepositoryService;

    @Test
    void getPaymentTermsAsFormattedString_payByDate_formatsDate() {
        PaymentTermsEntity account = PaymentTermsEntity.builder()
            .effectiveDate(LocalDate.of(2000, 1, 2))
            .termsTypeCode(TermsTypeCode.BY_DATE)
            .build();
        when(paymentTermsRepository
            .findTopByDefendantAccount_DefendantAccountIdOrderByPostedDateDescPaymentTermsIdDesc(1L))
            .thenReturn(Optional.of(account));
        String actual = paymentTermsRepositoryService.getPaymentTermsAsFormattedString(1L);
        assertThat(actual).isEqualTo("02/01/2000");
    }

    @Test
    void getPaymentTermsAsFormattedString_payByInstalment_formatsInstalmentString() {
        PaymentTermsEntity account = PaymentTermsEntity.builder()
            .effectiveDate(LocalDate.of(2000, 1, 2))
            .instalmentAmount(new BigDecimal("20.00"))
            .instalmentPeriod(InstalmentPeriod.MONTH)
            .termsTypeCode(TermsTypeCode.INSTALMENTS)
            .build();
        when(paymentTermsRepository
            .findTopByDefendantAccount_DefendantAccountIdOrderByPostedDateDescPaymentTermsIdDesc(1L))
            .thenReturn(Optional.of(account));
        String actual = paymentTermsRepositoryService.getPaymentTermsAsFormattedString(1L);
        assertThat(actual).isEqualTo("20.00 per month from 02/01/2000");
    }

    @Test
    void getPaymentTermsAsFormattedString_payByInstalment_formatsInstalmentStringWithLumpSum() {
        PaymentTermsEntity account = PaymentTermsEntity.builder()
            .effectiveDate(LocalDate.of(2000, 1, 2))
            .instalmentAmount(new BigDecimal("20.00"))
            .instalmentPeriod(InstalmentPeriod.MONTH)
            .instalmentLumpSum(new BigDecimal("60.00"))
            .termsTypeCode(TermsTypeCode.INSTALMENTS)
            .build();
        when(paymentTermsRepository
            .findTopByDefendantAccount_DefendantAccountIdOrderByPostedDateDescPaymentTermsIdDesc(1L))
            .thenReturn(Optional.of(account));
        String actual = paymentTermsRepositoryService.getPaymentTermsAsFormattedString(1L);
        assertThat(actual).isEqualTo("20.00 per month from 02/01/2000 following a lump sum of 60.00");
    }

    @Test
    void getPaymentTermsAsFormattedString_alreadyPaid_returnsNull() {
        PaymentTermsEntity account = PaymentTermsEntity.builder()
            .termsTypeCode(TermsTypeCode.PAID)
            .build();
        when(paymentTermsRepository.findTopByDefendantAccount_DefendantAccountIdOrderByPostedDateDescPaymentTermsIdDesc(
            1L)).thenReturn(Optional.of(account));
        assertNull(paymentTermsRepositoryService.getPaymentTermsAsFormattedString(1L));
    }

    @Test
    void getPaymentTermsAsFormattedString_noAccountFound_returnsNull() {
        when(paymentTermsRepository.findTopByDefendantAccount_DefendantAccountIdOrderByPostedDateDescPaymentTermsIdDesc(
            1L)).thenReturn(Optional.empty());
        assertNull(paymentTermsRepositoryService.getPaymentTermsAsFormattedString(1L));
    }
}