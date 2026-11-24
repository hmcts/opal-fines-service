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
import static uk.gov.hmcts.opal.controllers.shared.util.OpenApiContractAssertions.assertJsonRequestMatchesBundledSpec;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.repository.MiscellaneousAccountRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.report.PreAllocatedCashTillService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1c-payment=true")
@Sql(scripts = "classpath:db/insertData/insert_into_create_till.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayName("POST /tills - PO-11365")
@Tag("ExtendedTest")
// Spring injection is provided by the inherited AbstractIntegrationTest context.
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class CreateTillIntegrationTest extends AbstractIntegrationWithSecurityTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    @MockitoBean private PreAllocatedCashTillService reportService;
    @Autowired private TillRepository tillRepository;
    @Autowired private PaymentInRepository paymentInRepository;
    @Autowired private MiscellaneousAccountRepository accountRepository;
    @Autowired private PartyRepository partyRepository;
    @Autowired private PlatformTransactionManager transactionManager;

    @BeforeEach
    void permitPayments() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 10, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
    }

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @MethodSource("scenarios")
    void persistsScenarioWithoutDuplicatedPayer(String destination, String receivedFrom, String reason, String payer)
        throws Exception {
        ObjectNode request = request(destination, receivedFrom, reason, payer);
        assertJsonRequestMatchesBundledSpec(request, "/tills", "POST", "application/json");
        final long partiesBefore = partyRepository.count();
        final long accountsBefore = accountRepository.count();

        postRequest(request).andExpect(status().isCreated());

        PaymentInEntity payment = storedPayments().getFirst();
        TillEntity till = tillRepository.findById(payment.getTillEntity().getTillId()).orElseThrow();
        assertThat(till.getTotalAmount()).isEqualByComparingTo("12.50");
        assertThat(till.getPaymentsCount()).isEqualTo((short) 1);
        assertThat(storedPayments()).hasSize(1);
        assertThat(payment.getPaymentAmount()).isEqualByComparingTo("12.50");
        assertThat(payment.getDestinationType().name()).isEqualTo(destination);
        ObjectNode metadata = information(request).deepCopy();
        metadata.remove(List.of("payer_type", "payer_details", "third_party_payer_name"));
        assertThat(objectMapper.readTree(payment.getAdditionalInformation())).isEqualTo(metadata);
        assertThat(payment.getAdditionalInformation())
            .doesNotContain("payer_type", "payer_details", "Smith", "Company");
        if (destination.equals("F")) {
            assertThat(payment.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.DEFENDANT_ACCOUNTS);
            assertThat(payment.getAssociatedRecordId()).isEqualTo("11365001");
            assertThat(payment.getThirdPartyPayerName()).isEqualTo(receivedFrom.equals("T") ? "Third party" : null);
        } else if (payer == null) {
            assertThat(payment.getAssociatedRecordType()).isNull();
            assertThat(payment.getAssociatedRecordId()).isNull();
        } else {
            assertThat(payment.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.MISCELLANEOUS_ACCOUNTS);
            assertParty(payment, payer);
        }
        assertThat(partyRepository.count()).isEqualTo(partiesBefore + (payer == null ? 0 : 1));
        assertThat(accountRepository.count()).isEqualTo(accountsBefore + (payer == null ? 0 : 1));
    }

    static Stream<Arguments> scenarios() {
        return Stream.of(
            Arguments.of("F", "D", null, null), Arguments.of("F", "T", null, null),
            Arguments.of("C", "A", null, "Individual"), Arguments.of("C", "T", null, "Organisation"),
            Arguments.of("S", "D", "OF", "Individual"), Arguments.of("S", "A", "FA", "Organisation"),
            Arguments.of("S", "T", "MS", "Individual"), Arguments.of("S", "D", "UN", null),
            Arguments.of("S", "A", "UN", "Individual"), Arguments.of("S", "T", "UN", "Organisation"));
    }

    @Test
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    void acceptsCreditTransferAndOptionalAddress() throws Exception {
        ObjectNode request = request("C", "A", null, "Individual");
        ((ObjectNode) request.path("payments_in").get(0).path("payment_details")).put("method", "CT");
        ((ObjectNode) information(request).path("payer_details")).put("address_line_1", "1 Street")
            .put("address_line_2", "Town").put("address_line_3", "County").put("postcode", "AB1 2CD");
        postRequest(request).andExpect(status().isCreated());
        PaymentInEntity payment = storedPayments().getFirst();
        assertThat(payment.getPaymentMethod().name()).isEqualTo("CT");
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            PartyEntity party = accountRepository.findById(Long.valueOf(payment.getAssociatedRecordId()))
                .orElseThrow().getParty();
            assertThat(party.getAddressLine1()).isEqualTo("1 Street");
            assertThat(party.getAddressLine2()).isEqualTo("Town");
            assertThat(party.getAddressLine3()).isEqualTo("County");
            assertThat(party.getPostcode()).isEqualTo("AB1 2CD");
        });
    }

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @CsvSource({"NC,false", "NC,true", "CQ,false", "CQ,true", "PO,false", "PO,true", "CT,true"})
    void preservesOptionalMetadataForEveryMethod(String method, boolean supplied) throws Exception {
        ObjectNode request = request("S", "D", "UN", null);
        ((ObjectNode) payment(request).path("payment_details")).put("method", method);
        ObjectNode information = information(request);
        if (supplied) {
            information.put("payment_reference", "").put("suspense_reason_detail", "Unidentified receipt");
        } else {
            information.remove(List.of("transfer_source", "payment_reference"));
        }
        assertJsonRequestMatchesBundledSpec(request, "/tills", "POST", "application/json");
        postRequest(request).andExpect(status().isCreated());
        PaymentInEntity payment = storedPayments().getFirst();
        assertThat(objectMapper.readTree(payment.getAdditionalInformation())).isEqualTo(information);
        assertThat(payment.getAssociatedRecordType()).isNull();
        assertThat(payment.getAssociatedRecordId()).isNull();
    }

    @Test
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    void preservesDistinctPerPaymentMetadata() throws Exception {
        ObjectNode request = request("F", "D", null, null);
        ObjectNode second = (ObjectNode) request("S", "A", "UN", null).path("payments_in").get(0);
        ((ObjectNode) second.path("additional_information")).put("payment_reference", "second");
        request.withArray("payments_in").add(second);
        postRequest(request).andExpect(status().isCreated());
        List<PaymentInEntity> payments = storedPayments();
        assertThat(payments).hasSize(2);
        assertThat(payments.get(0).getAdditionalInformation()).contains("reference-123").doesNotContain("second");
        assertThat(payments.get(1).getAdditionalInformation()).contains("second").doesNotContain("reference-123");
    }

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @MethodSource("invalidRequests")
    void rejectsInvalidRequestWithoutPartialData(ObjectNode request) throws Exception {
        long[] before = counts();
        postRequest(request).andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").isNotEmpty());
        assertThat(counts()).containsExactly(before);
        verifyNoInteractions(reportService);
    }

    static Stream<ObjectNode> invalidRequests() {
        return Stream.of(
            changed("F", "D", null, null, r -> payment(r).remove("defendant_account_id")),
            changed("F", "D", null, null, r -> payment(r).put("defendant_account_id", 999999999)),
            changed("F", "D", null, null, r -> payment(r).put("defendant_account_id", 11365002)),
            changed("C", "A", null, "Individual", r -> payment(r).put("defendant_account_id", 11365001)),
            changed("S", "D", "UN", null, r -> payment(r).put("defendant_account_id", 11365001)),
            changed("F", "A", null, null, r -> { }), changed("C", "D", null, "Individual", r -> { }),
            changed("F", "T", null, null, r -> information(r).remove("third_party_payer_name")),
            changed("F", "T", null, null, r -> information(r).put("third_party_payer_name", " ")),
            changed("F", "D", null, "Individual", r -> { }),
            changed("C", "A", null, null, r -> { }),
            changed("S", "D", "OF", null, r -> { }), changed("S", "D", "FA", null, r -> { }),
            changed("S", "D", "MS", null, r -> { }),
            changed("F", "D", null, null, r -> {
                ((ObjectNode) payment(r).path("payment_details")).put("method", "CT");
                information(r).remove("transfer_source");
            }),
            changed("S", "D", "UN", null, r -> information(r).remove("suspense_note")),
            changed("C", "A", null, "Individual", r -> information(r).remove("court_fee_code")),
            changed("C", "A", null, "Individual", r -> information(r).remove("number_of_items")),
            changed("C", "A", null, "Individual", r -> information(r).put("number_of_items", 0)),
            changed("S", "D", "OF", "Individual", r -> information(r).put("suspense_reason_detail", "bad")),
            changed("F", "D", null, null, r -> information(r).put("court_fee_code", "FEE")),
            changed("F", "D", null, null, r -> information(r).put("suspense_note", "note")),
            changed("F", "D", null, null, r -> information(r).remove("payment_received_from")),
            changed("F", "D", null, null, r -> payment(r).remove("additional_information")),
            changed("F", "D", null, null, r -> r.set("additional_information", information(r).deepCopy())),
            changed("C", "A", null, "Individual", r -> information(r).put("payer_type", "Organisation")),
            changed("C", "A", null, "Individual", r -> information(r).remove("payer_details")),
            changed("C", "A", null, "Individual", r -> information(r).remove("payer_type")),
            changed("C", "A", null, "Individual", r -> ((ObjectNode) information(r).path("payer_details"))
                .putObject("organisation").put("organisation_name", "Mixed")),
            changed("F", "D", null, null, r -> ((ObjectNode) payment(r).path("payment_details"))
                .put("method", "Notes & Coins")),
            changed("F", "D", null, null, r -> ((ObjectNode) payment(r).path("payment_details"))
                .put("destination_type", "Fines")),
            changed("F", "D", null, null, r -> information(r).put("payment_received_from", "Defendant")),
            changed("F", "D", null, null, r -> information(r).put("transfer_source", "NatWest")),
            changed("S", "D", "UN", null, r -> information(r).put("suspense_reason_code", "Unidentified amount")),
            changed("F", "D", null, null, r -> {
                ObjectNode invalid = payment(r).deepCopy();
                invalid.remove("defendant_account_id");
                r.withArray("payments_in").add(invalid);
            }));
    }

    @Test
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    void rollsBackDurablePayerWhenReportCreationFails() throws Exception {
        long[] before = counts();
        when(reportService.createPreAllocatedReportInstance(anyLong(), anyLong(), anyString()))
            .thenThrow(new ReportGenerationException("Report failed", new RuntimeException()));
        postRequest(request("C", "A", null, "Individual")).andExpect(status().isInternalServerError());
        assertThat(counts()).containsExactly(before);
    }

    @Test
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    void requiresTokenAndBusinessUnitPermission() throws Exception {
        mockMvc.perform(post("/tills").contentType(MediaType.APPLICATION_JSON)
                .content(request("F", "D", null, null).toString())).andExpect(status().isUnauthorized());
        userStateStub.setupWithNoPermissions();
        postRequest(request("F", "D", null, null)).andExpect(status().isForbidden());
    }

    private void assertParty(PaymentInEntity payment, String payer) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            MiscellaneousAccountEntity account = accountRepository
                .findById(Long.valueOf(payment.getAssociatedRecordId())).orElseThrow();
            assertThat(account.getBusinessUnit().getBusinessUnitId()).isEqualTo((short) 10);
            assertThat(account.getAccountNumber()).isNotBlank();
            PartyEntity party = account.getParty();
            assertThat(party.isOrganisation()).isEqualTo(payer.equals("Organisation"));
            assertThat(party.getSurname()).isEqualTo(payer.equals("Individual") ? "Smith" : null);
            assertThat(party.getOrganisationName()).isEqualTo(payer.equals("Organisation") ? "Company" : null);
            assertThat(party.getAddressLine1()).isNull();
        });
    }

    private List<PaymentInEntity> storedPayments() {
        ArgumentCaptor<Long> id = ArgumentCaptor.forClass(Long.class);
        verify(reportService).createPreAllocatedReportInstance(id.capture(), anyLong(), anyString());
        return paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(id.getValue());
    }

    private long[] counts() {
        return new long[] {tillRepository.count(), paymentInRepository.count(), accountRepository.count(),
            partyRepository.count()};
    }

    private ResultActions postRequest(ObjectNode request) throws Exception {
        return mockMvc.perform(post("/tills").with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .contentType(MediaType.APPLICATION_JSON).content(request.toString()));
    }

    private static ObjectNode changed(String destination, String receivedFrom, String reason, String payer,
                                      Consumer<ObjectNode> change) {
        ObjectNode request = request(destination, receivedFrom, reason, payer);
        change.accept(request);
        return request;
    }

    static ObjectNode request(String destination, String receivedFrom, String reason, String payer) {
        ObjectNode request = JSON.createObjectNode().put("business_unit_id", 10);
        ObjectNode payment = request.putArray("payments_in").addObject();
        if (destination.equals("F")) {
            payment.put("defendant_account_id", 11365001);
        }
        payment.putObject("payment_details").put("amount", 12.50).put("method", "NC")
            .put("destination_type", destination).put("allocation_type", destination.equals("S") ? reason : "FULL");
        ObjectNode information = payment.putObject("additional_information").put("payment_received_from", receivedFrom)
            .put("payment_reference", "reference-123").put("transfer_source", "NATWEST");
        if (destination.equals("F") && receivedFrom.equals("T")) {
            information.put("third_party_payer_name", "Third party");
        }
        if (!destination.equals("F")) {
            information.put("suspense_note", "Payment note");
        }
        if (destination.equals("C")) {
            information.put("court_fee_code", "FEE").put("number_of_items", 2);
        } else if (destination.equals("S")) {
            information.put("suspense_reason_code", reason);
            if ("UN".equals(reason)) {
                information.put("suspense_reason_detail", "Unidentified receipt");
            }
        }
        if (payer != null) {
            information.put("payer_type", payer);
            ObjectNode details = information.putObject("payer_details");
            if (payer.equals("Individual")) {
                details.putObject("individual").put("surname", "Smith");
            } else {
                details.putObject("organisation").put("organisation_name", "Company");
            }
        }
        return request;
    }

    private static ObjectNode payment(ObjectNode request) {
        return (ObjectNode) request.path("payments_in").get(0);
    }

    static ObjectNode information(ObjectNode request) {
        return (ObjectNode) payment(request).path("additional_information");
    }
}
