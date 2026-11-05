package uk.gov.hmcts.opal.controllers.till;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS;
import static uk.gov.hmcts.opal.entity.AssociatedRecordType.MISCELLANEOUS_ACCOUNTS;

import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.repository.MiscellaneousAccountRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles("integration")
@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1c-payment=true")
@DisplayName("Tills Integration Tests")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(scripts = "classpath:db/insertData/insert_into_get_till.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_get_till.sql", executionPhase = AFTER_TEST_METHOD)
class TillsIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/tills/99000000836200";
    private static final Short BUSINESS_UNIT_ID = (short) 8362;

    @MockitoBean
    private UserStateService userStateService;

    @Autowired
    private PaymentInRepository paymentInRepository;

    @Autowired
    private MiscellaneousAccountRepository miscellaneousAccountRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private TillRepository tillRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @DisplayName("PO-11366 - Durable payer names without JSON payer details")
    @JiraStory("PO-11366")
    @JiraEpic("PO-3635")
    @Sql(scripts = "classpath:db/insertData/insert_into_get_till_durable.sql", executionPhase = BEFORE_TEST_METHOD)
    void returnsDurablePayersWithoutChangingRecords() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            List.of(BUSINESS_UNIT_ID), PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of(BUSINESS_UNIT_ID));
        List<PaymentInEntity> before = paymentInRepository
            .findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(99000000836200L);
        long partyCount = partyRepository.count();
        long accountCount = miscellaneousAccountRepository.count();
        String owner = tillRepository.findById(99000000836200L).orElseThrow().getOwnedByName();

        mockMvc.perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.payments_in", hasSize(5)))
            .andExpect(jsonPath("$.payments_in[0].defendant_detail.party_details.individual_names.surname")
                .value("Doe"))
            .andExpect(jsonPath("$.payments_in[1].payer_name.surname").value("Smith"))
            .andExpect(jsonPath("$.payments_in[2].payer_name.forenames").value("Durable Sam"))
            .andExpect(jsonPath("$.payments_in[2].payer_name.surname").value("Durable Smith"))
            .andExpect(jsonPath("$.payments_in[2].third_party_payer_name").value("First Class Third Party"))
            .andExpect(jsonPath("$.payments_in[3].payer_name.organisation_name").value("Durable Organisation"))
            .andExpect(jsonPath("$.payments_in[4].payer_name.organisation_name").value("Durable Organisation"));

        List<PaymentInEntity> after = paymentInRepository
            .findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(99000000836200L);
        assertThat(after).usingRecursiveComparison().ignoringFields("tillEntity").isEqualTo(before);
        assertThat(partyRepository.count()).isEqualTo(partyCount);
        assertThat(miscellaneousAccountRepository.count()).isEqualTo(accountCount);
        assertThat(tillRepository.findById(99000000836200L).orElseThrow().getOwnedByName()).isEqualTo(owner);
        assertThat(partyRepository.findById(99000000836270L).orElseThrow().getForenames()).isEqualTo("Durable Sam");
        assertThat(partyRepository.findById(99000000836271L).orElseThrow().getOrganisationName())
            .isEqualTo("Durable Organisation");
    }

    @Test
    @DisplayName("PO-11366 - Missing durable account returns common not-found response")
    @JiraStory("PO-11366")
    @JiraEpic("PO-3635")
    void rejectsMissingDurableAccount() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            List.of(BUSINESS_UNIT_ID), PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of(BUSINESS_UNIT_ID));
        PaymentInEntity payment = paymentInRepository.findById(99000000836250L).orElseThrow();
        payment.setAssociatedRecordType(MISCELLANEOUS_ACCOUNTS);
        paymentInRepository.saveAndFlush(payment);

        mockMvc.perform(get(URL))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Entity Not Found"));
    }

    @Test
    @DisplayName("PO-11366 - Batched miscellaneous accounts include loaded parties")
    @JiraStory("PO-11366")
    @JiraEpic("PO-3635")
    @Sql(scripts = "classpath:db/insertData/insert_into_get_till_durable.sql", executionPhase = BEFORE_TEST_METHOD)
    void batchLoadsParties() {
        List<MiscellaneousAccountEntity> accounts = miscellaneousAccountRepository
            .findAllByMiscellaneousAccountIdIn(List.of(99000000836280L, 99000000836281L));

        assertThat(accounts).hasSize(2).allSatisfy(account -> {
            assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(account, "party")).isTrue();
            assertThat(account.getParty().getPartyId()).isNotNull();
        });
    }

    @Test
    @DisplayName("PO-8362 INT.01 - Returns till with fine and suspense payments")
    @JiraStory("PO-8362")
    @JiraStory("PO-3629")
    @JiraEpic("PO-3635")
    void returnsTill() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            List.of(BUSINESS_UNIT_ID), PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of(BUSINESS_UNIT_ID));

        mockMvc.perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.till_number").value(8362))
            .andExpect(jsonPath("$.business_unit_id").value(8362))
            .andExpect(jsonPath("$.created_by").value("Alex Cashier"))
            .andExpect(jsonPath("$.created_date").value("2026-09-09T10:30:00"))
            .andExpect(jsonPath("$.payments_in", hasSize(2)))
            .andExpect(jsonPath("$.payments_in[0].payment_in_id").value(99000000836240L))
            .andExpect(jsonPath("$.payments_in[0].associated_record_type")
                .value("defendant_accounts"))
            .andExpect(jsonPath("$.payments_in[0].amount").value(25.50))
            .andExpect(jsonPath("$.payments_in[0].method").value("NC"))
            .andExpect(jsonPath("$.payments_in[0].destination_type").value("F"))
            .andExpect(jsonPath("$.payments_in[0].payment_received_from").value("D"))
            .andExpect(jsonPath("$.payments_in[0].defendant_detail.defendant_account_number")
                .value("GTIL123"))
            .andExpect(jsonPath("$.payments_in[0].defendant_detail.party_details.organisation_flag")
                .value(false))
            .andExpect(jsonPath("$.payments_in[0].defendant_detail.party_details"
                + ".individual_names.forenames").value("Jane"))
            .andExpect(jsonPath("$.payments_in[0].defendant_detail.party_details"
                + ".individual_names.surname").value("Doe"))
            .andExpect(jsonPath("$.payments_in[1].payment_in_id").value(99000000836250L))
            .andExpect(jsonPath("$.payments_in[1].payment_received_from").value("T"))
            .andExpect(jsonPath("$.payments_in[1].third_party_payer_name")
                .value("Third Party Payer"))
            .andExpect(jsonPath("$.payments_in[1].payer_name.forenames").value("Sam"))
            .andExpect(jsonPath("$.payments_in[1].payer_name.surname").value("Smith"));
    }

    @Test
    @DisplayName("PO-8362 INT.02 - Returns forbidden without business-unit permission")
    @JiraStory("PO-8362")
    @JiraStory("PO-3629")
    @JiraEpic("PO-3635")
    void rejectsUnpermittedBusinessUnit() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            List.of(BUSINESS_UNIT_ID), PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of());

        mockMvc.perform(get(URL))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"));
    }

    @Test
    @DisplayName("PO-8362 INT.03 - Returns not found for an unknown till")
    @JiraStory("PO-8362")
    @JiraStory("PO-3629")
    @JiraEpic("PO-3635")
    void returnsNotFound() throws Exception {
        mockMvc.perform(get("/tills/99000000836999"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Entity Not Found"));
    }

}
