package uk.gov.hmcts.opal.controllers;

import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeEntity;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@Slf4j(topic = "opal.OpalEnforcementAccountTypesPatchIntegrationTest")
@Sql(
    scripts = "classpath:db/insertData/insert_into_enforcement_account_types.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class OpalEnforcementAccountTypesPatchIntegrationTest extends AbstractOpalEnforcementAccountTypesIntegrationTest {

    private final RowMapper<EnforcementAccountTypeEntity> eafRowMapper = (resultSet, rowNum) -> {
        EnforcementAccountTypeEntity entity = new EnforcementAccountTypeEntity();
        entity.setEnforcementAccountTypeId(resultSet.getLong("enforcement_account_type_id"));
        entity.setMinimumBalance(resultSet.getBigDecimal("minimum_balance"));
        entity.setVersionNumber(resultSet.getLong("version_number"));
        return entity;
    };


    @Test
    @DisplayName("OPAL: PATCH Enforcement Account Types - Update single enforcement account type")
    @JiraStory("PO-2435")
    @JiraEpic("PO-2433")
    void patch_updatesSingleObjectOnly() throws Exception {
        authorizeWithPermission((short)78); // Auto enforcement permission

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");

        String body = """
            [
              {
                "id": 1,
                "version": 1,
                "minimum_balance": 200
              }
            ]
            """;

        ResultActions res = mockMvc.perform(
            patch(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        res.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.enforcement_account_types.[0].id").value("1"))
            .andExpect(jsonPath("$.enforcement_account_types.[0].version").value("2"))
            .andExpect(jsonPath("$.enforcement_account_types.[0].minimum_balance").value("200"));

        EnforcementAccountTypeEntity changedObject = jdbcTemplate.queryForObject(
            "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id = '1'",
            eafRowMapper);

        assertEquals(2L, changedObject.getVersionNumber());
        assertEquals(new BigDecimal("200.00"), changedObject.getMinimumBalance());
    }

    @Test
    @DisplayName("OPAL: PATCH Enforcement Account Types - Update multiple enforcement account types")
    @JiraStory("PO-2435")
    @JiraEpic("PO-2433")
    void patch_updatesMultipleObjects() throws Exception {
        authorizeWithPermission((short)78); // Auto enforcement permission

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");

        String body = """
            [
              {
                "id": 1,
                "version": 1,
                "minimum_balance": 200
              },
              {
                "id": 2,
                "version": 1,
                "minimum_balance": 300
              }
            ]
            """;

        ResultActions res = mockMvc.perform(
            patch(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        res.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.enforcement_account_types.[0].id").value("1"))
            .andExpect(jsonPath("$.enforcement_account_types.[0].version").value("2"))
            .andExpect(jsonPath("$.enforcement_account_types.[0].minimum_balance").value("200"))
            .andExpect(jsonPath("$.enforcement_account_types.[1].id").value("2"))
            .andExpect(jsonPath("$.enforcement_account_types.[1].version").value("2"))
            .andExpect(jsonPath("$.enforcement_account_types.[1].minimum_balance").value("300"));

        EnforcementAccountTypeEntity firstChanged = jdbcTemplate.queryForObject(
            "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id = '1'",
            eafRowMapper);
        assertEquals(2L, firstChanged.getVersionNumber());
        assertEquals(new BigDecimal("200.00"), firstChanged.getMinimumBalance());

        EnforcementAccountTypeEntity secondChanged = jdbcTemplate.queryForObject(
            "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id = '2'",
            eafRowMapper);
        assertEquals(2L, secondChanged.getVersionNumber());
        assertEquals(new BigDecimal("300.00"), secondChanged.getMinimumBalance());
    }

    @Test
    @DisplayName("OPAL: PATCH Enforcement Account Types - Updating a Low value to have NULL min balance should error")
    @JiraStory("PO-2435")
    @JiraEpic("PO-2433")
    void patch_updatingALowValueToHaveNullMinimumBalanceShouldError() throws Exception {
        authorizeWithPermission((short)78); // Auto enforcement permission

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");

        String body = """
            [
              {
                "id": 1,
                "version": 1
              }
            ]
            """;

        ResultActions res = mockMvc.perform(
            patch(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        res.andExpect(status().is(422))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.unprocessableReason").value(
                "Can not update enforcement account type minimum balance for a low enforcement path"))
            .andExpect(jsonPath("$.retriable").value("false"));


        EnforcementAccountTypeEntity changedObject = jdbcTemplate.queryForObject(
            "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id = '1'",
            eafRowMapper);

        assertEquals(1L, changedObject.getVersionNumber());
        assertEquals(new BigDecimal("100.00"), changedObject.getMinimumBalance());

    }

    @Test
    @DisplayName("OPAL: PATCH Enforcement Account Types - Updating a high value to have NULL min balance returns ok")
    @JiraStory("PO-2435")
    @JiraEpic("PO-2433")
    void patch_highPathNullBalanceAllowed() throws Exception {
        authorizeWithPermission((short)78); // Auto enforcement permission

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");

        String body = """
            [
              {
                "id": 3,
                "version": 1
              }
            ]
            """;

        ResultActions res = mockMvc.perform(
            patch(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        res.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.enforcement_account_types.[0].id").value("3"))
            .andExpect(jsonPath("$.enforcement_account_types.[0].version").value("2"))
            .andExpect(jsonPath("$.enforcement_account_types.[0].minimum_balance").isEmpty());

        EnforcementAccountTypeEntity changedObject = jdbcTemplate.queryForObject(
            "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id = '3'",
            eafRowMapper);

        assertEquals(2L, changedObject.getVersionNumber());
        assertNull(changedObject.getMinimumBalance());
    }

    @Test
    @DisplayName("OPAL: PATCH Enforcement Account Types - Version number mismatch should result in no update")
    @JiraStory("PO-2435")
    @JiraEpic("PO-2433")
    void patch_versionNumberMisMatch() throws Exception {
        authorizeWithPermission((short)78); // Auto enforcement permission

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");

        String body = """
            [
              {
                "id": 1,
                "version": 0,
                "minimum_balance": 200
              }
            ]
            """;

        ResultActions res = mockMvc.perform(
            patch(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        res.andExpect(status().is(409))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Version numbers do not match"))
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.retriable").value("false"))
            .andExpect(jsonPath("$.type")
                           .value("https://hmcts.gov.uk/problems/optimistic-locking"));

        EnforcementAccountTypeEntity changedObject = jdbcTemplate.queryForObject(
            "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id = '1'",
            eafRowMapper);

        assertEquals(1L, changedObject.getVersionNumber());
        assertEquals(new BigDecimal("100.00"), changedObject.getMinimumBalance());
    }

    @Test
    @DisplayName("OPAL: PATCH Enforcement Account Types - Enforcement Account Type not found")
    @JiraStory("PO-2435")
    @JiraEpic("PO-2433")
    void patch_enforcementAccountTypeNotFound() throws Exception {
        authorizeWithPermission((short)78); // Auto enforcement permission

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");

        String body = """
            [
              {
                "id": 1000,
                "version": 1,
                "minimum_balance": 200
              }
            ]
            """;

        ResultActions res = mockMvc.perform(
            patch(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        res.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value("false"));

        List<EnforcementAccountTypeEntity> changedObject = jdbcTemplate.query(
            "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id IN (1, 2, 3)", eafRowMapper);
        assertEquals(1L, changedObject.getFirst().getVersionNumber());
        assertEquals(1L, changedObject.get(1).getVersionNumber());
        assertEquals(1L, changedObject.get(2).getVersionNumber());
    }

    @Test
    @DisplayName("OPAL: PATCH Enforcement Account Types - Invalid permissions returns an error")
    @JiraStory("PO-2435")
    @JiraEpic("PO-2433")
    void patch_invalidPermissionReturnsError() throws Exception {
        authoriseNoPermissions(); // Auto enforcement permission

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(expiredToken);
        headers.add("Business-Unit-Id", "78");

        String body = """
            [
              {
                "id": 1,
                "version": 1,
                "minimum_balance": 200
              }
            ]
            """;

        ResultActions res = mockMvc.perform(
            patch(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        res.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value("false"))
            .andExpect(jsonPath("$.detail")
                           .value("You do not have permission to access this resource"));

        List<EnforcementAccountTypeEntity> changedObject = jdbcTemplate.query(
            "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id IN (1, 2, 3)", eafRowMapper);
        assertEquals(1L, changedObject.getFirst().getVersionNumber());
        assertEquals(1L, changedObject.get(1).getVersionNumber());
        assertEquals(1L, changedObject.get(2).getVersionNumber());
    }

    @Test
    @DisplayName("OPAL: PATCH Enforcement Account Types - Transaction rolled back on mixed success")
    @JiraStory("PO-2435")
    @JiraEpic("PO-2433")
    void patch_rollsbackOnMixedSuccessNullMinBalance() throws Exception {
        authorizeWithPermission((short)78); // Auto enforcement permission

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");

        EnforcementAccountTypeEntity orig = jdbcTemplate.queryForObject(
            "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id = '1'",
            eafRowMapper);

        String body = """
            [
              {
                "id": 1,
                "version": 1,
                "minimum_balance": 200
              },
              {
                "id": 2,
                "version": 1
              },
              {
                "id": 3,
                "version": 0,
                "minimum_balance": 300
              }
            ]
            """;

        ResultActions res = mockMvc.perform(
            patch(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        res.andExpect(status().is(422))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.unprocessableReason").value(
                "Can not update enforcement account type minimum balance for a low enforcement path"))
            .andExpect(jsonPath("$.retriable").value("false"));


        List<EnforcementAccountTypeEntity> changedObject = jdbcTemplate.query(
            "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id IN (1, 2, 3)", eafRowMapper);
        assertEquals(1L, changedObject.getFirst().getVersionNumber());
        assertEquals(1L, changedObject.get(1).getVersionNumber());
        assertEquals(1L, changedObject.get(2).getVersionNumber());
    }

    @Test
    @DisplayName("OPAL: PATCH Enforcement Account Types - Transaction rolled back on mixed success")
    @JiraStory("PO-2435")
    @JiraEpic("PO-2433")
    void patch_rollsbackOnMixedSuccessVersionMismatch() throws Exception {
        authorizeWithPermission((short)78); // Auto enforcement permission

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");

        String body = """
            [
              {
                "id": 1,
                "version": 1,
                "minimum_balance": 200
              },
              {
                "id": 2,
                "version": 5,
                "minimum_balance": 300
              },
              {
                "id": 3,
                "version": 0,
                "minimum_balance": 300
              }
            ]
            """;

        ResultActions res = mockMvc.perform(
            patch(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        res.andExpect(status().is(409))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Version numbers do not match"))
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.retriable").value("false"))
            .andExpect(jsonPath("$.type")
                           .value("https://hmcts.gov.uk/problems/optimistic-locking"));


        List<EnforcementAccountTypeEntity> changedObject = jdbcTemplate.query(
            "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id IN (1, 2, 3)", eafRowMapper);
        assertEquals(1L, changedObject.getFirst().getVersionNumber());
        assertEquals(1L, changedObject.get(1).getVersionNumber());
        assertEquals(1L, changedObject.get(2).getVersionNumber());
    }

    @Test
    @DisplayName("OPAL: PATCH Enforcement Account Types - Negative minimum balance is rejected")
    @JiraStory("PO-2435")
    @JiraEpic("PO-2433")
    void patch_minBalanceNumericValidation() throws Exception {
        authorizeWithPermission((short)78); // Auto enforcement permission

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");

        String body = """
            [
              {
                "id": 1,
                "version": 1,
                "minimum_balance": -200
              }
            ]
            """;

        ResultActions res = mockMvc.perform(
            patch(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        res.andExpect(status().is(422))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
            .andExpect(jsonPath("$.unprocessableReason")
                           .value("Can not set minimum balance to a negative value"))
            .andExpect(jsonPath("$.retriable").value("false"));


        List<EnforcementAccountTypeEntity> changedObject = jdbcTemplate.query(
            "SELECT * FROM enforcement_account_types WHERE enforcement_account_type_id IN (1, 2, 3)", eafRowMapper);
        assertEquals(1L, changedObject.getFirst().getVersionNumber());
        assertEquals(1L, changedObject.get(1).getVersionNumber());
        assertEquals(1L, changedObject.get(2).getVersionNumber());
    }
}
