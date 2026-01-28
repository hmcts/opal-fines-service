package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
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
import uk.gov.hmcts.opal.service.UserStateService;

@ExtendWith(MockitoExtension.class)
public class OpalDefendantAccountServiceAddPaymentTermsTest {
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
            .postedDate(LocalDateTime.now())
            .postedBy(postedBy)
            .postedByUsername(postedBy)
            .build();

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

        when(resultService.getResultById("55")).thenReturn(resultEntityLite);

        // Act
        defendantAccountService.addPaymentTerms(defendantAccountId, businessUnitId, "tester",
            ifMatch, postedBy, request);

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
        final String authHeader = "Bearer token";

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
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
        defendantAccountService.addPaymentTerms(defendantAccountId, businessUnitId, businessUnitUserId, ifMatch,
            authHeader, request);

        // Assert
        verify(paymentTermsService).addPaymentTerm(argThat(entity ->
            businessUnitUserId.equals(entity.getPostedBy())
                && businessUnitUserId.equals(entity.getPostedByUsername())
        ));
    }
}
