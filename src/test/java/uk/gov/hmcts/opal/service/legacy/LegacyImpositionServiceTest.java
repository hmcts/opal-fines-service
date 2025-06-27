package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyImpositionSearchResults;
import uk.gov.hmcts.opal.dto.search.ImpositionSearchDto;
import uk.gov.hmcts.opal.entity.ImpositionEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyImpositionServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyImpositionService legacyImpositionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyImpositionService = spy(new LegacyImpositionService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetImposition() {
        long id = 1L;
        ImpositionEntity expectedEntity = new ImpositionEntity();
        doReturn(expectedEntity).when(legacyImpositionService).postToGateway(anyString(), any(), anyLong());

        ImpositionEntity result = legacyImpositionService.getImposition(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchImpositions() {
        ImpositionSearchDto criteria = ImpositionSearchDto.builder().build();
        List<ImpositionEntity> expectedEntities = Collections.singletonList(new ImpositionEntity());
        LegacyImpositionSearchResults searchResults = LegacyImpositionSearchResults.builder().build();
        searchResults.setImpositionEntities(expectedEntities);
        doReturn(searchResults).when(legacyImpositionService).postToGateway(anyString(), any(), any());

        List<ImpositionEntity> result = legacyImpositionService.searchImpositions(criteria);

        assertEquals(expectedEntities, result);
    }
}
