package uk.gov.hmcts.opal.authentication.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authentication.service.AuthenticationService;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.Role;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.authorisation.service.AuthorisationService;

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

@WebMvcTest
@ContextConfiguration(classes = AuthenticationInternalUserController.class)
@ActiveProfiles({"integration"})
class AuthenticationInternalUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private AccessTokenService accessTokenService;

    @MockBean
    private AuthorisationService authorisationService;

    @Test
    void testLoginOrRefresh() throws Exception {
        String redirectUri = "http://example.com/redirect";
        when(authenticationService.loginOrRefresh(any(), any())).thenReturn(new URI(redirectUri));
        when(authenticationService.getLoginUri(any())).thenReturn(new URI(redirectUri));

        mockMvc.perform(get("/internal-user/login-or-refresh")
                            .param("redirect_uri", redirectUri))
            .andExpect(status().isFound())
            .andExpect(view().name("redirect:" + redirectUri));
    }

    @Test
    void testHandleOauthCode() throws Exception {
        when(authenticationService.handleOauthCode(anyString())).thenReturn("accessToken");

        when(accessTokenService.extractPreferredUsername(anyString())).thenReturn("username");
        UserState userState = UserState.builder()
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
        SecurityToken securityToken = SecurityToken.builder()
            .userState(userState)
            .accessToken("accessToken")
            .build();

        when(authenticationService.getSecurityToken(anyString())).thenReturn(securityToken);

        mockMvc.perform(post("/internal-user/handle-oauth-code")
                            .param("code", "code")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.access_token").value("accessToken"))
            .andExpect(jsonPath("$.user_state.user_name").value("name"))
            .andExpect(jsonPath("$.user_state.user_id").value("123"))
            .andExpect(jsonPath("$.user_state.roles[0].business_unit_id").value("123"))
            .andExpect(jsonPath("$.user_state.roles[0].business_user_id").value("BU123"))
            .andExpect(jsonPath("$.user_state.roles[0].permissions[0].permission_id").value("1"))
            .andExpect(jsonPath("$.user_state.roles[0].permissions[0].permission_name")
                           .value("Notes"));
    }

    @Test
    void testLogout() throws Exception {
        String redirectUri = "http://example.com/redirect";
        when(authenticationService.logout(any(), any())).thenReturn(new URI(redirectUri));

        mockMvc.perform(get("/internal-user/logout")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer access_token")
                            .param("redirect_uri", redirectUri))
            .andExpect(status().isFound())
            .andExpect(view().name("redirect:" + redirectUri));
    }

    @Test
    void testResetPassword() throws Exception {
        String redirectUri = "http://example.com/redirect";
        when(authenticationService.resetPassword(any())).thenReturn(new URI(redirectUri));

        mockMvc.perform(get("/internal-user/reset-password")
                            .param("redirect_uri", redirectUri))
            .andExpect(status().isFound())
            .andExpect(view().name("redirect:" + redirectUri));
    }
}
