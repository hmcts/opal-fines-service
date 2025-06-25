package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyTillSearchResults;
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyTillServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyTillService legacyTillService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyTillService = spy(new LegacyTillService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetTill() {
        long id = 1L;
        TillEntity expectedEntity = new TillEntity();
        doReturn(expectedEntity).when(legacyTillService).postToGateway(anyString(), any(), anyLong());

        TillEntity result = legacyTillService.getTill(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchTills() {
        TillSearchDto criteria = TillSearchDto.builder().build();
        List<TillEntity> expectedEntities = Collections.singletonList(new TillEntity());
        LegacyTillSearchResults searchResults = LegacyTillSearchResults.builder().build();
        searchResults.setTillEntities(expectedEntities);
        doReturn(searchResults).when(legacyTillService).postToGateway(anyString(), any(), any());

        List<TillEntity> result = legacyTillService.searchTills(criteria);

        assertEquals(expectedEntities, result);
    }
}
