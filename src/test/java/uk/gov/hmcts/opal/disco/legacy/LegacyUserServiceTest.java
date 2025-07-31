package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyUserSearchResults;
import uk.gov.hmcts.opal.dto.search.UserSearchDto;
import uk.gov.hmcts.opal.entity.UserEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyUserServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyUserService legacyUserService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyUserService = spy(new LegacyUserService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetUser() {
        String id = "1";
        UserEntity expectedEntity = new UserEntity();
        doReturn(expectedEntity).when(legacyUserService).postToGateway(anyString(), any(), anyString());

        UserEntity result = legacyUserService.getUser(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchUsers() {
        UserSearchDto criteria = UserSearchDto.builder().build();
        List<UserEntity> expectedEntities = Collections.singletonList(new UserEntity());
        LegacyUserSearchResults searchResults = LegacyUserSearchResults.builder().build();
        searchResults.setUserEntities(expectedEntities);
        doReturn(searchResults).when(legacyUserService).postToGateway(anyString(), any(), any());

        List<UserEntity> result = legacyUserService.searchUsers(criteria);

        assertEquals(expectedEntities, result);
    }
}
