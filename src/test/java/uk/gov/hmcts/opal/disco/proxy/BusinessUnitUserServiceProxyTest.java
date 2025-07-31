package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.BusinessUnitUserSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.disco.BusinessUnitUserServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyBusinessUnitUserService;
import uk.gov.hmcts.opal.disco.opal.BusinessUnitUserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BusinessUnitUserServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private BusinessUnitUserService opalService;

    @Mock
    private LegacyBusinessUnitUserService legacyService;

    @InjectMocks
    private BusinessUnitUserServiceProxy businessUnitUserServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(BusinessUnitUserServiceInterface targetService, BusinessUnitUserServiceInterface otherService) {
        testGetBusinessUnitUser(targetService, otherService);
        testSearchBusinessUnitUsers(targetService, otherService);
    }

    void testGetBusinessUnitUser(BusinessUnitUserServiceInterface targetService,
                                 BusinessUnitUserServiceInterface otherService) {
        // Given: a BusinessUnitUserEntity is returned from the target service
        BusinessUnitUserEntity entity = BusinessUnitUserEntity.builder().build();
        when(targetService.getBusinessUnitUser(anyString())).thenReturn(entity);

        // When: getBusinessUnitUser is called on the proxy
        BusinessUnitUserEntity businessUnitUserResult = businessUnitUserServiceProxy.getBusinessUnitUser("1");

        // Then: target service should be used, and the returned businessUnitUser should be as expected
        verify(targetService).getBusinessUnitUser("1");
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, businessUnitUserResult);
    }

    void testSearchBusinessUnitUsers(BusinessUnitUserServiceInterface targetService,
                                     BusinessUnitUserServiceInterface otherService) {
        // Given: a businessUnitUsers list result is returned from the target service
        BusinessUnitUserEntity entity = BusinessUnitUserEntity.builder().build();
        List<BusinessUnitUserEntity> businessUnitUsersList = List.of(entity);
        when(targetService.searchBusinessUnitUsers(any())).thenReturn(businessUnitUsersList);

        // When: searchBusinessUnitUsers is called on the proxy
        BusinessUnitUserSearchDto criteria = BusinessUnitUserSearchDto.builder().build();
        List<BusinessUnitUserEntity> listResult = businessUnitUserServiceProxy.searchBusinessUnitUsers(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchBusinessUnitUsers(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(businessUnitUsersList, listResult);
    }

    @Test
    void shouldUseOpalBusinessUnitUserServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyBusinessUnitUserServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
