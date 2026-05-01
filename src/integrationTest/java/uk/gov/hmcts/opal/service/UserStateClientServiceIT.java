package uk.gov.hmcts.opal.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest.V2_USER_STATE;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.UserStateClientServiceIT")
class UserStateClientServiceIT extends AbstractIntegrationTest {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    UserStateClientService userStateClientService;

    @Test
    void getUserStateByAuthenticationTokenTwiceProvingCacheWorks() {

        redisTemplate.delete("USER_STATE_GfsHbIMt49WjQ");

        WireMock.configureFor("localhost", 4553);
        stubFor(get("/opal/v2/users/0/state")
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(V2_USER_STATE)));

        Jwt jwt = Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("sub", "GfsHbIMt49WjQ")
            .claim("name", "Pablo")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();

        UserStateV2 userStateFromUserServiceStub = userStateClientService.getUserStateByAuthenticationToken(jwt).get();
        assertThat(userStateFromUserServiceStub.getName()).isEqualTo("Pablo");

        //we have only a stub user service so need to update cache ourselves
        UserStateV2 fakeCachedUserState = objectMapper.readValue(V2_USER_STATE, UserStateV2.class);
        fakeCachedUserState.setName("Pablo-CACHED");
        String fakeCachedUserStateJson = objectMapper.writeValueAsString(fakeCachedUserState);
        redisTemplate.opsForValue().set("USER_STATE_GfsHbIMt49WjQ", fakeCachedUserStateJson);

        UserStateV2 userStateFromCache = userStateClientService.getUserStateByAuthenticationToken(jwt).get();
        assertThat(userStateFromCache.getName()).isEqualTo("Pablo-CACHED");
    }
}
