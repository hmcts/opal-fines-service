package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.TemplateSearchDto;
import uk.gov.hmcts.opal.entity.TemplateEntity;
import uk.gov.hmcts.opal.service.TemplateServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyTemplateService;
import uk.gov.hmcts.opal.service.opal.TemplateService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TemplateServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private TemplateService opalService;

    @Mock
    private LegacyTemplateService legacyService;

    @InjectMocks
    private TemplateServiceProxy templateServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(TemplateServiceInterface targetService, TemplateServiceInterface otherService) {
        testGetTemplate(targetService, otherService);
        testSearchTemplates(targetService, otherService);
    }

    void testGetTemplate(TemplateServiceInterface targetService, TemplateServiceInterface otherService) {
        // Given: a TemplateEntity is returned from the target service
        TemplateEntity entity = TemplateEntity.builder().build();
        when(targetService.getTemplate(anyLong())).thenReturn(entity);

        // When: getTemplate is called on the proxy
        TemplateEntity templateResult = templateServiceProxy.getTemplate(1);

        // Then: target service should be used, and the returned template should be as expected
        verify(targetService).getTemplate(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, templateResult);
    }

    void testSearchTemplates(TemplateServiceInterface targetService, TemplateServiceInterface otherService) {
        // Given: a templates list result is returned from the target service
        TemplateEntity entity = TemplateEntity.builder().build();
        List<TemplateEntity> templatesList = List.of(entity);
        when(targetService.searchTemplates(any())).thenReturn(templatesList);

        // When: searchTemplates is called on the proxy
        TemplateSearchDto criteria = TemplateSearchDto.builder().build();
        List<TemplateEntity> listResult = templateServiceProxy.searchTemplates(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchTemplates(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(templatesList, listResult);
    }

    @Test
    void shouldUseOpalTemplateServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyTemplateServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
