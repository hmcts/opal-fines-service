package uk.gov.hmcts.opal.service.opal.till;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.opal.till.TillRequestTestData.payment;
import static uk.gov.hmcts.opal.service.opal.till.TillRequestTestData.request;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.common.util.SecurityUtil;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.TillsCreateAdditionalInformation;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentIn;
import uk.gov.hmcts.opal.generated.model.TillsCreateRequest;
import uk.gov.hmcts.opal.mapper.till.CreateTillMapper;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.CourtFeeRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.report.PreAllocatedCashTillService;

@ExtendWith(MockitoExtension.class)
class CreateTillServiceTest {

    @Mock private TillRepository tillRepository;
    @Mock private PaymentInRepository paymentInRepository;
    @Mock private BusinessUnitRepository businessUnitRepository;
    @Mock private DefendantAccountRepository defendantAccountRepository;
    @Mock private PreAllocatedCashTillService reportService;
    @Mock private UserStateService userStateService;
    @Mock private CreateTillPayerService payerService;
    @Mock private CourtFeeRepository courtFeeRepository;
    @Mock private OpalJwtAuthenticationToken authToken;
    @Mock private UserStateV2 userState;
    private MockedStatic<SecurityUtil> securityUtil;
    private CreateTillService service;
    private final BusinessUnitEntity businessUnit = BusinessUnitEntity.builder().businessUnitId((short) 10).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        securityUtil = mockStatic(SecurityUtil.class);
        securityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(authToken);
        when(authToken.hasPermissionInBusinessUnit(FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS, (short) 10))
            .thenReturn(true);
        service = new CreateTillService(tillRepository, paymentInRepository, businessUnitRepository, reportService,
            userStateService, Mappers.getMapper(CreateTillMapper.class), objectMapper,
            Clock.fixed(Instant.parse("2026-09-14T10:30:00Z"), ZoneOffset.UTC),
            defendantAccountRepository, payerService,
            new CreateTillCourtFeeService(courtFeeRepository, Mappers.getMapper(CreateTillMapper.class), objectMapper));
    }

    @AfterEach
    void tearDown() {
        securityUtil.close();
    }

    @ParameterizedTest
    @CsvSource({"F,", "C,", "S,OF", "S,FA", "S,MS", "S,UN"})
    void persistsScenarioWithCorrectAssociation(String destination, String reason) {
        TillsCreatePaymentIn payment = payment(destination, reason);
        givenContext();
        if (destination.equals("C")) {
            givenCourtFee("10.00");
        }
        if (destination.equals("F")) {
            givenAccount((short) 10);
        } else {
            when(payerService.createPayerAccount(payment.getAdditionalInformation(), businessUnit))
                .thenReturn(reason != null && reason.equals("UN") ? null
                    : MiscellaneousAccountEntity.builder().miscellaneousAccountId(500L).build());
        }

        service.createTill(request(payment));

        PaymentInEntity stored = capturedPayment();
        assertThat(stored.getPaymentAmount()).isEqualByComparingTo("12.50");
        assertThat(stored.getDestinationType().name()).isEqualTo(destination);
        assertThat(stored.getPaymentDate()).isEqualTo(LocalDateTime.of(2026, 9, 14, 10, 30));
        if (destination.equals("F")) {
            assertThat(stored.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.DEFENDANT_ACCOUNTS);
            assertThat(stored.getAssociatedRecordId()).isEqualTo("99");
            verifyNoInteractions(payerService);
        } else if ("UN".equals(reason)) {
            assertThat(stored.getAssociatedRecordType()).isNull();
            assertThat(stored.getAssociatedRecordId()).isNull();
        } else {
            assertThat(stored.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.MISCELLANEOUS_ACCOUNTS);
            assertThat(stored.getAssociatedRecordId()).isEqualTo("500");
        }
        assertThat(stored.getAdditionalInformation()).doesNotContain("payer_type", "payer_details", "Smith");
        verify(reportService).createPreAllocatedReportInstance(123L, 7L, "Test User");
    }

    @Test
    void persistsThirdPartyAndOptionalMetadataWithoutInventingPayer() {
        TillsCreatePaymentIn payment = payment("F", null);
        TillsCreateAdditionalInformation information = payment.getAdditionalInformation();
        information.setPaymentReceivedFrom(TillsCreateAdditionalInformation.PaymentReceivedFromEnum.T);
        information.setThirdPartyPayerName("Third party");
        information.setPaymentReference("");
        information.setTransferSource(TillsCreateAdditionalInformation.TransferSourceEnum.NATWEST);
        givenContext();
        givenAccount((short) 10);

        service.createTill(request(payment));

        PaymentInEntity stored = capturedPayment();
        assertThat(stored.getThirdPartyPayerName()).isEqualTo("Third party");
        assertThat(objectMapper.readTree(stored.getAdditionalInformation()))
            .isEqualTo(objectMapper.readTree("""
                {"payment_received_from":"T","payment_reference":"","transfer_source":"NATWEST"}
                """));
        verifyNoInteractions(payerService);
    }

    @ParameterizedTest
    @CsvSource({"C,", "S,UN"})
    void retainsAllocationMetadata(String destination, String reason) {
        TillsCreatePaymentIn payment = payment(destination, reason);
        payment.getAdditionalInformation().setPaymentReference("reference");
        if (destination.equals("C")) {
            givenCourtFee("10.00");
        }
        if ("UN".equals(reason)) {
            payment.getAdditionalInformation().setSuspenseReasonDetail("Unidentified receipt");
        }
        givenContext();
        when(payerService.createPayerAccount(payment.getAdditionalInformation(), businessUnit)).thenReturn(null);
        service.createTill(request(payment));
        String json = capturedPayment().getAdditionalInformation();
        assertThat(json).contains("\"payment_reference\":\"reference\"", "\"suspense_note\":\"Payment note\"");
        if (destination.equals("C")) {
            assertThat(json).contains("\"court_fee_code\":\"FEE\"", "\"number_of_items\":2");
        } else {
            assertThat(json).contains("\"suspense_reason_code\":\"UN\"", "Unidentified receipt");
        }
    }

    @Test
    void createsTwoCourtFeePaymentsWithOnePayer() {
        TillsCreatePaymentIn payment = payment("C", null);
        givenContext();
        givenCourtFee("5.00");
        when(payerService.createPayerAccount(payment.getAdditionalInformation(), businessUnit))
            .thenReturn(MiscellaneousAccountEntity.builder().miscellaneousAccountId(500L).build());

        service.createTill(request(payment));

        ArgumentCaptor<List<PaymentInEntity>> captured = ArgumentCaptor.captor();
        verify(paymentInRepository).saveAll(captured.capture());
        List<PaymentInEntity> payments = captured.getValue();
        assertThat(payments).hasSize(2).allSatisfy(stored -> {
            assertThat(stored.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.MISCELLANEOUS_ACCOUNTS);
            assertThat(stored.getAssociatedRecordId()).isEqualTo("500");
        });
        assertThat(payments.get(0).getPaymentAmount()).isEqualByComparingTo("10.00");
        assertThat(payments.get(1).getPaymentAmount()).isEqualByComparingTo("2.50");
        assertThat(payments.getFirst().getTillEntity().getTotalAmount()).isEqualByComparingTo("12.50");
        assertThat(payments.getFirst().getTillEntity().getPaymentsCount()).isEqualTo((short) 2);
        verify(payerService).createPayerAccount(payment.getAdditionalInformation(), businessUnit);
    }

    @Test
    void rejectsInvalidBatchBeforePersistence() {
        TillsCreatePaymentIn invalid = payment("F", null);
        invalid.setDefendantAccountId(null);
        assertThatThrownBy(() -> service.createTill(request(payment("C", null), invalid)))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining("defendant_account_id");
        verifyNoPersistence();
    }

    @Test
    void rejectsMissingDefendantBeforePersistence() {
        when(defendantAccountRepository.findAllById(any())).thenReturn(List.of());
        assertThatThrownBy(() -> service.createTill(request(payment("F", null))))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining("existing account");
        verifyNoPersistence();
    }

    @Test
    void rejectsDefendantInAnotherBusinessUnit() {
        givenAccount((short) 11);
        assertThatThrownBy(() -> service.createTill(request(payment("F", null))))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining("business_unit_id 10");
        verifyNoPersistence();
    }

    @Test
    void rejectsPaymentCountOverflow() {
        TillsCreateRequest request = TillsCreateRequest.builder().businessUnitId((short) 10)
            .paymentsIn(Collections.nCopies(Short.MAX_VALUE + 1, payment("F", null))).build();
        assertThatThrownBy(() -> service.createTill(request)).isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("cannot contain more than");
        verifyNoPersistence();
    }

    @Test
    void rejectsPaymentCountOverflowAfterCourtFeeSplitting() {
        final TillsCreatePaymentIn payment = payment("C", null);
        when(businessUnitRepository.findById((short) 10)).thenReturn(Optional.of(businessUnit));
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        when(userState.getUserId()).thenReturn(7L);
        when(userState.getUsername()).thenReturn("Test User");
        when(tillRepository.getNextTillNumber((short) 10)).thenReturn(42L);
        givenCourtFee("5.00");
        when(payerService.createPayerAccount(payment.getAdditionalInformation(), businessUnit))
            .thenReturn(MiscellaneousAccountEntity.builder().miscellaneousAccountId(500L).build());
        TillsCreateRequest request = TillsCreateRequest.builder().businessUnitId((short) 10)
            .paymentsIn(Collections.nCopies(Short.MAX_VALUE / 2 + 1, payment)).build();

        assertThatThrownBy(() -> service.createTill(request)).isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("cannot contain more than");
        verify(tillRepository, never()).save(any());
        verifyNoInteractions(paymentInRepository, reportService);
    }

    private PaymentInEntity capturedPayment() {
        ArgumentCaptor<List<PaymentInEntity>> payments = ArgumentCaptor.captor();
        verify(paymentInRepository).saveAll(payments.capture());
        return payments.getValue().getFirst();
    }

    private void givenContext() {
        when(businessUnitRepository.findById((short) 10)).thenReturn(Optional.of(businessUnit));
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        when(userState.getUserId()).thenReturn(7L);
        when(userState.getUsername()).thenReturn("Test User");
        when(tillRepository.getNextTillNumber((short) 10)).thenReturn(42L);
        when(tillRepository.save(any())).thenAnswer(invocation -> {
            TillEntity till = invocation.getArgument(0);
            till.setTillId(123L);
            return till;
        });
    }

    private void givenCourtFee(String amount) {
        when(courtFeeRepository.findByBusinessUnit_BusinessUnitIdAndCourtFeeCode((short) 10, "FEE"))
            .thenReturn(Optional.of(CourtFeeEntity.builder().amount(new BigDecimal(amount)).build()));
    }

    private void givenAccount(short businessUnitId) {
        when(defendantAccountRepository.findAllById(any())).thenReturn(List.of(DefendantAccountEntity.builder()
            .defendantAccountId(99L).businessUnit(BusinessUnitEntity.builder().businessUnitId(businessUnitId).build())
            .build()));
    }

    private void verifyNoPersistence() {
        verify(tillRepository, never()).getNextTillNumber(any());
        verify(tillRepository, never()).save(any());
        verifyNoInteractions(paymentInRepository, payerService, reportService);
    }
}
