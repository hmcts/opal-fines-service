package uk.gov.hmcts.opal.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.opal.support.UserServiceStub.USER_STATE_PATH;
import static uk.gov.hmcts.opal.support.UserServiceStub.V2_USER_STATE;
import static uk.gov.hmcts.opal.support.UserServiceStub.stubAuthorisedUser;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.UserStateClientServiceIT")
class UserStateClientServiceIT extends AbstractIntegrationWithSecurityTest {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    UserStateClientService userStateClientService;

    @BeforeEach
    void setUp() {
        redisTemplate.delete("USER_STATE_GfsHbIMt49WjQ");
        WireMock.reset(); // Clears everything before each test
    }

    @Test
    void getUserStateByAuthenticationTokenTwiceProvingCacheWorks() {

        stubAuthorisedUser(V2_USER_STATE);

        WireMock.verify(0, getRequestedFor(urlEqualTo(USER_STATE_PATH)));

        Jwt jwt = Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("sub", "GfsHbIMt49WjQ")
            .claim("name", "Pablo")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();

        // First Call - user service used
        UserStateV2 userStateFromUserServiceStub = userStateClientService.getUserStateByAuthenticationToken(jwt).get();
        assertThat(userStateFromUserServiceStub.getName()).isEqualTo("Pablo");
        WireMock.verify(1, getRequestedFor(urlEqualTo(USER_STATE_PATH)));

        // We have only a stub user service so need to update cache ourselves.
        @SuppressWarnings("unchecked")
        Map<String, Object> fakeCachedUserState = objectMapper.readValue(V2_USER_STATE, Map.class);
        fakeCachedUserState.put("name", "Pablo-CACHED");
        String fakeCachedUserStateJson = objectMapper.writeValueAsString(fakeCachedUserState);
        redisTemplate.opsForValue().set("USER_STATE_GfsHbIMt49WjQ", fakeCachedUserStateJson);

        // Second Call - cache should be used
        UserStateV2 userStateFromCache = userStateClientService.getUserStateByAuthenticationToken(jwt).get();
        assertThat(userStateFromCache.getName()).isEqualTo("Pablo-CACHED");
        WireMock.verify(1, getRequestedFor(urlEqualTo(USER_STATE_PATH)));
    }
}
