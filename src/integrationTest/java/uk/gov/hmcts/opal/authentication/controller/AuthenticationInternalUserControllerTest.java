package uk.gov.hmcts.opal.authentication.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authentication.service.AuthenticationService;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.net.URI;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.AuthenticationInternalUserControllerTest")
class AuthenticationInternalUserControllerTest extends AbstractIntegrationTest {

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @Test
    void testLoginOrRefresh() throws Exception {
        String redirectUri = "http://example.com/redirect";
        when(authenticationService.loginOrRefresh(any(), any())).thenReturn(new URI(redirectUri));
        when(authenticationService.getLoginUri(any())).thenReturn(new URI(redirectUri));

        ResultActions actions = mockMvc.perform(get("/internal-user/login-or-refresh")
                            .param("redirect_uri", redirectUri));

        String redirectUrl = actions.andReturn().getResponse().getRedirectedUrl();
        log.info(":testLoginOrRefresh: \n\tRedirect URL: {}", redirectUrl);

        actions.andExpect(status().isFound())
            .andExpect(view().name("redirect:" + redirectUri));
    }

    @Test
    void testHandleOauthCode() throws Exception {
        when(authenticationService.handleOauthCode(anyString())).thenReturn("accessToken");

        when(accessTokenService.extractPreferredUsername(anyString())).thenReturn("username");
        UserState userState = UserState.builder()
            .userName("name")
            .userId(123L)
            .businessUnitUser(Set.of(BusinessUnitUser.builder()
                              .businessUnitId((short) 123)
                              .businessUnitUserId("BU123")
                              .permissions(Set.of(
                                  Permission.builder()
                                      .permissionId(1L)
                                      .permissionName("Notes")
                                      .build()))
                              .build()))
            .build();
        SecurityToken securityToken = SecurityToken.builder()
            .userState(userState)
            .accessToken("accessToken")
            .build();

        when(authenticationService.getSecurityToken(anyString())).thenReturn(securityToken);

        ResultActions actions = mockMvc.perform(post("/internal-user/handle-oauth-code")
                            .param("code", "code")
                            .contentType(MediaType.APPLICATION_JSON));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testHandleOauthCode: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.access_token").value("accessToken"))
            .andExpect(jsonPath("$.user_state.user_name").value("name"))
            .andExpect(jsonPath("$.user_state.user_id").value("123"))
            .andExpect(jsonPath("$.user_state.business_unit_user[0].business_unit_id")
                           .value("123"))
            .andExpect(jsonPath("$.user_state.business_unit_user[0].business_unit_user_id")
                           .value("BU123"))
            .andExpect(jsonPath("$.user_state.business_unit_user[0].permissions[0].permission_id")
                           .value("1"))
            .andExpect(
               jsonPath("$.user_state.business_unit_user[0].permissions[0].permission_name")
                           .value("Notes"));
    }

    @Test
    void testLogout() throws Exception {
        String redirectUri = "http://example.com/redirect";
        when(authenticationService.logout(any(), any())).thenReturn(new URI(redirectUri));

        ResultActions actions = mockMvc.perform(get("/internal-user/logout")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer access_token")
                            .param("redirect_uri", redirectUri));

        String redirectUrl = actions.andReturn().getResponse().getRedirectedUrl();
        log.info(":testLogout: \n\tRedirect URL: {}", redirectUrl);

        actions.andExpect(status().isFound())
            .andExpect(view().name("redirect:" + redirectUri));
    }

    @Test
    void testResetPassword() throws Exception {
        String redirectUri = "http://example.com/redirect";
        when(authenticationService.resetPassword(any())).thenReturn(new URI(redirectUri));

        ResultActions actions = mockMvc.perform(get("/internal-user/reset-password")
                            .param("redirect_uri", redirectUri));

        String redirectUrl = actions.andReturn().getResponse().getRedirectedUrl();
        log.info(":testResetPassword: \n\tRedirect URL: {}", redirectUrl);

        actions.andExpect(status().isFound())
            .andExpect(view().name("redirect:" + redirectUri));
    }
}
