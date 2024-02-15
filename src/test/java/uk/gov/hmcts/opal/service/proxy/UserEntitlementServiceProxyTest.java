package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.UserEntitlementSearchDto;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity;
import uk.gov.hmcts.opal.service.UserEntitlementServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyUserEntitlementService;
import uk.gov.hmcts.opal.service.opal.UserEntitlementService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserEntitlementServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private UserEntitlementService opalService;

    @Mock
    private LegacyUserEntitlementService legacyService;

    @InjectMocks
    private UserEntitlementServiceProxy userEntitlementServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(UserEntitlementServiceInterface targetService, UserEntitlementServiceInterface otherService) {
        testGetUserEntitlement(targetService, otherService);
        testSearchUserEntitlements(targetService, otherService);
    }

    void testGetUserEntitlement(UserEntitlementServiceInterface targetService,
                                UserEntitlementServiceInterface otherService) {
        // Given: a UserEntitlementEntity is returned from the target service
        UserEntitlementEntity entity = UserEntitlementEntity.builder().build();
        when(targetService.getUserEntitlement(anyLong())).thenReturn(entity);

        // When: getUserEntitlement is called on the proxy
        UserEntitlementEntity userEntitlementResult = userEntitlementServiceProxy.getUserEntitlement(1);

        // Then: target service should be used, and the returned userEntitlement should be as expected
        verify(targetService).getUserEntitlement(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, userEntitlementResult);
    }

    void testSearchUserEntitlements(UserEntitlementServiceInterface targetService,
                                    UserEntitlementServiceInterface otherService) {
        // Given: a userEntitlements list result is returned from the target service
        UserEntitlementEntity entity = UserEntitlementEntity.builder().build();
        List<UserEntitlementEntity> userEntitlementsList = List.of(entity);
        when(targetService.searchUserEntitlements(any())).thenReturn(userEntitlementsList);

        // When: searchUserEntitlements is called on the proxy
        UserEntitlementSearchDto criteria = UserEntitlementSearchDto.builder().build();
        List<UserEntitlementEntity> listResult = userEntitlementServiceProxy.searchUserEntitlements(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchUserEntitlements(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(userEntitlementsList, listResult);
    }

    @Test
    void shouldUseOpalUserEntitlementServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyUserEntitlementServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
