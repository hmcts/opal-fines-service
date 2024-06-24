package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.UserSearchDto;
import uk.gov.hmcts.opal.entity.UserEntity;
import uk.gov.hmcts.opal.service.UserServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyUserService")
public class LegacyUserService extends LegacyService implements UserServiceInterface {

    public LegacyUserService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public UserEntity getUser(String userId) {
        log.info("getUser for {} from {}", userId, legacyGateway.getUrl());
        return postToGateway("getUser", UserEntity.class, userId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UserEntity> searchUsers(UserSearchDto criteria) {
        log.info(":searchUsers: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchUsers", List.class, criteria);
    }

}
