package uk.gov.hmcts.opal.client.user.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.client.user.UserClient;
import uk.gov.hmcts.opal.client.user.dto.UserStateDto;
import uk.gov.hmcts.opal.client.user.mapper.UserStateMapper;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j

public class UserStateClientService {

    private final UserClient userClient;
    private final UserStateMapper userStateMapper;


    @Cacheable(value = "userState",
        key = "#userId == 0 ? T(org.springframework.security.core.context.SecurityContextHolder)"
           + ".getContext().getAuthentication().getName() : #userId")
    public Optional<UserState> getUserState(Long userId) {

        log.info("Fetching user state for specific userId: {}", userId);

        // Call the Feign client. Auth intercepted  - used to get authenticated user state if userId is 0.

        try {
            log.info("Fetching user state for userId: {}", userId);

            UserStateDto userStateDto = userClient.getUserStateById(userId);
            UserState userState = userStateMapper.toUserState(userStateDto);

            log.debug("Mapped UserState for userId {}: {}", userId, userState);
            return Optional.of(userState);

        } catch (FeignException.NotFound e) {
            log.warn("User not found in User Service for userId: {}", userId);
            return Optional.empty();
        }
    }

    public Optional<UserState> getUserStateByAuthenticatedUser() {
        return this.getUserState(0L);
    }
}
