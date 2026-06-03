package uk.gov.hmcts.opal.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.opal.support.UserStateStub.USER_STATE_MAPPER;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Instant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.UserStateClientServiceIT")
class UserStateClientServiceIT extends AbstractIntegrationTest {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    UserStateClientService userStateClientService;


    @SneakyThrows
    @Test
    @JiraStory("PO-2833")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-6321")
    void getUserStateByAuthenticationTokenTwiceProvingCacheWorks() {

        WireMock.configureFor("localhost", 4553);
        stubFor(get("/opal/v2/users/0/state")
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(userStateStub.getUserStateAsJson())));

        WireMock.verify(0, getRequestedFor(urlEqualTo("/opal/v2/users/0/state")));

        Jwt jwt = Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("sub", "GfsHbIMt49WjQ")
            .claim("name", "Pablo")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();

        // First Call - user service used
        UserStateV2 userStateFromUserServiceStub = userStateClientService.getUserStateByAuthenticationToken(jwt)
            .orElseThrow();
        assertThat(userStateFromUserServiceStub.getName()).isEqualTo("Pablo");
        WireMock.verify(1, getRequestedFor(urlEqualTo("/opal/v2/users/0/state")));

        //we have only a stub user service so need to update cache ourselves
        UserStateV2 userStateV2 = userStateStub.getDefaultUserStateBuilder()
            .name("Pablo-CACHED")
            .build();
        String fakeCachedUserStateJson = USER_STATE_MAPPER.writeValueAsString(userStateV2);
        redisTemplate.opsForValue().set("USER_STATE_GfsHbIMt49WjQ", fakeCachedUserStateJson);

        // Second Call - cache should be used
        UserStateV2 userStateFromCache = userStateClientService.getUserStateByAuthenticationToken(jwt)
            .orElseThrow();
        assertThat(userStateFromCache.getName()).isEqualTo("Pablo-CACHED");
        WireMock.verify(1, getRequestedFor(urlEqualTo("/opal/v2/users/0/state")));
    }
}
