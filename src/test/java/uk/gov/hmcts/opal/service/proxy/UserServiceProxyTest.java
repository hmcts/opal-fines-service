package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.UserSearchDto;
import uk.gov.hmcts.opal.entity.UserEntity;
import uk.gov.hmcts.opal.service.UserServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyUserService;
import uk.gov.hmcts.opal.service.opal.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private UserService opalService;

    @Mock
    private LegacyUserService legacyService;

    @InjectMocks
    private UserServiceProxy userServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(UserServiceInterface targetService, UserServiceInterface otherService) {
        testGetUser(targetService, otherService);
        testSearchUsers(targetService, otherService);
    }

    void testGetUser(UserServiceInterface targetService, UserServiceInterface otherService) {
        // Given: a UserEntity is returned from the target service
        UserEntity entity = UserEntity.builder().build();
        when(targetService.getUser(anyString())).thenReturn(entity);

        // When: getUser is called on the proxy
        UserEntity userResult = userServiceProxy.getUser("1");

        // Then: target service should be used, and the returned user should be as expected
        verify(targetService).getUser("1");
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, userResult);
    }

    void testSearchUsers(UserServiceInterface targetService, UserServiceInterface otherService) {
        // Given: a users list result is returned from the target service
        UserEntity entity = UserEntity.builder().build();
        List<UserEntity> usersList = List.of(entity);
        when(targetService.searchUsers(any())).thenReturn(usersList);

        // When: searchUsers is called on the proxy
        UserSearchDto criteria = UserSearchDto.builder().build();
        List<UserEntity> listResult = userServiceProxy.searchUsers(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchUsers(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(usersList, listResult);
    }

    @Test
    void shouldUseOpalUserServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyUserServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
