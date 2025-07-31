package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.ApplicationFunctionSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;
import uk.gov.hmcts.opal.disco.ApplicationFunctionServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyApplicationFunctionService;
import uk.gov.hmcts.opal.disco.opal.ApplicationFunctionService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ApplicationFunctionServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private ApplicationFunctionService opalService;

    @Mock
    private LegacyApplicationFunctionService legacyService;

    @InjectMocks
    private ApplicationFunctionServiceProxy applicationFunctionServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(ApplicationFunctionServiceInterface targetService, ApplicationFunctionServiceInterface otherService) {
        testGetApplicationFunction(targetService, otherService);
        testSearchApplicationFunctions(targetService, otherService);
    }

    void testGetApplicationFunction(ApplicationFunctionServiceInterface targetService,
                                    ApplicationFunctionServiceInterface otherService) {
        // Given: a ApplicationFunctionEntity is returned from the target service
        ApplicationFunctionEntity entity = ApplicationFunctionEntity.builder().build();
        when(targetService.getApplicationFunction(anyLong())).thenReturn(entity);

        // When: getApplicationFunction is called on the proxy
        ApplicationFunctionEntity applicationFunctionResult = applicationFunctionServiceProxy
            .getApplicationFunction(1);

        // Then: target service should be used, and the returned applicationFunction should be as expected
        verify(targetService).getApplicationFunction(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, applicationFunctionResult);
    }

    void testSearchApplicationFunctions(ApplicationFunctionServiceInterface targetService,
                                        ApplicationFunctionServiceInterface otherService) {
        // Given: a applicationFunctions list result is returned from the target service
        ApplicationFunctionEntity entity = ApplicationFunctionEntity.builder().build();
        List<ApplicationFunctionEntity> applicationFunctionsList = List.of(entity);
        when(targetService.searchApplicationFunctions(any())).thenReturn(applicationFunctionsList);

        // When: searchApplicationFunctions is called on the proxy
        ApplicationFunctionSearchDto criteria = ApplicationFunctionSearchDto.builder().build();
        List<ApplicationFunctionEntity> listResult = applicationFunctionServiceProxy
            .searchApplicationFunctions(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchApplicationFunctions(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(applicationFunctionsList, listResult);
    }

    @Test
    void shouldUseOpalApplicationFunctionServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyApplicationFunctionServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
