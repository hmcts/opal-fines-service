package uk.gov.hmcts.opal.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.client.user.service.UserStateClientService;

import java.util.Optional;

@RestController
public class TempUserServiceController {

    private final UserStateClientService userStateClientService;

    public TempUserServiceController(UserStateClientService userStateClientService) {
        this.userStateClientService = userStateClientService;
    }

    @GetMapping(value = "/user-client-test/{userId}")
    public Optional<UserState> getUserState(@PathVariable Long userId) {

        return userStateClientService.getUserState(userId);

    }

}
