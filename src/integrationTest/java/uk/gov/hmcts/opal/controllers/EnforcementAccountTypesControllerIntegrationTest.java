package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.core.type.TypeReference;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;
import uk.gov.hmcts.opal.generated.model.GetEnforcementAccountTypes200Response;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j(topic = "opal.EnforcementAccountTypesControllerIntegrationTest")
@DisplayName("Enforcement Account Types Integration Test")
public class EnforcementAccountTypesControllerIntegrationTest extends AbstractIntegrationTest {
    private static final String URL = "/enforcement-accounts-types/";

    @Test
    @JiraStory("PO-2434")
    @JiraEpic("PO-2433")
//    @FeatureToggle(
//        feature = FeatureFlags.RELEASE_1B,
//        defaultValueProperty = FeatureFlags.RELEASE_1B_ENABLED_PROPERTY
//    )
    void returnsAllEnforcementAccountTypes_200() throws Exception {
        setupAuthorisedUser();
        ResultActions result = mockMvc.perform(
            get(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
        );

        String body = result.andReturn().getResponse().getContentAsString();
        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        GetEnforcementAccountTypes200Response response = objectMapper.readValue(body, new TypeReference<GetEnforcementAccountTypes200Response>(){});
        List<EnforcementAccountTypeCommon.EnforcementAccountTypeEnum> eats = response.getEnforcementAccountTypes()
            .stream()
            .map(eat -> eat.getEnforcementAccountType())
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

    private void setupAuthorisedUser() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 1, FinesPermission.AUTO_ENFORCEMENT);
    }

    @Test
    @JiraStory("PO-2434")
    @JiraEpic("PO-2433")
//    @FeatureToggle(
//        feature = FeatureFlags.RELEASE_1B,
//        defaultValueProperty = FeatureFlags.RELEASE_1B_ENABLED_PROPERTY
//    )
    void forbiddenWithoutAutoEnforcementPermission() throws Exception {
        userStateStub.setupWithNoPermissions();
        ResultActions result = mockMvc.perform(
            get(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
        );

        result.andExpect(status().isForbidden());
    }

//    @Test
//    @JiraStory("PO-2434")
//    @JiraEpic("PO-2433")
//    @FeatureToggle(
//        feature = FeatureFlags.RELEASE_1B,
//        defaultValueProperty = FeatureFlags.RELEASE_1B_ENABLED_PROPERTY
//    )
//    void unauthorisedWhenTokenIsMissing() throws Exception {
//        mockMvc.perform(
//            get(URL)
//                .header(HttpHeaders.AUTHORIZATION, "")
//            )
//            .andExpect(status().isUnauthorized());
//    }

    @Test
    @JiraStory("PO-2434")
    @JiraEpic("PO-2433")
//    @FeatureToggle(
//        feature = FeatureFlags.RELEASE_1B,
//        defaultValueProperty = FeatureFlags.RELEASE_1B_ENABLED_PROPERTY
//    )
    void deterministicAndIdempotentGET() throws Exception {
        setupAuthorisedUser();
        String responseBody1 = callGetAndReturnContentAsString();
        String responseBody2 = callGetAndReturnContentAsString();
        String responseBody3 = callGetAndReturnContentAsString();

        assertAll(
            () -> assertEquals(responseBody1, responseBody2),
            () -> assertEquals(responseBody2, responseBody3)
        );
    }

    private String callGetAndReturnContentAsString() throws Exception {
        return mockMvc.perform(
                get(URL)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }
}
