package uk.gov.hmcts.opal.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.opal.client.user.dto.UserStateDto;
import uk.gov.hmcts.opal.config.FeignClientConfig;

@FeignClient(
    name = "userService",
    url = "${user.service.url}",
    configuration = FeignClientConfig.class
)
public interface UserClient {
    @GetMapping("/users/{id}/state")
    UserStateDto getUserStateById(@PathVariable("id") Long id);
}
