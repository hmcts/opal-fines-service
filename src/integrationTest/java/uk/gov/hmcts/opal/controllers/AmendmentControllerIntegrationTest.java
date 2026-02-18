package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.TestContainerConfig;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.service.opal.AmendmentService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountService;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.AmendmentControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_amendments.sql", executionPhase = BEFORE_TEST_CLASS)
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestContainerConfig.class})
@DisplayName("AmendmentController Integration Test")
class AmendmentControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/amendments";

    private TransactionalContext transactionalContext;

    @MockitoSpyBean
    OpalDefendantAccountService opalDefendantAccountService;

    private DefendantAccountRepository defendantAccountRepository;

    @BeforeEach
    void loadBeans(ApplicationContext applicationContext) {
        this.transactionalContext = applicationContext.getBean(TransactionalContext.class);
        this.defendantAccountRepository = applicationContext.getBean(DefendantAccountRepository.class);
    }

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
        Long defAccId = 77L;
        Short busUnitId = (short)78;

        transactionalContext.callTheStoredProcedures(defAccId, busUnitId);
        log.info(":testAuditStoredProcedures: found defendant:{} in business unit: {}", defAccId, busUnitId);

        transactionalContext.callTheStoredProcedures(defAccId, busUnitId);

        DefendantAccountEntity account = defendantAccountRepository.findById(defAccId)
            .orElseThrow(() -> new AssertionError("Defendant account not found: " + defAccId));
        log.info(":testAuditStoredProcedures: defendant account: {}", account);

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

    @Test
    @Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_METHOD)
    @Sql(scripts = "classpath:db/deleteData/delete_from_amendments.sql", executionPhase = AFTER_TEST_METHOD)
    void testAuditStoredProcedures_enforcementFields() throws Exception {
        Long defAccId = 77L;
        Short busUnitId = (short)78;

        transactionalContext.callTheStoredProceduresForEnforcementFields(defAccId, busUnitId);
        log.info(":testAuditStoredProcedures_enforcementFields: found defendant:{} in business unit: {}",
            defAccId, busUnitId);

        ResultActions actions =  mockMvc.perform(post(URL_BASE + "/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"associated_record_id\": \"" + defAccId + "\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testAuditStoredProcedures_enforcementFields: Response body:\n" + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(4))
            .andExpect(jsonPath("$.searchData[*].field_code", containsInAnyOrder(14, 15, 16, 26)))
            .andReturn();
    }

    @Component
    @Transactional
    public static class TransactionalContext {

        private final DefendantAccountRepository defendantAccountRepository;
        private final CourtRepository courtRepository;
        private final AmendmentService amendmentService;

        public TransactionalContext(DefendantAccountRepository defendantAccountRepository,
            CourtRepository courtRepository,
            AmendmentService amendmentService) {
            this.defendantAccountRepository = defendantAccountRepository;
            this.courtRepository = courtRepository;
            this.amendmentService = amendmentService;
        }

        /* In order for the stored procedures to work, the initialise, update and finalise calls all need
        to be executed within the same transaction context. */
        public void callTheStoredProcedures(Long defAccId, Short busUnitId) {

            // Initialize before making a change to defendant_accounts table
            amendmentService.auditInitialiseStoredProc(defAccId, RecordType.DEFENDANT_ACCOUNTS);

            DefendantAccountEntity account = defendantAccountRepository.findById(defAccId)
                .orElseThrow(() -> new AssertionError("Defendant account not found: " + defAccId));
            account.setChequeClearancePeriod((short) 400);
            account.setCreditTransferClearancePeriod((short) 500);
            defendantAccountRepository.saveAndFlush(account);

            // Finalize after making a change to defendant_accounts table
            amendmentService.auditFinaliseStoredProc(
                defAccId, RecordType.DEFENDANT_ACCOUNTS, busUnitId,
                "Tester_A", "Case_Ref", "Func_Code");
        }

        public void callTheStoredProceduresForEnforcementFields(Long defAccId, Short busUnitId) {
            amendmentService.auditInitialiseStoredProc(defAccId, RecordType.DEFENDANT_ACCOUNTS);

            DefendantAccountEntity account = defendantAccountRepository.findById(defAccId)
                .orElseThrow(() -> new AssertionError("Defendant account not found: " + defAccId));
            account.setEnforcingCourt(courtRepository.findById(100L)
                .orElseThrow(() -> new AssertionError("Court not found: 100")));
            account.setEnforcementOverrideEnforcerId(21L);
            account.setEnforcementOverrideResultId("ABDC");
            account.setEnforcementOverrideTfoLjaId((short) 101);
            defendantAccountRepository.saveAndFlush(account);

            amendmentService.auditFinaliseStoredProc(
                defAccId, RecordType.DEFENDANT_ACCOUNTS, busUnitId,
                "Tester_A", "Case_Ref", "Func_Code");
        }
    }


}
