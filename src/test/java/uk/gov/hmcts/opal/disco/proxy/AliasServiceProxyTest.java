package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.AliasSearchDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.disco.AliasServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyAliasService;
import uk.gov.hmcts.opal.disco.opal.AliasService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AliasServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private AliasService opalService;

    @Mock
    private LegacyAliasService legacyService;

    @InjectMocks
    private AliasServiceProxy aliasServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(AliasServiceInterface targetService, AliasServiceInterface otherService) {
        testGetAlias(targetService, otherService);
        testSearchAliass(targetService, otherService);
    }

    void testGetAlias(AliasServiceInterface targetService, AliasServiceInterface otherService) {
        // Given: a AliasEntity is returned from the target service
        AliasEntity entity = AliasEntity.builder().build();
        when(targetService.getAlias(anyLong())).thenReturn(entity);

        // When: getAlias is called on the proxy
        AliasEntity aliasResult = aliasServiceProxy.getAlias(1);

        // Then: target service should be used, and the returned alias should be as expected
        verify(targetService).getAlias(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, aliasResult);
    }

    void testSearchAliass(AliasServiceInterface targetService, AliasServiceInterface otherService) {
        // Given: a aliass list result is returned from the target service
        AliasEntity entity = AliasEntity.builder().build();
        List<AliasEntity> aliassList = List.of(entity);
        when(targetService.searchAliass(any())).thenReturn(aliassList);

        // When: searchAliass is called on the proxy
        AliasSearchDto criteria = AliasSearchDto.builder().build();
        List<AliasEntity> listResult = aliasServiceProxy.searchAliass(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchAliass(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(aliassList, listResult);
    }

    @Test
    void shouldUseOpalAliasServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyAliasServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
