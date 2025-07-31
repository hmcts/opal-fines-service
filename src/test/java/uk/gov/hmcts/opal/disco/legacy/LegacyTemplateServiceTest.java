package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyTemplateSearchResults;
import uk.gov.hmcts.opal.dto.search.TemplateSearchDto;
import uk.gov.hmcts.opal.entity.TemplateEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyTemplateServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyTemplateService legacyTemplateService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyTemplateService = spy(new LegacyTemplateService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetTemplate() {
        long id = 1L;
        TemplateEntity expectedEntity = new TemplateEntity();
        doReturn(expectedEntity).when(legacyTemplateService).postToGateway(anyString(), any(), anyLong());

        TemplateEntity result = legacyTemplateService.getTemplate(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchTemplates() {
        TemplateSearchDto criteria = TemplateSearchDto.builder().build();
        List<TemplateEntity> expectedEntities = Collections.singletonList(new TemplateEntity());
        LegacyTemplateSearchResults searchResults = LegacyTemplateSearchResults.builder().build();
        searchResults.setTemplateEntities(expectedEntities);
        doReturn(searchResults).when(legacyTemplateService).postToGateway(anyString(), any(), any());

        List<TemplateEntity> result = legacyTemplateService.searchTemplates(criteria);

        assertEquals(expectedEntities, result);
    }
}
