package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.service.opal.AmendmentService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.AmendmentControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_amendments.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("AmendmentController Integration Test")
class AmendmentControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/amendments";

    @Autowired
    TransactionalContext transactionalContext;

    @MockitoSpyBean
    @Autowired
    OpalDefendantAccountService opalDefendantAccountService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testGetAmendmentById() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "/7"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAmendmentById: Response body:\n" + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.amendment_id").value(7))
            .andExpect(jsonPath("$.business_unit_id").value(77))
            .andExpect(jsonPath("$.associated_record_type").value("defendant_accounts"))
            .andExpect(jsonPath("$.associated_record_id").value("1"))
            .andExpect(jsonPath("$.amended_by").value("User_A"))
            .andExpect(jsonPath("$.field_code").value(1))
            .andExpect(jsonPath("$.old_value").value("Initial Data"))
            .andExpect(jsonPath("$.new_value").value("Updated Data"))
            .andExpect(jsonPath("$.case_reference").value("Case_ref"))
            .andExpect(jsonPath("$.function_code").value("Func_code"));
    }

    @Test
    void testGetAmendmentById_fail() throws Exception {
        mockMvc.perform(get(URL_BASE + "/999999")).andExpect(status().isNotFound());
    }

    @Test
    void testPostAmendmentsSearch() throws Exception {
        ResultActions actions =  mockMvc.perform(post(URL_BASE + "/search")
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .content("{}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostAmendmentsSearch: Response body:\n" + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.searchData[0].amendment_id").value(7))
            .andExpect(jsonPath("$.searchData[0].amended_by").value("User_A"))
            .andExpect(jsonPath("$.searchData[0].field_code").value(1))
            .andExpect(jsonPath("$.searchData[0].case_reference").value("Case_ref"))
            .andExpect(jsonPath("$.searchData[0].function_code").value("Func_code"))
            .andReturn();

    }

    @Test
    void testPostAmendmentSearch_noMatch() throws Exception {
        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content("{\"function_code\":\"NOTREALCODE\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostAmendmentSearch_noMatch: Response body:\n" + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
    }


    @Test
    @Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_METHOD)
    @Sql(scripts = "classpath:db/deleteData/delete_from_amendments.sql", executionPhase = AFTER_TEST_METHOD)
    void testAuditStoredProcedures() throws Exception {

        DefendantAccountSearchResultsDto searchResults =
            opalDefendantAccountService.searchDefendantAccounts(AccountSearchDto.builder().build());
        Long defAccId = 77L;
        Short busUnitId = 78;

        log.info(":testAuditStoredProcedures: found defendant:{} in business unit: {}", defAccId, busUnitId);

        transactionalContext.callTheStoredProcedures(defAccId, busUnitId);

        String sql = "SELECT * FROM defendant_accounts WHERE defendant_account_id = ?";
        Map<String, Object> rowData = jdbcTemplate.queryForMap(sql, defAccId);
        log.info(":testAuditStoredProcedures: defendant account: {}", rowData);

        ResultActions actions =  mockMvc.perform(post(URL_BASE + "/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"associated_record_id\": \"" + defAccId + "\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testAuditStoredProcedures: Response body:\n" + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.searchData[0].amendment_id").value(70000000000001L))
            .andExpect(jsonPath("$.searchData[0].amended_by").value("Tester_A"))
            .andExpect(jsonPath("$.searchData[0].field_code").value(9))
            .andExpect(jsonPath("$.searchData[0].old_value").value(14))
            .andExpect(jsonPath("$.searchData[0].new_value").value(400))
            .andExpect(jsonPath("$.searchData[1].amendment_id").value(70000000000002L))
            .andExpect(jsonPath("$.searchData[1].amended_by").value("Tester_A"))
            .andExpect(jsonPath("$.searchData[1].field_code").value(11))
            .andExpect(jsonPath("$.searchData[1].old_value").value(21))
            .andExpect(jsonPath("$.searchData[1].new_value").value(500))
            .andReturn();
    }

    @Component
    @Transactional
    public static class TransactionalContext {

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @MockitoSpyBean
        @Autowired
        AmendmentService amendmentService;

        /* In order for the stored procedures to work, the initialise, update and finalise calls all need
        to be executed within the same transaction context. */
        public void callTheStoredProcedures(Long defAccId, Short busUnitId) {

            // Initialize before making a change to defendant_accounts table
            amendmentService.auditInitialiseStoredProc(defAccId, RecordType.DEFENDANT_ACCOUNTS);

            // Directly update a defendant_accounts table row - this should cause insertions in the amendments table
            String sql = "UPDATE defendant_accounts SET cheque_clearance_period = ?, "
                + " credit_trans_clearance_period = ? WHERE defendant_account_id = ?";
            int rowsAffected = jdbcTemplate.update(sql, 400, 500, defAccId);
            log.info(":callTheStoredProcedures: directly updated: {} rows", rowsAffected);
            assertEquals(1, rowsAffected);

            // Finalize after making a change to defendant_accounts table
            amendmentService.auditFinaliseStoredProc(
                defAccId, RecordType.DEFENDANT_ACCOUNTS, busUnitId,
                "Tester_A", "Case_Ref", "Func_Code");
        }
    }



}
