package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyTemplateMappingSearchResults;
import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyTemplateMappingServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyTemplateMappingService legacyTemplateMappingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyTemplateMappingService = spy(new LegacyTemplateMappingService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetTemplateMapping() {
        long templateId = 1L;
        long applicationFunctionId = 2L;
        TemplateMappingEntity expectedEntity = new TemplateMappingEntity();
        doReturn(expectedEntity).when(legacyTemplateMappingService).postToGateway(anyString(), any(), anyLong());

        TemplateMappingEntity result = legacyTemplateMappingService.getTemplateMapping(templateId,
                                                                                       applicationFunctionId);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchTemplateMappings() {
        TemplateMappingSearchDto criteria = TemplateMappingSearchDto.builder().build();
        List<TemplateMappingEntity> expectedEntities = Collections.singletonList(new TemplateMappingEntity());
        LegacyTemplateMappingSearchResults searchResults = LegacyTemplateMappingSearchResults.builder().build();
        searchResults.setTemplateMappingEntities(expectedEntities);
        doReturn(searchResults).when(legacyTemplateMappingService).postToGateway(anyString(), any(), any());

        List<TemplateMappingEntity> result = legacyTemplateMappingService.searchTemplateMappings(criteria);

        assertEquals(expectedEntities, result);
    }
}
