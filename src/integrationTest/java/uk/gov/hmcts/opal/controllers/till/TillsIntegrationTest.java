package uk.gov.hmcts.opal.controllers.till;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.wiremock.spring.InjectWireMock;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles("integration")
@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1c-payment=true")
@DisplayName("Tills Integration Tests")
@Sql(scripts = "classpath:db/insertData/insert_into_get_till.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_get_till.sql", executionPhase = AFTER_TEST_METHOD)
class TillsIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/tills/99000000836200";
    private static final Short BUSINESS_UNIT_ID = (short) 8362;

    @InjectWireMock("user-service")
    private WireMockServer userServiceWireMock;

    @Test
    @DisplayName("PO-8362 INT.01 - Returns till with fine and suspense payments")
    @JiraStory("PO-8362")
    @JiraStory("PO-3629")
    @JiraEpic("PO-3635")
    void returnsTill() throws Exception {
        permitBusinessUnit();

        mockMvc.perform(get(URL).header(AUTHORIZATION, "Bearer " + AUTH_HEADER))
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
        userStateStub.setupWithNoPermissions();
        userStateStub.createAuthentication(userServiceWireMock);

        mockMvc.perform(get(URL).header(AUTHORIZATION, "Bearer " + AUTH_HEADER))
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
        permitBusinessUnit();

        mockMvc.perform(get("/tills/99000000836999")
                .header(AUTHORIZATION, "Bearer " + AUTH_HEADER))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Entity Not Found"));
    }

    private void permitBusinessUnit() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(BUSINESS_UNIT_ID, PROCESS_AND_ALLOCATE_PAYMENTS);
        userStateStub.createAuthentication(userServiceWireMock);
    }

}
