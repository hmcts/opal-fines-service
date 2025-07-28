package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.WarrantRegisterSearchDto;
import uk.gov.hmcts.opal.entity.WarrantRegisterEntity;
import uk.gov.hmcts.opal.disco.WarrantRegisterServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyWarrantRegisterService;
import uk.gov.hmcts.opal.disco.opal.WarrantRegisterService;
import uk.gov.hmcts.opal.service.proxy.ProxyTestsBase;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WarrantRegisterServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private WarrantRegisterService opalService;

    @Mock
    private LegacyWarrantRegisterService legacyService;

    @InjectMocks
    private WarrantRegisterServiceProxy warrantRegisterServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(WarrantRegisterServiceInterface targetService, WarrantRegisterServiceInterface otherService) {
        testGetWarrantRegister(targetService, otherService);
        testSearchWarrantRegisters(targetService, otherService);
    }

    void testGetWarrantRegister(WarrantRegisterServiceInterface targetService,
                                WarrantRegisterServiceInterface otherService) {
        // Given: a WarrantRegisterEntity is returned from the target service
        WarrantRegisterEntity entity = WarrantRegisterEntity.builder().build();
        when(targetService.getWarrantRegister(anyLong())).thenReturn(entity);

        // When: getWarrantRegister is called on the proxy
        WarrantRegisterEntity warrantRegisterResult = warrantRegisterServiceProxy.getWarrantRegister(1);

        // Then: target service should be used, and the returned warrantRegister should be as expected
        verify(targetService).getWarrantRegister(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, warrantRegisterResult);
    }

    void testSearchWarrantRegisters(WarrantRegisterServiceInterface targetService,
                                    WarrantRegisterServiceInterface otherService) {
        // Given: a warrantRegisters list result is returned from the target service
        WarrantRegisterEntity entity = WarrantRegisterEntity.builder().build();
        List<WarrantRegisterEntity> warrantRegistersList = List.of(entity);
        when(targetService.searchWarrantRegisters(any())).thenReturn(warrantRegistersList);

        // When: searchWarrantRegisters is called on the proxy
        WarrantRegisterSearchDto criteria = WarrantRegisterSearchDto.builder().build();
        List<WarrantRegisterEntity> listResult = warrantRegisterServiceProxy.searchWarrantRegisters(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchWarrantRegisters(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(warrantRegistersList, listResult);
    }

    @Test
    void shouldUseOpalWarrantRegisterServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyWarrantRegisterServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
