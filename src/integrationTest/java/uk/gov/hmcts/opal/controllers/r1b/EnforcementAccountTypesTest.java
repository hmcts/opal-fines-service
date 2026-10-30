package uk.gov.hmcts.opal.controllers.r1b;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.wiremock.spring.InjectWireMock;
import tools.jackson.core.type.TypeReference;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;
import uk.gov.hmcts.opal.generated.model.GetEnforcementAccountTypes200Response;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@Slf4j(topic = "opal.EnforcementAccountTypesTest")
@DisplayName("Enforcement Account Types Integration Test")
public class EnforcementAccountTypesTest extends AbstractIntegrationTest {

    private static final String URL = "/enforcement-accounts-types";

    @TestPropertySource(properties = {
        "launchdarkly.enabled=false",
        "launchdarkly.default-flag-values.release-1c-auto-enforcement-config=true"
    })
    @Nested
    class FeatureOn {

        @InjectWireMock("user-service")
        private WireMockServer userServiceWireMock;

        @Test
        @DisplayName("PO-2434 - INT.01 & INT.06 – Return all enforcement account types")
        @JiraStory("PO-2434")
        @JiraEpic("PO-2433")
        @JiraTestKey("PO-9391")
        void returnsAllEnforcementAccountTypes_200() throws Exception {
            ResultActions result = mockMvc.perform(get(URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + AUTH_HEADER));

            String body = result.andReturn().getResponse().getContentAsString();
            result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            GetEnforcementAccountTypes200Response response = objectMapper.readValue(
                body, new TypeReference<GetEnforcementAccountTypes200Response>() {
                }
            );
            List<EnforcementAccountTypeCommon.EnforcementAccountTypeEnum> eats = response.getEnforcementAccountTypes()
                .stream()
                .map(EnforcementAccountTypeCommon::getEnforcementAccountType)
                .toList();
            assertAll(
                () -> assertEquals(8, eats.size()),
                () -> assertTrue(eats.contains(EnforcementAccountTypeCommon.EnforcementAccountTypeEnum.AL)),
                () -> assertTrue(eats.contains(EnforcementAccountTypeCommon.EnforcementAccountTypeEnum.AH)),
                () -> assertTrue(eats.contains(EnforcementAccountTypeCommon.EnforcementAccountTypeEnum.COL)),
                () -> assertTrue(eats.contains(EnforcementAccountTypeCommon.EnforcementAccountTypeEnum.COH)),
                () -> assertTrue(eats.contains(EnforcementAccountTypeCommon.EnforcementAccountTypeEnum.COLL)),
                () -> assertTrue(eats.contains(EnforcementAccountTypeCommon.EnforcementAccountTypeEnum.COLH)),
                () -> assertTrue(eats.contains(EnforcementAccountTypeCommon.EnforcementAccountTypeEnum.YL)),
                () -> assertTrue(eats.contains(EnforcementAccountTypeCommon.EnforcementAccountTypeEnum.YH))
            );
        }

        @Test
        @DisplayName("PO-2434 - INT.07 – Forbidden without Auto Enforcement permission")
        @JiraStory("PO-2434")
        @JiraEpic("PO-2433")
        @JiraTestKey("PO-9390")
        void forbiddenWithoutAutoEnforcementPermission() throws Exception {
            userServiceWireMock.stubFor(WireMock.get(urlPathEqualTo("/v2/users/0/state"))
                .atPriority(1).willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("UserService/user-state-no-permissions.json")));

            ResultActions result = mockMvc.perform(get(URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + AUTH_HEADER));

            result.andExpect(status().isForbidden());
        }


        @Test
        @DisplayName("PO-2434 - INT.04 & INT.06 – Deterministic and idempotent GET")
        @JiraStory("PO-2434")
        @JiraEpic("PO-2433")
        @JiraTestKey("PO-9392")
        void deterministicAndIdempotentGET() throws Exception {
            String responseBody1 = callGetAndReturnContentAsString();
            String responseBody2 = callGetAndReturnContentAsString();
            String responseBody3 = callGetAndReturnContentAsString();

            assertAll(
                () -> assertEquals(responseBody1, responseBody2),
                () -> assertEquals(responseBody2, responseBody3)
            );
        }

        private String callGetAndReturnContentAsString() throws Exception {
            return mockMvc.perform(get(URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + AUTH_HEADER))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        }
    }

    @TestPropertySource(properties = {
        "launchdarkly.enabled=false",
        "launchdarkly.default-flag-values.release-1c-auto-enforcement-config=false"
    })
    @Nested
    class FeatureOff {

        @Test
        @DisplayName("PO-2434 - Feature flag off test")
        @JiraStory("PO-2434")
        @JiraEpic("PO-2433")
        @JiraTestKey("PO-9393")
        void getAllEnforcementAccountTypes_FeatureOff_404() throws Exception {
            ResultActions result = mockMvc.perform(get(URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + AUTH_HEADER));

            result.andExpect(status().isNotFound());
        }
    }
}
