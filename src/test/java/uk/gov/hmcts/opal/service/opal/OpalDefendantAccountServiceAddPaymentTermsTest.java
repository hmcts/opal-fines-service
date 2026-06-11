package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.mapper.request.PaymentTermsMapper;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.ReportEntryRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@ExtendWith(MockitoExtension.class)
public class OpalDefendantAccountServiceAddPaymentTermsTest {
    private static final LocalDateTime TEST_POSTED_DATE = LocalDateTime.of(2026, Month.JUNE, 11, 10, 0);

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
    @Mock
    private AmendmentService amendmentService;
    @Mock
    private PaymentTermsService paymentTermsService;
    @Mock
    private ResultService resultService;
    @Mock
    private ReportEntryService reportEntryService;
    @Mock
    private UserStateService userStateService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private OpalDefendantAccountService defendantAccountService;

    @Captor
    private ArgumentCaptor<DefendantAccountEntity> accountCaptor;

    @Mock
    private BusinessUnitUser buUser;

    @Mock
    private UserState userState;

    @Mock
    private PaymentTermsMapper paymentTermsMapper;

    @Test
    void testAddPaymentTerms_clearsLastEnforcementAndCreatesReportEntry_Happy() {
        // Arrange
        final Long defendantAccountId = 77L;
        final String businessUnitId = "10";
        final String ifMatch = "\"1\"";
        final String postedBy = "tester";

        BusinessUnitEntity bu = BusinessUnitEntity.builder()
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
        when(defendantAccountRepository.findByDefendantAccountIdForUpdate(defendantAccountId))
            .thenReturn(Optional.of(account));

        PaymentTermsEntity paymentTermsReturned = PaymentTermsEntity.builder()
            .paymentTermsId(200L)
            .active(Boolean.TRUE)
            .defendantAccount(account)  // ensure builder can access the account and its version
            .extension(Boolean.TRUE)
            .build();

        when(paymentTermsService.addPaymentTerm(any(PaymentTermsEntity.class)))
            .thenReturn(paymentTermsReturned);

        PaymentTermsEntity paymentTerms = PaymentTermsEntity.builder()
            .active(Boolean.TRUE)
            .postedDate(TEST_POSTED_DATE)
            .postedBy(postedBy)
            .postedByUsername(postedBy)
            .build();

        when(paymentTermsMapper.toEntity(any(PaymentTerms.class))).thenReturn(paymentTerms);

        EnforcementEntity enforcementLite = new EnforcementEntity();
        enforcementLite.setEnforcementId(300L);
        enforcementLite.setDefendantAccountId(defendantAccountId);
        enforcementLite.setPostedDate(TEST_POSTED_DATE);
        enforcementLite.setPostedBy("enf_tester");
        enforcementLite.setResultId("55");

        when(enforcementRepository.findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(any(Long.class),
            any(String.class))).thenReturn(Optional.of(enforcementLite));

        ResultEntity resultEntityLite = new ResultEntity();
        resultEntityLite.setResultId(String.valueOf(55L));
        resultEntityLite.setExtendTtpPreserveLastEnf(Boolean.FALSE);
        //when(resultRepository.findById(Long.valueOf("55"))).thenReturn(Optional.of(resultEntityLite));

        when(resultService.getResultById("55")).thenReturn(resultEntityLite);

        // Act
        defendantAccountService.addPaymentTerms(defendantAccountId, businessUnitId, "tester",
            ifMatch, request);

        // Assert
        // 1) Verify PaymentTermsService.addPaymentTerm was called
        verify(paymentTermsService).addPaymentTerm(any(PaymentTermsEntity.class));
        // 2) Verify that defendantAccountRepository.save was called to update lastEnforcement
        verify(defendantAccountRepository).save(accountCaptor.capture());
        DefendantAccountEntity savedAccount = accountCaptor.getValue();
        assertNotNull(savedAccount);
        assertNull(savedAccount.getLastEnforcement(), "Expected lastEnforcement to be cleared to null");
        // 3) Verify that reportEntryService.createExtendTtpReportEntry was called to create report entry
        verify(reportEntryService).createExtendTtpReportEntry(any(Long.class), any(short.class));
    }

    @Test
    void addPaymentTerms_setsPostedByFields_whenNull() {
        // Arrange
        final Long defendantAccountId = 1L;
        final String businessUnitId = "10";
        final String businessUnitUserId = "userX";
        final String ifMatch = "\"1\"";

        BusinessUnitEntity bu = BusinessUnitEntity.builder()
            .businessUnitId((short) 10)
            .build();

        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(defendantAccountId)
            .businessUnit(bu)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findByDefendantAccountIdForUpdate(defendantAccountId))
            .thenReturn(Optional.of(account));

        PaymentTerms paymentTermsDto = new PaymentTerms();
        AddDefendantAccountPaymentTermsRequest request = new AddDefendantAccountPaymentTermsRequest();
        request.setPaymentTerms(paymentTermsDto);

        PaymentTermsEntity paymentTermsEntity = PaymentTermsEntity.builder()
            .postedBy(null)
            .postedByUsername(null)
            .build();

        when(paymentTermsMapper.toEntity(any(PaymentTerms.class))).thenReturn(paymentTermsEntity);

        PaymentTermsEntity savedPaymentTermsEntity = PaymentTermsEntity.builder()
            .paymentTermsId(200L)
            .active(Boolean.TRUE)
            .defendantAccount(account)  // ensure builder can access the account and its version
            .postedBy(businessUnitUserId)
            .postedByUsername(businessUnitUserId)
            .extension(Boolean.TRUE)
            .build();

        when(paymentTermsService.addPaymentTerm(any(PaymentTermsEntity.class))).thenReturn(savedPaymentTermsEntity);

        // Act
        defendantAccountService.addPaymentTerms(
            defendantAccountId, businessUnitId, businessUnitUserId, ifMatch, request);

        // Assert
        verify(paymentTermsService).addPaymentTerm(argThat(entity ->
            businessUnitUserId.equals(entity.getPostedBy())
                && businessUnitUserId.equals(entity.getPostedByUsername())
        ));
    }

    @Test
    void addPaymentTerms_preservesLastEnforcement_whenRequestedByEnforcementFlow() {
        final Long defendantAccountId = 77L;
        final String businessUnitId = "10";
        final String ifMatch = "\"1\"";
        final String businessUnitUserId = "tester";

        BusinessUnitEntity bu = BusinessUnitEntity.builder()
            .businessUnitId((short) 10)
            .build();

        DefendantAccountEntity account = new DefendantAccountEntity();
        account.setDefendantAccountId(defendantAccountId);
        account.setBusinessUnit(bu);
        account.setLastEnforcement("55");
        account.setVersionNumber(1L);

        PaymentTerms paymentTermsDto = new PaymentTerms();
        AddDefendantAccountPaymentTermsRequest request = new AddDefendantAccountPaymentTermsRequest();
        request.setPaymentTerms(paymentTermsDto);

        when(defendantAccountRepository.findByDefendantAccountIdForUpdate(defendantAccountId))
            .thenReturn(Optional.of(account));

        PaymentTermsEntity paymentTerms = PaymentTermsEntity.builder()
            .active(Boolean.TRUE)
            .postedDate(TEST_POSTED_DATE)
            .postedBy(businessUnitUserId)
            .postedByUsername(businessUnitUserId)
            .build();

        when(paymentTermsMapper.toEntity(any(PaymentTerms.class))).thenReturn(paymentTerms);

        PaymentTermsEntity savedPaymentTermsEntity = PaymentTermsEntity.builder()
            .paymentTermsId(200L)
            .active(Boolean.TRUE)
            .defendantAccount(account)
            .extension(Boolean.TRUE)
            .build();

        when(paymentTermsService.addPaymentTerm(any(PaymentTermsEntity.class))).thenReturn(savedPaymentTermsEntity);

        defendantAccountService.addPaymentTermsPreservingLastEnforcement(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            ifMatch,
            businessUnitUserId,
            request
        );

        verify(defendantAccountRepository).save(accountCaptor.capture());
        DefendantAccountEntity savedAccount = accountCaptor.getValue();
        assertNotNull(savedAccount);
        assertEquals("55", savedAccount.getLastEnforcement(),
            "Expected lastEnforcement to be preserved for enforcement-driven payment terms");
        verify(reportEntryService).createExtendTtpReportEntry(any(Long.class), any(short.class));
    }
}
