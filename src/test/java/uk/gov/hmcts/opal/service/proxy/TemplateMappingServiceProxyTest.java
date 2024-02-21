package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity.MappingId;
import uk.gov.hmcts.opal.service.TemplateMappingServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyTemplateMappingService;
import uk.gov.hmcts.opal.service.opal.TemplateMappingService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TemplateMappingServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private TemplateMappingService opalService;

    @Mock
    private LegacyTemplateMappingService legacyService;

    @InjectMocks
    private TemplateMappingServiceProxy templateMappingServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(TemplateMappingServiceInterface targetService, TemplateMappingServiceInterface otherService) {
        testGetTemplateMapping(targetService, otherService);
        testSearchTemplateMappings(targetService, otherService);
    }

    void testGetTemplateMapping(TemplateMappingServiceInterface targetService,
                                TemplateMappingServiceInterface otherService) {
        // Given: a TemplateMappingEntity is returned from the target service
        TemplateMappingEntity entity = TemplateMappingEntity.builder().build();
        when(targetService.getTemplateMapping(any(MappingId.class))).thenReturn(entity);

        // When: getTemplateMapping is called on the proxy
        MappingId key = new MappingId(1L, 1L);
        TemplateMappingEntity templateMappingResult = templateMappingServiceProxy.getTemplateMapping(key);

        // Then: target service should be used, and the returned templateMapping should be as expected
        verify(targetService).getTemplateMapping(key);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, templateMappingResult);
    }

    void testSearchTemplateMappings(TemplateMappingServiceInterface targetService,
                                    TemplateMappingServiceInterface otherService) {
        // Given: a templateMappings list result is returned from the target service
        TemplateMappingEntity entity = TemplateMappingEntity.builder().build();
        List<TemplateMappingEntity> templateMappingsList = List.of(entity);
        when(targetService.searchTemplateMappings(any())).thenReturn(templateMappingsList);

        // When: searchTemplateMappings is called on the proxy
        TemplateMappingSearchDto criteria = TemplateMappingSearchDto.builder().build();
        List<TemplateMappingEntity> listResult = templateMappingServiceProxy.searchTemplateMappings(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchTemplateMappings(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(templateMappingsList, listResult);
    }

    @Test
    void shouldUseOpalTemplateMappingServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyTemplateMappingServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
