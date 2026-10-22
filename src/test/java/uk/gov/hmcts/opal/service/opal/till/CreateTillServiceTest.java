package uk.gov.hmcts.opal.service.opal.till;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.util.SecurityUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.PaymentReceivedFrom;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.TransferSource;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.TillsCreateAdditionalInformation;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentIn;
import uk.gov.hmcts.opal.generated.model.TillsCreatePayerType;
import uk.gov.hmcts.opal.generated.model.TillsCreateRequest;
import uk.gov.hmcts.opal.mapper.till.CreateTillMapper;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.report.PreAllocatedCashTillService;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class CreateTillServiceTest {

    private static final short BUSINESS_UNIT_ID = 10;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 14, 10, 30);

    @Mock
    private TillRepository tillRepository;

    @Mock
    private PaymentInRepository paymentInRepository;

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @Mock
    private PreAllocatedCashTillService preAllocatedCashTillService;

    @Mock
    private UserStateService userStateService;

    @Mock
    private CreateTillMapper createTillMapper;

    @Mock
    private OpalJwtAuthenticationToken authToken;

    @Mock
    private UserStateV2 userState;

    private MockedStatic<SecurityUtil> securityUtil;
    private CreateTillService service;

    @BeforeEach
    void setUp() {
        securityUtil = mockStatic(SecurityUtil.class);
        service = new CreateTillService(
            tillRepository,
            paymentInRepository,
            businessUnitRepository,
            preAllocatedCashTillService,
            userStateService,
            createTillMapper,
            new ObjectMapper(),
            Clock.fixed(Instant.parse("2026-09-14T10:30:00Z"), ZoneOffset.UTC));
    }

    @AfterEach
    void tearDown() {
        securityUtil.close();
    }

    @Test
    void createTill_persistsManualTillPaymentsAndPreAllocatedReport() {
        // Arrange
        final BusinessUnitEntity businessUnit = BusinessUnitEntity.builder().businessUnitId(BUSINESS_UNIT_ID).build();
        final TillEntity persistedTill = TillEntity.builder().tillId(123L).businessUnit(businessUnit).build();
        final TillsCreateRequest request = request(
            TillsCreateAdditionalInformation.builder()
                .payerType(TillsCreatePayerType.INDIVIDUAL)
                .paymentReference("reference-123")
                .build(),
            finesPayment(99L),
            finesPayment(100L));
        final ArgumentCaptor<List<PaymentInEntity>> paymentsCaptor = ArgumentCaptor.captor();
        final ArgumentCaptor<String> additionalInformationCaptor = ArgumentCaptor.forClass(String.class);

        givenPermission();
        givenUserDetails();
        when(businessUnitRepository.findById(BUSINESS_UNIT_ID)).thenReturn(Optional.of(businessUnit));
        when(tillRepository.getNextTillNumber(BUSINESS_UNIT_ID)).thenReturn(42L);
        when(createTillMapper.toTillEntity(eq(businessUnit), eq("7"), eq("Test User"), eq((short) 42),
            eq(new BigDecimal("25.00")), eq((short) 2), eq(NOW))).thenReturn(persistedTill);
        when(tillRepository.save(persistedTill)).thenReturn(persistedTill);
        when(createTillMapper.toPaymentIn(any(), eq(persistedTill), eq(NOW), anyString(), any(), any()))
            .thenReturn(PaymentInEntity.builder().build());
        when(createTillMapper.toPaymentReceivedFrom(
            TillsCreatePaymentDetails.AdditionalInformationEnum.DEFENDANT)).thenReturn(PaymentReceivedFrom.DEFENDANT);

        // Act
        service.createTill(request);

        // Assert
        verify(createTillMapper).toTillEntity(businessUnit, "7", "Test User", (short) 42,
            new BigDecimal("25.00"), (short) 2, NOW);
        verify(tillRepository).save(persistedTill);

        verify(paymentInRepository).saveAll(paymentsCaptor.capture());
        List<PaymentInEntity> payments = paymentsCaptor.getValue();
        assertThat(payments).hasSize(2);
        verify(createTillMapper, times(2)).toPaymentIn(any(), eq(persistedTill), eq(NOW),
            additionalInformationCaptor.capture(), any(), any());
        assertThat(additionalInformationCaptor.getAllValues().getFirst())
            .contains("\"payment_received_from\":\"D\"")
            .contains("\"additional_information\"")
            .contains("\"payment_reference\":\"reference-123\"")
            .doesNotContain("\"transfer_source\"");

        verify(preAllocatedCashTillService).createPreAllocatedReportInstance(
            persistedTill.getTillId(), 7L, "Test User");
    }

    @Test
    void createTill_storesTransferSourceCodeWithoutChangingTheRequest() {
        // Arrange
        final BusinessUnitEntity businessUnit = BusinessUnitEntity.builder().businessUnitId(BUSINESS_UNIT_ID).build();
        final TillEntity persistedTill = TillEntity.builder().tillId(123L).businessUnit(businessUnit).build();
        final TillsCreateAdditionalInformation additionalInformation = TillsCreateAdditionalInformation.builder()
            .payerType(TillsCreatePayerType.INDIVIDUAL)
            .transferSource(TillsCreateAdditionalInformation.TransferSourceEnum.NAT_WEST)
            .build();
        final ArgumentCaptor<String> additionalInformationCaptor = ArgumentCaptor.forClass(String.class);

        givenPermission();
        givenUserDetails();
        when(businessUnitRepository.findById(BUSINESS_UNIT_ID)).thenReturn(Optional.of(businessUnit));
        when(tillRepository.getNextTillNumber(BUSINESS_UNIT_ID)).thenReturn(42L);
        when(createTillMapper.toTillEntity(any(), anyString(), anyString(), any(), any(), any(), any()))
            .thenReturn(persistedTill);
        when(tillRepository.save(persistedTill)).thenReturn(persistedTill);
        when(createTillMapper.toPaymentIn(any(), eq(persistedTill), eq(NOW), anyString(), any(), any()))
            .thenReturn(PaymentInEntity.builder().build());
        when(createTillMapper.toPaymentReceivedFrom(
            TillsCreatePaymentDetails.AdditionalInformationEnum.DEFENDANT)).thenReturn(PaymentReceivedFrom.DEFENDANT);
        when(createTillMapper.toTransferSource(TillsCreateAdditionalInformation.TransferSourceEnum.NAT_WEST))
            .thenReturn(TransferSource.NAT_WEST);

        // Act
        service.createTill(request(additionalInformation, finesPayment(99L)));

        // Assert
        verify(createTillMapper).toPaymentIn(any(), eq(persistedTill), eq(NOW),
            additionalInformationCaptor.capture(), any(), any());
        assertThat(additionalInformationCaptor.getValue())
            .contains("\"transfer_source\":\"NATWEST\"")
            .doesNotContain("\"transfer_source\":\"NatWest\"");
        assertThat(additionalInformation.getTransferSource())
            .isEqualTo(TillsCreateAdditionalInformation.TransferSourceEnum.NAT_WEST);
    }

    @ParameterizedTest
    @EnumSource(value = TillsCreatePaymentDetails.DestinationTypeEnum.class, names = {"SUSPENSE", "COURT_FEE"})
    void createTill_rejectsOutOfScopePaymentDestination(
        TillsCreatePaymentDetails.DestinationTypeEnum destinationType) {
        final TillsCreateRequest request = request(payment(destinationType, null));

        givenPermission();

        assertThatThrownBy(() -> service.createTill(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Only fines payments are supported");

        verifyNoPersistence();
    }

    @Test
    void createTill_rejectsFinesPaymentWithoutDefendantAccount() {
        final TillsCreateRequest request = request(finesPayment(null));

        givenPermission();

        assertThatThrownBy(() -> service.createTill(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("defendant_account_id is required");

        verifyNoPersistence();
    }

    @Test
    void createTill_rejectsMorePaymentsThanCanBeStoredInTillCount() {
        TillsCreatePaymentIn payment = finesPayment(99L);
        TillsCreateRequest request = TillsCreateRequest.builder()
            .businessUnitId(BUSINESS_UNIT_ID)
            .paymentsIn(Collections.nCopies(Short.MAX_VALUE + 1, payment))
            .build();

        givenPermission();

        assertThatThrownBy(() -> service.createTill(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("A till cannot contain more than " + Short.MAX_VALUE + " payments");

        verifyNoPersistence();
    }

    private void verifyNoPersistence() {
        verify(tillRepository, never()).getNextTillNumber(any());
        verify(tillRepository, never()).save(any());
        verify(paymentInRepository, never()).saveAll(any());
        verify(preAllocatedCashTillService, never()).createPreAllocatedReportInstance(any(), any(), any());
    }

    private void givenPermission() {
        securityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(authToken);
        when(authToken.hasPermissionInBusinessUnit(FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS, BUSINESS_UNIT_ID))
            .thenReturn(true);
    }

    private void givenUserDetails() {
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        when(userState.getUserId()).thenReturn(7L);
        when(userState.getUsername()).thenReturn("Test User");
    }

    private static TillsCreateRequest request(TillsCreatePaymentIn... payments) {
        return request(null, payments);
    }

    private static TillsCreateRequest request(TillsCreateAdditionalInformation additionalInformation,
                                              TillsCreatePaymentIn... payments) {
        return TillsCreateRequest.builder()
            .businessUnitId(BUSINESS_UNIT_ID)
            .paymentsIn(List.of(payments))
            .additionalInformation(additionalInformation)
            .build();
    }

    private static TillsCreatePaymentIn finesPayment(Long defendantAccountId) {
        return payment(TillsCreatePaymentDetails.DestinationTypeEnum.FINES, defendantAccountId);
    }

    private static TillsCreatePaymentIn payment(TillsCreatePaymentDetails.DestinationTypeEnum destinationType,
                                                Long defendantAccountId) {
        return TillsCreatePaymentIn.builder()
            .defendantAccountId(defendantAccountId)
            .paymentDetails(TillsCreatePaymentDetails.builder()
                .amount(new BigDecimal("12.50"))
                .method(TillsCreatePaymentDetails.MethodEnum.NOTES_COINS)
                .destinationType(destinationType)
                .allocationType("FULL")
                .additionalInformation(TillsCreatePaymentDetails.AdditionalInformationEnum.DEFENDANT)
                .build())
            .build();
    }

}
