package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyDocumentInstanceSearchResults;
import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyDocumentInstanceServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyDocumentInstanceService legacyDocumentInstanceService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyDocumentInstanceService = spy(new LegacyDocumentInstanceService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetDocumentInstance() {
        long id = 1L;
        DocumentInstanceEntity expectedEntity = new DocumentInstanceEntity();
        doReturn(expectedEntity).when(legacyDocumentInstanceService).postToGateway(anyString(), any(), anyLong());

        DocumentInstanceEntity result = legacyDocumentInstanceService.getDocumentInstance(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchDocumentInstances() {
        DocumentInstanceSearchDto criteria = DocumentInstanceSearchDto.builder().build();
        List<DocumentInstanceEntity> expectedEntities = Collections.singletonList(new DocumentInstanceEntity());
        LegacyDocumentInstanceSearchResults searchResults = LegacyDocumentInstanceSearchResults.builder().build();
        searchResults.setDocumentInstanceEntities(expectedEntities);
        doReturn(searchResults).when(legacyDocumentInstanceService).postToGateway(anyString(), any(), any());

        List<DocumentInstanceEntity> result = legacyDocumentInstanceService.searchDocumentInstances(criteria);

        assertEquals(expectedEntities, result);
    }
}
