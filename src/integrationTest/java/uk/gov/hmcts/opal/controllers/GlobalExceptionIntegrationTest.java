package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.UserStateService;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.GlobalExceptionIntegrationTest")
@Import(GlobalExceptionIntegrationTest.ThrowingController.class)
public class GlobalExceptionIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    UserStateService userStateService;

    @MockitoBean
    private UserState userState;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @BeforeEach
    void setupUserState() {
        Mockito.when(userState.anyBusinessUnitUserHasPermission(Mockito.any()))
            .thenReturn(true);

        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.any()))
            .thenReturn(userState);
    }

    @Test
    @DisplayName("PO-2120 / QueryTimeoutException -> 408 with retriable=true")
    void retriable_QueryTimeout_ReturnsTrue() throws Exception {
        var a = mockMvc.perform(get("/__exc/query-timeout")
                                    .header("authorization", "Bearer some_value")
                                    .accept(MediaType.APPLICATION_PROBLEM_JSON));

        String body = a.andReturn().getResponse().getContentAsString();
        log.info("QueryTimeout body:\n{}", ToJsonString.toPrettyJson(body));

        a.andExpect(status().isRequestTimeout())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(true));
    }

    @Test
    @DisplayName("PO-2120 / DataAccessResourceFailureException -> 503 with retriable=true")
    void retriable_DataAccessResourceFailure_ReturnsTrue() throws Exception {
        var a = mockMvc.perform(get("/__exc/data-access-resource-failure")
                                    .header("authorization", "Bearer some_value")
                                    .accept(MediaType.APPLICATION_PROBLEM_JSON));

        String body = a.andReturn().getResponse().getContentAsString();
        log.info("DARF body:\n{}", ToJsonString.toPrettyJson(body));

        a.andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(true));
    }

    @Test
    @DisplayName("PO-2120 / PSQLException(connectivity) -> 503 with retriable=true")
    void retriable_PsqlConnectivity_ReturnsTrue() throws Exception {
        var a = mockMvc.perform(get("/__exc/psql-connectivity")
                                    .header("authorization", "Bearer some_value")
                                    .accept(MediaType.APPLICATION_PROBLEM_JSON));

        String body = a.andReturn().getResponse().getContentAsString();
        log.info("PSQL connectivity body:\n{}", ToJsonString.toPrettyJson(body));

        a.andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(true));
    }

    @Test
    @DisplayName("PO-2120 / JpaSystemException(40001) -> 500 with retriable=true")
    void retriable_JpaTransient_ReturnsTrue() throws Exception {
        var a = mockMvc.perform(get("/__exc/jpa-serial-failure")
                                    .header("authorization", "Bearer some_value")
                                    .accept(MediaType.APPLICATION_PROBLEM_JSON));

        String body = a.andReturn().getResponse().getContentAsString();
        log.info("JPA serial body:\n{}", ToJsonString.toPrettyJson(body));

        a.andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(true));
    }

    @Test
    @DisplayName("PO-2120 / TransactionSystemException(40P01) -> 500 with retriable=true")
    void retriable_TransactionDeadlock_ReturnsTrue() throws Exception {
        var a = mockMvc.perform(get("/__exc/tx-deadlock")
                                    .header("authorization", "Bearer some_value")
                                    .accept(MediaType.APPLICATION_PROBLEM_JSON));

        String body = a.andReturn().getResponse().getContentAsString();
        log.info("TX deadlock body:\n{}", ToJsonString.toPrettyJson(body));

        a.andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(true));
    }

    @Test
    @DisplayName("PO-2120 / HttpServerErrorException upstream 503 -> 500 with retriable=true")
    void retriable_HttpServer503_ReturnsTrue() throws Exception {
        var a = mockMvc.perform(get("/__exc/http-503")
                                    .header("authorization", "Bearer some_value")
                                    .accept(MediaType.APPLICATION_PROBLEM_JSON));

        String body = a.andReturn().getResponse().getContentAsString();
        log.info("HTTP 503 body:\n{}", ToJsonString.toPrettyJson(body));

        a.andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(true));
    }

    @Test
    @DisplayName("PO-2120 / FeignException 503 -> 503 with retriable=true")
    void retriable_Feign503_ReturnsTrue() throws Exception {
        var a = mockMvc.perform(get("/__exc/feign-503")
                                    .header("authorization", "Bearer some_value")
                                    .accept(MediaType.APPLICATION_PROBLEM_JSON));

        String body = a.andReturn().getResponse().getContentAsString();
        log.info("Feign 503 body:\n{}", ToJsonString.toPrettyJson(body));

        a.andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(true));
    }

    /** Build a FeignException with typed headers (no unsafe casts). */
    private static FeignException buildFeignException(int status, String reason) {
        Map<String, Collection<String>> headers = Collections.emptyMap();
        Request request = Request.create(
            Request.HttpMethod.GET,
            "/test",
            headers,
            null,
            StandardCharsets.UTF_8,
            new RequestTemplate()
        );

        Response response = Response.builder()
            .request(request)
            .status(status)
            .reason(reason)
            .headers(headers)
            .build();

        return FeignException.errorStatus("GET /test", response);
    }

    /** Test-only controller to trigger specific exception paths in GlobalExceptionHandler. */
    @RestController
    static class ThrowingController {
        @GetMapping("/__exc/query-timeout")
        void queryTimeout() {
            throw new jakarta.persistence.QueryTimeoutException("timeout", null, null);
        }

        @GetMapping("/__exc/data-access-resource-failure")
        void dataAccessResourceFailure() {
            throw new DataAccessResourceFailureException("db unavailable");
        }

        @GetMapping("/__exc/psql-connectivity")
        void psqlConnectivity() throws PSQLException {
            throw new PSQLException("connect fail", PSQLState.CONNECTION_FAILURE,
                                    new java.net.ConnectException("refused"));
        }

        @GetMapping("/__exc/jpa-serial-failure")
        void jpaSerialFailure() {
            // JpaSystemException requires a RuntimeException; wrap the PSQLException
            PSQLException psql = new PSQLException("serial", PSQLState.SERIALIZATION_FAILURE);
            throw new JpaSystemException(new RuntimeException(psql));
        }

        @GetMapping("/__exc/tx-deadlock")
        void txDeadlock() {
            PSQLException psql = new PSQLException("deadlock", PSQLState.DEADLOCK_DETECTED);
            throw new TransactionSystemException("tx", psql);
        }

        @GetMapping("/__exc/http-503")
        void http503() {
            throw new HttpServerErrorException(HttpStatusCode.valueOf(503), "Service Unavailable");
        }

        @GetMapping("/__exc/feign-503")
        void feign503() {
            throw buildFeignException(503, "Service Unavailable");
        }
    }

}
