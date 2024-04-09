package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.OffenseSearchDto;
import uk.gov.hmcts.opal.entity.OffenseEntity;
import uk.gov.hmcts.opal.service.OffenseServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyOffenseService;
import uk.gov.hmcts.opal.service.opal.OffenseService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OffenseServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private OffenseService opalService;

    @Mock
    private LegacyOffenseService legacyService;

    @InjectMocks
    private OffenseServiceProxy offenseServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(OffenseServiceInterface targetService, OffenseServiceInterface otherService) {
        testGetOffense(targetService, otherService);
        testSearchOffenses(targetService, otherService);
    }

    void testGetOffense(OffenseServiceInterface targetService, OffenseServiceInterface otherService) {
        // Given: a OffenseEntity is returned from the target service
        OffenseEntity entity = OffenseEntity.builder().build();
        when(targetService.getOffense(anyLong())).thenReturn(entity);

        // When: getOffense is called on the proxy
        OffenseEntity offenseResult = offenseServiceProxy.getOffense(1);

        // Then: target service should be used, and the returned offense should be as expected
        verify(targetService).getOffense(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, offenseResult);
    }

    void testSearchOffenses(OffenseServiceInterface targetService, OffenseServiceInterface otherService) {
        // Given: a offenses list result is returned from the target service
        OffenseEntity entity = OffenseEntity.builder().build();
        List<OffenseEntity> offensesList = List.of(entity);
        when(targetService.searchOffenses(any())).thenReturn(offensesList);

        // When: searchOffenses is called on the proxy
        OffenseSearchDto criteria = OffenseSearchDto.builder().build();
        List<OffenseEntity> listResult = offenseServiceProxy.searchOffenses(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchOffenses(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(offensesList, listResult);
    }

    @Test
    void shouldUseOpalOffenseServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyOffenseServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
