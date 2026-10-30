package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_SUSPENSE;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_SUSPENSE_ENABLED_PROPERTY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.common.controllers.advice.OpalGlobalExceptionHandler;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggleAspect;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.generated.model.SuspenseItemTransactionRequestSuspenseItems;
import uk.gov.hmcts.opal.generated.model.SuspenseItemTransactionRequestSuspenseItems.TransactionTypeEnum;
import uk.gov.hmcts.opal.generated.model.SuspenseItemTransactionResponseSuspenseItems;
import uk.gov.hmcts.opal.service.opal.SuspenseItemTransactionService;

class SuspenseItemsApiControllerTest {

    private static final String PATH = "/suspense-items/1/transaction";
    private static final String BODY = "{\"transactionType\":\"MS\",\"noteText\":\"Suspense note\"}";
    private SuspenseItemTransactionService service;
    private FeatureToggleApi featureToggleApi;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(SuspenseItemTransactionService.class);
        featureToggleApi = mock(FeatureToggleApi.class);
        AspectJProxyFactory factory = new AspectJProxyFactory(new SuspenseItemsApiController(service));
        factory.setProxyTargetClass(true);
        factory.addAspect(new FeatureToggleAspect(featureToggleApi));
        SuspenseItemsApiController controller = factory.getProxy();
        mockMvc = standaloneSetup(controller).setControllerAdvice(new OpalGlobalExceptionHandler()).build();
    }

    @Test
    void returnsCreatedTransaction() throws Exception {
        enableFeature();
        when(service.createTransaction(eq(1L), eq((short) 78), eq("\"2\""), any()))
            .thenReturn(new SuspenseItemTransactionResponseSuspenseItems()
                .suspenseItemVersion(3L).suspenseTransactionId(123L));

        mockMvc.perform(post(PATH).header("Business-Unit-Id", 78).header("If-Match", "\"2\"")
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.suspenseItemVersion").value(3))
            .andExpect(jsonPath("$.suspenseTransactionId").value(123));

        ArgumentCaptor<SuspenseItemTransactionRequestSuspenseItems> request =
            ArgumentCaptor.forClass(SuspenseItemTransactionRequestSuspenseItems.class);
        verify(service).createTransaction(eq(1L), eq((short) 78), eq("\"2\""), request.capture());
        assertThat(request.getValue().getTransactionType()).isEqualTo(TransactionTypeEnum.MS);
        assertThat(request.getValue().getNoteText()).isEqualTo("Suspense note");
    }

    @Test
    void disabledFeaturePreventsServiceCall() throws Exception {
        mockMvc.perform(post(PATH).header("Business-Unit-Id", 78).header("If-Match", "\"2\"")
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isNotFound());

        verify(featureToggleApi).isFeatureEnabledWithPropertyValueDefault(
            RELEASE_1C_SUSPENSE, RELEASE_1C_SUSPENSE_ENABLED_PROPERTY, false);
        verifyNoInteractions(service);
    }

    @Test
    void returnsUnavailableProblemWhileDatabaseWorkIsPending() throws Exception {
        enableFeature();
        when(service.createTransaction(any(), any(), any(), any()))
            .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Suspense item transaction creation is not yet available"));

        mockMvc.perform(post(PATH).header("Business-Unit-Id", 78).header("If-Match", "\"2\"")
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(503));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{}", "{", "{\"transactionType\":\"MS\"}",
        "{\"noteText\":\"Note\"}", "{\"transactionType\":null,\"noteText\":\"Note\"}",
        "{\"transactionType\":\"MS\",\"noteText\":null}", "{\"transactionType\":\"XX\",\"noteText\":\"Note\"}"})
    void rejectsInvalidBody(String body) throws Exception {
        mockMvc.perform(post(PATH).header("Business-Unit-Id", 78).header("If-Match", "\"2\"")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Business-Unit-Id", "If-Match"})
    void requiresHeaders(String header) throws Exception {
        String suppliedHeader = "Business-Unit-Id".equals(header) ? "If-Match" : "Business-Unit-Id";
        mockMvc.perform(post(PATH).header(suppliedHeader, "78")
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    private void enableFeature() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            RELEASE_1C_SUSPENSE, RELEASE_1C_SUSPENSE_ENABLED_PROPERTY, false)).thenReturn(true);
    }
}
