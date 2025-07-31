package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.SuspenseItemSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.disco.SuspenseItemServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacySuspenseItemService;
import uk.gov.hmcts.opal.disco.opal.SuspenseItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SuspenseItemServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private SuspenseItemService opalService;

    @Mock
    private LegacySuspenseItemService legacyService;

    @InjectMocks
    private SuspenseItemServiceProxy suspenseItemServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(SuspenseItemServiceInterface targetService, SuspenseItemServiceInterface otherService) {
        testGetSuspenseItem(targetService, otherService);
        testSearchSuspenseItems(targetService, otherService);
    }

    void testGetSuspenseItem(SuspenseItemServiceInterface targetService, SuspenseItemServiceInterface otherService) {
        // Given: a SuspenseItemEntity is returned from the target service
        SuspenseItemEntity entity = SuspenseItemEntity.builder().build();
        when(targetService.getSuspenseItem(anyLong())).thenReturn(entity);

        // When: getSuspenseItem is called on the proxy
        SuspenseItemEntity suspenseItemResult = suspenseItemServiceProxy.getSuspenseItem(1);

        // Then: target service should be used, and the returned suspenseItem should be as expected
        verify(targetService).getSuspenseItem(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, suspenseItemResult);
    }

    void testSearchSuspenseItems(SuspenseItemServiceInterface targetService,
                                 SuspenseItemServiceInterface otherService) {
        // Given: a suspenseItems list result is returned from the target service
        SuspenseItemEntity entity = SuspenseItemEntity.builder().build();
        List<SuspenseItemEntity> suspenseItemsList = List.of(entity);
        when(targetService.searchSuspenseItems(any())).thenReturn(suspenseItemsList);

        // When: searchSuspenseItems is called on the proxy
        SuspenseItemSearchDto criteria = SuspenseItemSearchDto.builder().build();
        List<SuspenseItemEntity> listResult = suspenseItemServiceProxy.searchSuspenseItems(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchSuspenseItems(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(suspenseItemsList, listResult);
    }

    @Test
    void shouldUseOpalSuspenseItemServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacySuspenseItemServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
