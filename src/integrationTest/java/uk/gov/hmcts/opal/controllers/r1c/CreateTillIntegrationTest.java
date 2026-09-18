package uk.gov.hmcts.opal.controllers.r1c;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.DestinationType;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.PaymentMethod;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.TillStatusEnum;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.report.PreAllocatedCashTillService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1c-payment=true")
@DisplayName("POST /tills integration tests")
@Tag("ExtendedTest")
class CreateTillIntegrationTest extends AbstractIntegrationWithSecurityTest {

    @MockitoBean
    private PreAllocatedCashTillService preAllocatedCashTillService;

    @Autowired
    private TillRepository tillRepository;

    @Autowired
    private PaymentInRepository paymentInRepository;

    private static final String URL = "/tills";
    private static final short BUSINESS_UNIT_ID = 10;
    private static final String VALID_PAYMENT_DETAILS = """
        {
          "amount": 12.34,
          "method": "Notes & Coins",
          "destination_type": "Fines",
          "allocation_type": "FULL",
          "additional_information": "Third Party",
          "third_party_payer_name": "Test third-party payer"
        }
        """;
    private static final String VALID_REQUEST = """
        {
          "business_unit_id": 10,
          "additional_information": {
            "payer_type": "Individual",
            "transfer_source": "NatWest"
          },
          "payments_in": [
            {
              "defendant_account_id": 123,
              "payment_details": %s
            }
          ]
        }
        """.formatted(VALID_PAYMENT_DETAILS);

    @Test
    @JiraStory("PO-3630")
    @JiraEpic("PO-2439")
    void postTills_rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @JiraStory("PO-3630")
    @JiraEpic("PO-2439")
    void postTills_rejectsRequestWithoutBusinessUnitId() throws Exception {
        mockMvc.perform(post(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"payments_in\":[]}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @JiraStory("PO-3630")
    @JiraEpic("PO-2439")
    void postTills_rejectsEmptyPayments() throws Exception {
        mockMvc.perform(post(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"business_unit_id\":3630,\"payments_in\":[]}"))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("invalidRequestBodies")
    @JiraStory("PO-3630")
    @JiraEpic("PO-2439")
    void postTills_rejectsInvalidRequest(String request) throws Exception {
        mockMvc.perform(post(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    @JiraStory("PO-3630")
    @JiraEpic("PO-2439")
    void postTills_rejectsCallerWithoutBusinessUnitPermission() throws Exception {
        userStateStub.setupWithNoPermissions();

        mockMvc.perform(post(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
            .andExpect(status().isForbidden());
    }

    @Test
    @JiraStory("PO-3630")
    @JiraEpic("PO-2439")
    void postTills_createsTillForAuthorisedFinesPayment() throws Exception {
        // Arrange
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(BUSINESS_UNIT_ID, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        long tillsBefore = tillRepository.count();
        long paymentsBefore = paymentInRepository.count();

        // Act
        mockMvc.perform(post(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
            .andExpect(status().isCreated());

        // Assert
        ArgumentCaptor<Long> tillIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(preAllocatedCashTillService).createPreAllocatedReportInstance(
            tillIdCaptor.capture(), anyLong(), anyString());

        Long tillId = tillIdCaptor.getValue();
        TillEntity till = tillRepository.findById(tillId).orElseThrow();
        List<PaymentInEntity> payments =
            paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(tillId);

        assertThat(tillRepository.count()).isEqualTo(tillsBefore + 1);
        assertThat(paymentInRepository.count()).isEqualTo(paymentsBefore + 1);
        assertThat(till.getBusinessUnit().getBusinessUnitId()).isEqualTo(BUSINESS_UNIT_ID);
        assertThat(till.getStatus()).isEqualTo(TillStatusEnum.Created);
        assertThat(till.getTotalAmount()).isEqualByComparingTo("12.34");
        assertThat(till.getPaymentsCount()).isEqualTo((short) 1);
        assertThat(till.isAutoPayment()).isFalse();

        assertThat(payments).singleElement().satisfies(payment -> {
            assertThat(payment.getTillEntity().getTillId()).isEqualTo(tillId);
            assertThat(payment.getPaymentAmount()).isEqualByComparingTo("12.34");
            assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.NC);
            assertThat(payment.getDestinationType()).isEqualTo(DestinationType.F);
            assertThat(payment.getAllocationType()).isEqualTo("FULL");
            assertThat(payment.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.DEFENDANT_ACCOUNTS);
            assertThat(payment.getAssociatedRecordId()).isEqualTo("123");
            assertThat(payment.getThirdPartyPayerName()).isEqualTo("Test third-party payer");
            assertThat(payment.getAdditionalInformation())
                .contains("\"payment_received_from\":\"T\"")
                .contains("\"transfer_source\":\"NATWEST\"");
        });
    }

    @Test
    @JiraStory("PO-3630")
    @JiraEpic("PO-2439")
    void postTills_rollsBackTillAndPaymentsWhenPreAllocatedReportCreationFails() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(BUSINESS_UNIT_ID, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        long tillsBefore = tillRepository.count();
        long paymentsBefore = paymentInRepository.count();
        when(preAllocatedCashTillService.createPreAllocatedReportInstance(anyLong(), anyLong(), anyString()))
            .thenThrow(new ReportGenerationException("Report creation failed", new RuntimeException()));

        mockMvc.perform(post(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Report Generation Failed"))
            .andExpect(jsonPath("$.detail").value("Unable to generate the requested report"))
            .andExpect(jsonPath("$.retriable").value(true));

        assertThat(tillRepository.count()).isEqualTo(tillsBefore);
        assertThat(paymentInRepository.count()).isEqualTo(paymentsBefore);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Suspense", "Court Fee"})
    @JiraStory("PO-3630")
    @JiraEpic("PO-2439")
    void postTills_rejectsOutOfScopePaymentDestination(String destinationType) throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(BUSINESS_UNIT_ID, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        long tillsBefore = tillRepository.count();
        long paymentsBefore = paymentInRepository.count();

        mockMvc.perform(post(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST.replace("\"Fines\"", "\"" + destinationType + "\"")))
            .andExpect(status().isBadRequest());

        assertThat(tillRepository.count()).isEqualTo(tillsBefore);
        assertThat(paymentInRepository.count()).isEqualTo(paymentsBefore);
        verifyNoInteractions(preAllocatedCashTillService);
    }

    @Test
    @JiraStory("PO-3630")
    @JiraEpic("PO-2439")
    void postTills_rejectsFinesPaymentWithoutDefendantAccount() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(BUSINESS_UNIT_ID, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        long tillsBefore = tillRepository.count();
        long paymentsBefore = paymentInRepository.count();

        mockMvc.perform(post(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST.replace("\"defendant_account_id\": 123,", "")))
            .andExpect(status().isBadRequest());

        assertThat(tillRepository.count()).isEqualTo(tillsBefore);
        assertThat(paymentInRepository.count()).isEqualTo(paymentsBefore);
        verifyNoInteractions(preAllocatedCashTillService);
    }

    static Stream<String> invalidRequestBodies() {
        return Stream.of(
            "{\"business_unit_id\":3630}",
            "{\"business_unit_id\":3630,\"payments_in\":[{}]}",
            requestWithPaymentDetails("""
                {"method":"Notes & Coins","destination_type":"Fines","allocation_type":"FULL",
                 "additional_information":"Third Party"}
                """),
            requestWithPaymentDetails("""
                {"amount":12.34,"destination_type":"Fines","allocation_type":"FULL",
                 "additional_information":"Third Party"}
                """),
            requestWithPaymentDetails("""
                {"amount":12.34,"method":"Notes & Coins","allocation_type":"FULL",
                 "additional_information":"Third Party"}
                """),
            requestWithPaymentDetails("""
                {"amount":12.34,"method":"Notes & Coins","destination_type":"Fines",
                 "additional_information":"Third Party"}
                """),
            requestWithPaymentDetails("""
                {"amount":12.34,"method":"Notes & Coins","destination_type":"Fines","allocation_type":"FULL"}
                """),
            requestWithPaymentDetails(VALID_PAYMENT_DETAILS.replace("Notes & Coins", "XX")),
            requestWithPaymentDetails(VALID_PAYMENT_DETAILS.replace("Fines", "X")),
            requestWithPaymentDetails(VALID_PAYMENT_DETAILS.replace("Third Party", "X"))
        );
    }

    private static String requestWithPaymentDetails(String paymentDetails) {
        return VALID_REQUEST.replace(VALID_PAYMENT_DETAILS, paymentDetails);
    }
}
