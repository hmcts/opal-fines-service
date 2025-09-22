package uk.gov.hmcts.opal.common.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.common.user.client.dto.UserStateDto;
import uk.gov.hmcts.opal.config.FeignClientConfig;

@FeignClient(
    name = "userService",
    url = "${user.service.url}",
    configuration = FeignClientConfig.class
)
public interface UserClient {

    static final String X_USER_EMAIL = "X-User-Email";

    @GetMapping("/users/{id}/state")
    UserStateDto getUserStateById(@PathVariable("id") Long id);

    @GetMapping("/testing-support/token/test-user")
    SecurityToken getTestUserToken();

    @GetMapping("/testing-support/token/user")
    SecurityToken getTestUserToken(@RequestHeader(value = X_USER_EMAIL) String userEmail);
}
