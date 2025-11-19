package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.mapper.request.PaymentTermsMapper;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.ReportEntryRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;

@ExtendWith(MockitoExtension.class)
public class OpalDefendantAccountServiceTestAddPaymentTermsTest {
    @Mock
    private DefendantAccountPaymentTermsRepository paymentTermsRepository;

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private EnforcementRepository enforcementRepository;

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private ReportEntryRepository reportEntryRepository;

    // other dependencies the service needs (audit, userState, mappers etc.)
    @Mock private AmendmentService amendmentService;
    @Mock private PaymentTermsService paymentTermsService;
    @Mock private ResultService resultService;
    @Mock private ReportEntryService reportEntryService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private OpalDefendantAccountService defendantAccountService;

    @Captor
    private ArgumentCaptor<DefendantAccountEntity> accountCaptor;

    @Mock
    private PaymentTermsMapper paymentTermsMapper;

    @Test
    void testAddPaymentTerms_clearsLastEnforcementAndCreatesReportEntry_Happy() {
        // Arrange
        final Long defendantAccountId = 77L;
        final String businessUnitId = "10";
        final String ifMatch = "\"1\"";
        final String postedBy = "tester";

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10)
            .build();

        DefendantAccountEntity account = new DefendantAccountEntity();
        account.setDefendantAccountId(defendantAccountId);
        account.setBusinessUnit(bu);
        account.setLastEnforcement(String.valueOf(55L));
        account.setVersionNumber(1L);

        // Request DTO (minimal)
        PaymentTerms paymentTermsDto = new PaymentTerms();
        AddDefendantAccountPaymentTermsRequest request = new AddDefendantAccountPaymentTermsRequest();
        request.setPaymentTerms(paymentTermsDto);

        // Mock account lookup
        when(defendantAccountRepository.findById(defendantAccountId)).thenReturn(Optional.of(account));

        PaymentTermsEntity paymentTermsReturned = new PaymentTermsEntity();
        paymentTermsReturned.setPaymentTermsId(200L);
        paymentTermsReturned.setActive(Boolean.TRUE);

        when(paymentTermsService.addPaymentTerm(any(PaymentTermsEntity.class), any(String.class)))
            .thenReturn(paymentTermsReturned);

        PaymentTermsEntity paymentTerms = new PaymentTermsEntity();
        paymentTerms.setActive(Boolean.TRUE);
        paymentTerms.setPostedDate(LocalDate.from(LocalDateTime.now()));
        paymentTerms.setPostedBy(postedBy);
        paymentTerms.setPostedByUsername(postedBy);
        when(paymentTermsMapper.toEntity(any(PaymentTerms.class))).thenReturn(paymentTerms);

        EnforcementEntity.Lite enforcementLite = new EnforcementEntity.Lite();
        enforcementLite.setEnforcementId(300L);
        enforcementLite.setDefendantAccountId(defendantAccountId);
        enforcementLite.setPostedDate(LocalDateTime.now());
        enforcementLite.setPostedBy("enf_tester");
        enforcementLite.setResultId("55");

        when(enforcementRepository.findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(any(Long.class),
            any(String.class))).thenReturn(Optional.of(enforcementLite));

        ResultEntity.Lite resultEntityLite = new ResultEntity.Lite();
        resultEntityLite.setResultId(String.valueOf(55L));
        resultEntityLite.setExtendTtpPreserveLastEnf(Boolean.FALSE);
        //when(resultRepository.findById(Long.valueOf("55"))).thenReturn(Optional.of(resultEntityLite));

        when(resultService.getLiteResultById("55")).thenReturn(resultEntityLite);

        // Act
        defendantAccountService.addPaymentTerms(defendantAccountId, businessUnitId, ifMatch, postedBy, request);

        // Assert
        // 1) Verify PaymentTermsService.addPaymentTerm was called
        verify(paymentTermsService).addPaymentTerm(any(PaymentTermsEntity.class), any(String.class));
        // 2) Verify that defendantAccountRepository.save was called to update lastEnforcement
        verify(defendantAccountRepository).save(accountCaptor.capture());
        DefendantAccountEntity savedAccount = accountCaptor.getValue();
        assertNotNull(savedAccount);
        assertNull(savedAccount.getLastEnforcement(), "Expected lastEnforcement to be cleared to null");
        // 3) Verify that reportEntryService.createExtendTtpReportEntry was called to create report entry
        verify(reportEntryService).createExtendTtpReportEntry(any(Long.class), any(short.class));

    }
}
