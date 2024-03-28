package uk.gov.hmcts.opal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.authentication.model.AccessTokenResponse;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.Role;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.authorisation.service.AuthorisationService;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.opal.service.DynamicConfigService;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = TestingSupportController.class)
@ActiveProfiles({"integration"})
class TestingSupportControllerTest {

    private static final String TEST_TOKEN = "testToken";
    private static final UserState USER_STATE = UserState.builder()
        .userName("name")
        .userId(123L)
        .roles(Set.of(Role.builder()
                          .businessUnitId((short) 123)
                          .businessUserId("BU123")
                          .permissions(Set.of(
                              Permission.builder()
                                  .permissionId(1L)
                                  .permissionName("Notes")
                                  .build()))
                          .build()))
        .build();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DynamicConfigService dynamicConfigService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private AccessTokenService accessTokenService;

    @MockBean
    private AuthorisationService authorisationService;

    @Test
    void testGetAppMode() throws Exception {
        AppMode appMode = AppMode.builder().mode("test").build();

        when(dynamicConfigService.getAppMode()).thenReturn(appMode);

        mockMvc.perform(get("/api/testing-support/app-mode"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.mode").value("test"));
    }

    @Test
    void testUpdateMode() throws Exception {
        AppMode appMode = AppMode.builder().mode("test").build();

        when(dynamicConfigService.updateAppMode(any(AppMode.class))).thenReturn(appMode);

        mockMvc.perform(put("/api/testing-support/app-mode")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(appMode)))
            .andExpect(status().isAccepted())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.mode").value("test"));
    }

    @Test
    void testIsFeatureEnabled() throws Exception {
        when(featureToggleService.isFeatureEnabled(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/testing-support/launchdarkly/bool/testFeature"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isBoolean());
    }

    @Test
    void testGetFeatureValue() throws Exception {
        String featureValue = "testValue";
        when(featureToggleService.getFeatureValue(anyString())).thenReturn(featureValue);

        mockMvc.perform(get("/api/testing-support/launchdarkly/string/testFeature"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(featureValue));
    }

    @Test
    void testGetToken() throws Exception {
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken("testAccessToken");

        when(accessTokenService.getTestUserToken()).thenReturn(accessTokenResponse);

        SecurityToken securityToken = SecurityToken.builder()
            .accessToken(TEST_TOKEN)
            .userState(USER_STATE)
            .build();

        when(authorisationService.getSecurityToken("testAccessToken")).thenReturn(securityToken);

        mockMvc.perform(get("/api/testing-support/token/test-user"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").value("testToken"))
            .andExpect(jsonPath("$.userState.userName").value("name"))
            .andExpect(jsonPath("$.userState.userId").value("123"))
            .andExpect(jsonPath("$.userState.roles[0].businessUnitId").value("123"))
            .andExpect(jsonPath("$.userState.roles[0].businessUserId").value("BU123"))
            .andExpect(jsonPath("$.userState.roles[0].businessUserId").value("BU123"))
            .andExpect(jsonPath("$.userState.roles[0].permissions[0].permissionId").value("1"))
            .andExpect(jsonPath("$.userState.roles[0].permissions[0].permissionName")
                           .value("Notes"));

    }

    @Test
    void testGetTokenForUser() throws Exception {
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken("testAccessToken");

        when(accessTokenService.getTestUserToken(anyString())).thenReturn(accessTokenResponse);

        SecurityToken securityToken = SecurityToken.builder()
            .accessToken(TEST_TOKEN)
            .userState(USER_STATE)
            .build();
        when(authorisationService.getSecurityToken("testAccessToken")).thenReturn(securityToken);

        mockMvc.perform(get("/api/testing-support/token/user")
                            .header("X-User-Email", "test@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").value("testToken"))
            .andExpect(jsonPath("$.userState.userName").value("name"))
            .andExpect(jsonPath("$.userState.userId").value("123"))
            .andExpect(jsonPath("$.userState.roles[0].businessUnitId").value("123"))
            .andExpect(jsonPath("$.userState.roles[0].businessUserId").value("BU123"))
            .andExpect(jsonPath("$.userState.roles[0].businessUserId").value("BU123"))
            .andExpect(jsonPath("$.userState.roles[0].permissions[0].permissionId").value("1"))
            .andExpect(jsonPath("$.userState.roles[0].permissions[0].permissionName")
                           .value("Notes"));
    }

    @Test
    void testParseToken() throws Exception {
        String token = "Bearer testToken";

        when(accessTokenService.extractPreferredUsername(token)).thenReturn("testUser");

        mockMvc.perform(get("/api/testing-support/token/parse")
                            .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("testUser"));
    }
}
