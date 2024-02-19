package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.service.LocalJusticeAreaServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyLocalJusticeAreaService;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LocalJusticeAreaServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private LocalJusticeAreaService opalService;

    @Mock
    private LegacyLocalJusticeAreaService legacyService;

    @InjectMocks
    private LocalJusticeAreaServiceProxy localJusticeAreaServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(LocalJusticeAreaServiceInterface targetService, LocalJusticeAreaServiceInterface otherService) {
        testGetLocalJusticeArea(targetService, otherService);
        testSearchLocalJusticeAreas(targetService, otherService);
    }

    void testGetLocalJusticeArea(LocalJusticeAreaServiceInterface targetService,
                                 LocalJusticeAreaServiceInterface otherService) {
        // Given: a LocalJusticeAreaEntity is returned from the target service
        LocalJusticeAreaEntity entity = LocalJusticeAreaEntity.builder().build();
        when(targetService.getLocalJusticeArea(anyShort())).thenReturn(entity);

        // When: getLocalJusticeArea is called on the proxy
        LocalJusticeAreaEntity localJusticeAreaResult = localJusticeAreaServiceProxy.getLocalJusticeArea((short)1);

        // Then: target service should be used, and the returned localJusticeArea should be as expected
        verify(targetService).getLocalJusticeArea((short)1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, localJusticeAreaResult);
    }

    void testSearchLocalJusticeAreas(LocalJusticeAreaServiceInterface targetService,
                                     LocalJusticeAreaServiceInterface otherService) {
        // Given: a localJusticeAreas list result is returned from the target service
        LocalJusticeAreaEntity entity = LocalJusticeAreaEntity.builder().build();
        List<LocalJusticeAreaEntity> localJusticeAreasList = List.of(entity);
        when(targetService.searchLocalJusticeAreas(any())).thenReturn(localJusticeAreasList);

        // When: searchLocalJusticeAreas is called on the proxy
        LocalJusticeAreaSearchDto criteria = LocalJusticeAreaSearchDto.builder().build();
        List<LocalJusticeAreaEntity> listResult = localJusticeAreaServiceProxy.searchLocalJusticeAreas(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchLocalJusticeAreas(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(localJusticeAreasList, listResult);
    }

    @Test
    void shouldUseOpalLocalJusticeAreaServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyLocalJusticeAreaServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
