package uk.gov.hmcts.opal.controllers.develop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.search.UserSearchDto;
import uk.gov.hmcts.opal.entity.UserEntity;
import uk.gov.hmcts.opal.service.UserServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/user")
@Slf4j(topic = "UserController")
@Tag(name = "User Controller")
public class UserController {

    private final UserServiceInterface userService;

    public UserController(@Qualifier("userService") UserServiceInterface userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/{userId}")
    @Operation(summary = "Returns the User for the given userId.")
    public ResponseEntity<UserEntity> getUserById(@PathVariable String userId) {

        log.info(":GET:getUserById: userId: {}", userId);

        UserEntity response = userService.getUser(userId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Users based upon criteria in request body")
    public ResponseEntity<List<UserEntity>> postUsersSearch(@RequestBody UserSearchDto criteria) {
        log.info(":POST:postUsersSearch: query: \n{}", criteria);

        List<UserEntity> response = userService.searchUsers(criteria);

        log.info(":POST:postUsersSearch: response count: {}", response.size());

        return buildResponse(response);
    }


}
