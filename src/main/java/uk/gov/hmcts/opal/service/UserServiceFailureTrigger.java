package uk.gov.hmcts.opal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UserServiceFailureTrigger {

    private static final String FAKE_TOKEN = "fake-token";

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserServiceFailureTrigger(RestTemplate restTemplate, @Value("${user.service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    public void triggerUserStateCallWithFakeJwt() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(FAKE_TOKEN);

        String uri = UriComponentsBuilder.fromUriString(userServiceUrl)
            .path("/v2/users/0/state")
            .build()
            .toUriString();

        restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }
}
