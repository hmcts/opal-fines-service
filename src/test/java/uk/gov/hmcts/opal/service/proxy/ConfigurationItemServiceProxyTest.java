package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.ConfigurationItemLite;
import uk.gov.hmcts.opal.service.ConfigurationItemServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyConfigurationItemService;
import uk.gov.hmcts.opal.service.opal.ConfigurationItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConfigurationItemServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private ConfigurationItemService opalService;

    @Mock
    private LegacyConfigurationItemService legacyService;

    @InjectMocks
    private ConfigurationItemServiceProxy configurationItemServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(ConfigurationItemServiceInterface targetService, ConfigurationItemServiceInterface otherService) {
        testGetConfigurationItem(targetService, otherService);
        testSearchConfigurationItems(targetService, otherService);
    }

    void testGetConfigurationItem(ConfigurationItemServiceInterface targetService,
                                  ConfigurationItemServiceInterface otherService) {
        // Given: a ConfigurationItemEntity is returned from the target service
        ConfigurationItemLite entity = ConfigurationItemLite.builder().build();
        when(targetService.getConfigurationItem(anyLong())).thenReturn(entity);

        // When: getConfigurationItem is called on the proxy
        ConfigurationItemLite configurationItemResult = configurationItemServiceProxy.getConfigurationItem(1);

        // Then: target service should be used, and the returned configurationItem should be as expected
        verify(targetService).getConfigurationItem(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, configurationItemResult);
    }

    void testSearchConfigurationItems(ConfigurationItemServiceInterface targetService,
                                      ConfigurationItemServiceInterface otherService) {
        // Given: a configurationItems list result is returned from the target service
        ConfigurationItemLite entity = ConfigurationItemLite.builder().build();
        List<ConfigurationItemLite> configurationItemsList = List.of(entity);
        when(targetService.searchConfigurationItems(any())).thenReturn(configurationItemsList);

        // When: searchConfigurationItems is called on the proxy
        ConfigurationItemSearchDto criteria = ConfigurationItemSearchDto.builder().build();
        List<ConfigurationItemLite> listResult = configurationItemServiceProxy.searchConfigurationItems(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchConfigurationItems(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(configurationItemsList, listResult);
    }

    @Test
    void shouldUseOpalConfigurationItemServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyConfigurationItemServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
