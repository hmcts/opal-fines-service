package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyPrisonSearchResults;
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyPrisonServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyPrisonService legacyPrisonService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyPrisonService = spy(new LegacyPrisonService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetPrison() {
        long id = 1L;
        PrisonEntity expectedEntity = new PrisonEntity();
        doReturn(expectedEntity).when(legacyPrisonService).postToGateway(anyString(), any(), anyLong());

        PrisonEntity result = legacyPrisonService.getPrison(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchPrisons() {
        PrisonSearchDto criteria = PrisonSearchDto.builder().build();
        List<PrisonEntity> expectedEntities = Collections.singletonList(new PrisonEntity());
        LegacyPrisonSearchResults searchResults = LegacyPrisonSearchResults.builder().build();
        searchResults.setPrisonEntities(expectedEntities);
        doReturn(searchResults).when(legacyPrisonService).postToGateway(anyString(), any(), any());

        List<PrisonEntity> result = legacyPrisonService.searchPrisons(criteria);

        assertEquals(expectedEntities, result);
    }
}
