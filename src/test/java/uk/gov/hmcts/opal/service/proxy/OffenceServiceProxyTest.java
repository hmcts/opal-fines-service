package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.OffenceEntity;
import uk.gov.hmcts.opal.service.OffenceServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyOffenceService;
import uk.gov.hmcts.opal.service.opal.OffenceService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OffenceServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private OffenceService opalService;

    @Mock
    private LegacyOffenceService legacyService;

    @InjectMocks
    private OffenceServiceProxy offenceServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(OffenceServiceInterface targetService, OffenceServiceInterface otherService) {
        testGetOffence(targetService, otherService);
        testSearchOffences(targetService, otherService);
    }

    void testGetOffence(OffenceServiceInterface targetService, OffenceServiceInterface otherService) {
        // Given: a OffenceEntity is returned from the target service
        OffenceEntity entity = OffenceEntity.builder().build();
        when(targetService.getOffence(anyLong())).thenReturn(entity);

        // When: getOffence is called on the proxy
        OffenceEntity offenceResult = offenceServiceProxy.getOffence(1L);

        // Then: target service should be used, and the returned offence should be as expected
        verify(targetService).getOffence((short)1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, offenceResult);
    }

    void testSearchOffences(OffenceServiceInterface targetService, OffenceServiceInterface otherService) {
        // Given: an offences list result is returned from the target service
        OffenceEntity entity = OffenceEntity.builder().build();
        List<OffenceEntity> offencesList = List.of(entity);
        when(targetService.searchOffences(any())).thenReturn(offencesList);

        // When: searchOffences is called on the proxy
        OffenceSearchDto criteria = OffenceSearchDto.builder().build();
        List<OffenceEntity> listResult = offenceServiceProxy.searchOffences(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchOffences(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(offencesList, listResult);
    }

    @Test
    void shouldUseOpalOffenceServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyOffenceServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
