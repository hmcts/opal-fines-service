package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyDocumentSearchResults;
import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyDocumentServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyDocumentService legacyDocumentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyDocumentService = spy(new LegacyDocumentService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetDocumentService() {
        String id = "1";
        DocumentEntity expectedEntity = new DocumentEntity();
        doReturn(expectedEntity).when(legacyDocumentService).postToGateway(anyString(), any(), anyString());

        DocumentEntity result = legacyDocumentService.getDocument(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchDocumentServices() {
        DocumentSearchDto criteria = DocumentSearchDto.builder().build();
        List<DocumentEntity> expectedEntities = Collections.singletonList(new DocumentEntity());
        LegacyDocumentSearchResults searchResults = LegacyDocumentSearchResults.builder().build();
        searchResults.setDocumentEntities(expectedEntities);
        doReturn(searchResults).when(legacyDocumentService).postToGateway(anyString(), any(), any());

        List<DocumentEntity> result = legacyDocumentService.searchDocuments(criteria);

        assertEquals(expectedEntities, result);
    }
}
