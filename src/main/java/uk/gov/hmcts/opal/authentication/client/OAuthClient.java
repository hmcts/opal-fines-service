package uk.gov.hmcts.opal.authentication.client;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.authentication.config.AuthProviderConfigurationProperties;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


@Component
@Slf4j(topic = "OAuthClient")
public class OAuthClient {
    @SneakyThrows({URISyntaxException.class, IOException.class})
    public HTTPResponse fetchAccessToken(AuthProviderConfigurationProperties providerConfigurationProperties,
                                         String redirectType, String authCode,
                                         String clientId,
                                         String authClientSecret,
                                         String scope) {
        log.debug(":fetchAccessToken:");
        AuthorizationCode code = new AuthorizationCode(authCode);
        URI callback = new URI(redirectType);
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, callback);
        Scope authScope = new Scope();
        authScope.add(scope);

        ClientID clientID = new ClientID(clientId);
        Secret clientSecret = new Secret(authClientSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

        URI tokenEndpoint = new URI(providerConfigurationProperties.getTokenUri());

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant, authScope);
        return request.toHTTPRequest().send();
    }
}
