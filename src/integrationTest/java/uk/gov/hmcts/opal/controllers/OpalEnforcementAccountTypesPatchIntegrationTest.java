package uk.gov.hmcts.opal.controllers;

import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.LowHighValue;
import uk.gov.hmcts.opal.entity.enforcement.AccountType;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountType;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeEntity;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static uk.gov.hmcts.opal.common.dto.ToJsonString.toJsonString;


@ActiveProfiles({"integration", "opal"})
@Slf4j(topic = "opal.OpalEnforcementAccountTypesPatchIntegrationTest")
@Sql(
    scripts = "classpath:db/deleteData/delete_from_enforcement_account_types.sql",
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD
)
class OpalEnforcementAccountTypesPatchIntegrationTest extends AbstractIntegrationWithSecurityTest {

    protected static final String URL_BASE = "/enforcement-accounts-types";

    protected void authorizeWithPermission() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short)1, FinesPermission.AUTO_ENFORCEMENT);
    }

    protected void authoriseNoPermissions() {
        userStateStub.setupWithNoPermissions();
    }

    private final RowMapper<EnforcementAccountTypeEntity> eatRowMapper = (resultSet, rowNum) -> {
        return EnforcementAccountTypeEntity.builder()
            .enforcementAccountTypeId(resultSet.getLong("enforcement_account_type_id"))
            .minimumBalance(resultSet.getBigDecimal("minimum_balance"))
            .versionNumber(resultSet.getLong("version_number"))
            .enforcementAccountType(
                EnforcementAccountType.getByCode(resultSet.getString("enforcement_account_type")))
            .accountType(AccountType.getByCode(resultSet.getString("account_type")))
            .accountTypePath(LowHighValue.getByValue(resultSet.getString("account_type_path")))
            .build();
    };

    private void assertEnforcementAccountType(int id, long expectedVersion, BigDecimal expectedMinBal) {
        EnforcementAccountTypeEntity changedObject = jdbcTemplate.queryForObject(
            "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id = ?",
            eatRowMapper, id);

        assertEquals(expectedVersion, changedObject.getVersionNumber());
        assertEquals(expectedMinBal, changedObject.getMinimumBalance());
    }

    @TestPropertySource(properties = {
        "launchdarkly.enabled=false",
        "launchdarkly.default-flag-values.release-1c-auto-enforcement-config=true"
    })
    @Nested
    class FeatureOn {

        @Test
        @DisplayName("OPAL: PATCH Enforcement Account Types - Update single enforcement account type")
        @JiraStory("PO-2435")
        @JiraEpic("PO-2433")
        void patch_updatesSingleObjectOnly() throws Exception {
            authorizeWithPermission(); // Auto enforcement permission

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userStateStub.getBearerToken());

            List<EnforcementAccountTypeCommon> body = List.of(
                EnforcementAccountTypeCommon.builder()
                    .id(1L)
                    .version(1L)
                    .minimumBalance(new BigDecimal("200"))
                    .build()
            );

            ResultActions res = mockMvc.perform(
                patch(URL_BASE)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJsonString(body))
            );

            res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.enforcement_account_types.[0].id").value("1"))
                .andExpect(jsonPath("$.enforcement_account_types.[0].version").value("2"))
                .andExpect(jsonPath("$.enforcement_account_types.[0].minimum_balance").value("200"));


            assertEnforcementAccountType(1, 2L, new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("OPAL: PATCH Enforcement Account Types - Update multiple enforcement account types")
        @JiraStory("PO-2435")
        @JiraEpic("PO-2433")
        void patch_updatesMultipleObjects() throws Exception {
            authorizeWithPermission(); // Auto enforcement permission

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userStateStub.getBearerToken());

            List<EnforcementAccountTypeCommon> body = List.of(
                EnforcementAccountTypeCommon.builder()
                    .id(1L)
                    .version(1L)
                    .minimumBalance(new BigDecimal("200"))
                    .build(),
                EnforcementAccountTypeCommon.builder()
                    .id(2L)
                    .version(1L)
                    .minimumBalance(new BigDecimal("300"))
                    .build()
            );

            ResultActions res = mockMvc.perform(
                patch(URL_BASE)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJsonString(body))
            );

            res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.enforcement_account_types.[0].id").value("1"))
                .andExpect(jsonPath("$.enforcement_account_types.[0].version").value("2"))
                .andExpect(jsonPath("$.enforcement_account_types.[0].minimum_balance").value("200"))
                .andExpect(jsonPath("$.enforcement_account_types.[1].id").value("2"))
                .andExpect(jsonPath("$.enforcement_account_types.[1].version").value("2"))
                .andExpect(jsonPath("$.enforcement_account_types.[1].minimum_balance").value("300"));

            assertEnforcementAccountType(1, 2L, new BigDecimal("200.00"));
            assertEnforcementAccountType(2, 2L, new BigDecimal("300.00"));
        }

        @Test
        @DisplayName("OPAL: PATCH Enforcement Account Types - Updating a Low value to NULL min balance should error")
        @JiraStory("PO-2435")
        @JiraEpic("PO-2433")
        void patch_updatingALowValueToHaveNullMinimumBalanceShouldError() throws Exception {
            authorizeWithPermission(); // Auto enforcement permission

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userStateStub.getBearerToken());

            List<EnforcementAccountTypeCommon> body = List.of(
                EnforcementAccountTypeCommon.builder()
                    .id(1L)
                    .version(1L)
                    .build()
            );


            ResultActions res = mockMvc.perform(
                patch(URL_BASE)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJsonString(body))
            );

            res.andExpect(status().is(422))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.unprocessableReason").value(
                    "Can not update enforcement account type minimum balance for a low enforcement path"))
                .andExpect(jsonPath("$.retriable").value("false"));

            assertEnforcementAccountType(1, 1L, null);

        }

        @Test
        @DisplayName("OPAL: PATCH Enforcement Account Types - Updating a high value to NULL min balance returns ok")
        @JiraStory("PO-2435")
        @JiraEpic("PO-2433")
        void patch_highPathNullBalanceAllowed() throws Exception {
            authorizeWithPermission(); // Auto enforcement permission

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userStateStub.getBearerToken());

            List<EnforcementAccountTypeCommon> body = List.of(
                EnforcementAccountTypeCommon.builder()
                    .id(3L)
                    .version(1L)
                    .build()
            );

            ResultActions res = mockMvc.perform(
                patch(URL_BASE)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJsonString(body))
            );

            res.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.enforcement_account_types.[0].id").value("3"))
                .andExpect(jsonPath("$.enforcement_account_types.[0].version").value("1"))
                .andExpect(jsonPath("$.enforcement_account_types.[0].minimum_balance").isEmpty());

            assertEnforcementAccountType(3, 1L, null);
        }

        @Test
        @DisplayName("OPAL: PATCH Enforcement Account Types - Version number mismatch should result in no update")
        @JiraStory("PO-2435")
        @JiraEpic("PO-2433")
        void patch_versionNumberMisMatch() throws Exception {
            authorizeWithPermission(); // Auto enforcement permission

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userStateStub.getBearerToken());

            List<EnforcementAccountTypeCommon> body = List.of(
                EnforcementAccountTypeCommon.builder()
                    .id(1L)
                    .version(0L)
                    .minimumBalance(new BigDecimal("200"))
                    .build()
            );

            ResultActions res = mockMvc.perform(
                patch(URL_BASE)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJsonString(body))
            );

            res.andExpect(status().is(409))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(":updateEnforcementAccountType: Versions do"
                    + " not match for: EnforcementAccountTypeEntity '1'; DB version: 1, supplied update version: 0"))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.retriable").value("false"))
                .andExpect(jsonPath("$.type")
                    .value("https://hmcts.gov.uk/problems/optimistic-locking"));

            assertEnforcementAccountType(1, 1L, null);
        }

        @Test
        @DisplayName("OPAL: PATCH Enforcement Account Types - Enforcement Account Type not found")
        @JiraStory("PO-2435")
        @JiraEpic("PO-2433")
        void patch_enforcementAccountTypeNotFound() throws Exception {
            authorizeWithPermission(); // Auto enforcement permission

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userStateStub.getBearerToken());

            List<EnforcementAccountTypeCommon> body = List.of(
                EnforcementAccountTypeCommon.builder()
                    .id(1000L)
                    .version(1L)
                    .minimumBalance(new BigDecimal("200"))
                    .build()
            );
            ResultActions res = mockMvc.perform(
                patch(URL_BASE)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJsonString(body))
            );

            res.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.retriable").value("false"));

            assertEnforcementAccountType(1, 1L, null);
            assertEnforcementAccountType(2, 1L, null);
            assertEnforcementAccountType(3, 1L, null);
        }

        @Test
        @DisplayName("OPAL: PATCH Enforcement Account Types - Invalid permissions returns an error")
        @JiraStory("PO-2435")
        @JiraEpic("PO-2433")
        void patch_invalidPermissionReturnsError() throws Exception {
            authoriseNoPermissions(); // Auto enforcement permission

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(expiredToken);

            List<EnforcementAccountTypeCommon> body = List.of(
                EnforcementAccountTypeCommon.builder()
                    .id(1L)
                    .version(1L)
                    .minimumBalance(new BigDecimal("200"))
                    .build()
            );
            ResultActions res = mockMvc.perform(
                patch(URL_BASE)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJsonString(body))
            );

            res.andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.retriable").value("false"))
                .andExpect(jsonPath("$.detail")
                    .value("You do not have permission to access this resource"));

            assertEnforcementAccountType(1, 1L, null);
            assertEnforcementAccountType(2, 1L, null);
            assertEnforcementAccountType(3, 1L, null);
        }

        @Test
        @DisplayName("OPAL: PATCH Enforcement Account Types - Transaction rolled back on mixed success")
        @JiraStory("PO-2435")
        @JiraEpic("PO-2433")
        void patch_rollsbackOnMixedSuccessNullMinBalance() throws Exception {
            authorizeWithPermission(); // Auto enforcement permission

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userStateStub.getBearerToken());

            EnforcementAccountTypeEntity orig = jdbcTemplate.queryForObject(
                "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id = '1'",
                eatRowMapper);

            List<EnforcementAccountTypeCommon> body = List.of(
                EnforcementAccountTypeCommon.builder()
                    .id(1L)
                    .version(1L)
                    .minimumBalance(new BigDecimal("200"))
                    .build(),
                EnforcementAccountTypeCommon.builder()
                    .id(2L)
                    .version(1L)
                    .build(),
                EnforcementAccountTypeCommon.builder()
                    .id(3L)
                    .version(0L)
                    .minimumBalance(new BigDecimal("300"))
                    .build()
            );

            ResultActions res = mockMvc.perform(
                patch(URL_BASE)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJsonString(body))
            );

            res.andExpect(status().is(422))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.unprocessableReason").value(
                    "Can not update enforcement account type minimum balance for a low enforcement path"))
                .andExpect(jsonPath("$.retriable").value("false"));

            assertEnforcementAccountType(1, 1L, null);
            assertEnforcementAccountType(2, 1L, null);
            assertEnforcementAccountType(3, 1L, null);
        }

        @Test
        @DisplayName("OPAL: PATCH Enforcement Account Types - Transaction rolled back on mixed success")
        @JiraStory("PO-2435")
        @JiraEpic("PO-2433")
        void patch_rollsbackOnMixedSuccessVersionMismatch() throws Exception {
            authorizeWithPermission(); // Auto enforcement permission

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userStateStub.getBearerToken());

            List<EnforcementAccountTypeCommon> body = List.of(
                EnforcementAccountTypeCommon.builder()
                    .id(1L)
                    .version(1L)
                    .minimumBalance(new BigDecimal("200"))
                    .build(),
                EnforcementAccountTypeCommon.builder()
                    .id(2L)
                    .version(5L)
                    .minimumBalance(new BigDecimal("300"))
                    .build(),
                EnforcementAccountTypeCommon.builder()
                    .id(3L)
                    .version(0L)
                    .minimumBalance(new BigDecimal("300"))
                    .build()
            );

            ResultActions res = mockMvc.perform(
                patch(URL_BASE)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJsonString(body))
            );

            res.andExpect(status().is(409))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(":updateEnforcementAccountType: Versions do"
                    + " not match for: EnforcementAccountTypeEntity '2'; DB version: 1, supplied update version: 5"))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.retriable").value("false"))
                .andExpect(jsonPath("$.type")
                    .value("https://hmcts.gov.uk/problems/optimistic-locking"));

            assertEnforcementAccountType(1, 1L, null);
            assertEnforcementAccountType(2, 1L, null);
            assertEnforcementAccountType(3, 1L, null);
        }

        @Test
        @DisplayName("OPAL: PATCH Enforcement Account Types - Negative minimum balance is rejected")
        @JiraStory("PO-2435")
        @JiraEpic("PO-2433")
        void patch_minBalanceNumericValidation() throws Exception {
            authorizeWithPermission(); // Auto enforcement permission

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userStateStub.getBearerToken());

            List<EnforcementAccountTypeCommon> body = List.of(
                EnforcementAccountTypeCommon.builder()
                    .id(1L)
                    .version(1L)
                    .minimumBalance(new BigDecimal("-200"))
                    .build()
            );

            ResultActions res = mockMvc.perform(
                patch(URL_BASE)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJsonString(body))
            );

            res.andExpect(status().is(422))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.unprocessableReason")
                    .value("Can not set minimum balance to a negative value"))
                .andExpect(jsonPath("$.retriable").value("false"));

            assertEnforcementAccountType(1, 1L, null);
            assertEnforcementAccountType(2, 1L, null);
            assertEnforcementAccountType(3, 1L, null);
        }
    }

    @TestPropertySource(properties = {
        "launchdarkly.enabled=false",
        "launchdarkly.default-flag-values.release-1c-auto-enforcement-config=false"
    })
    @Nested
    class FeatureOff {

        @Test
        @DisplayName("PO-2435 - Feature flag off test")
        @JiraStory("PO-2435")
        @JiraEpic("PO-2433")
        void getAllEnforcementAccountTypes_FeatureOff_404() throws Exception {
            authorizeWithPermission(); // Auto enforcement permission

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userStateStub.getBearerToken());

            List<EnforcementAccountTypeCommon> body = List.of(
                EnforcementAccountTypeCommon.builder()
                    .id(1L)
                    .version(1L)
                    .minimumBalance(new BigDecimal("200"))
                    .build()
            );
            ResultActions res = mockMvc.perform(
                patch(URL_BASE)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJsonString(body))
            );

            res.andExpect(status().isNotFound());
        }
    }
}
