package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyCourtSearchResults;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyCourtServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyCourtService legacyCourtService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyCourtService = spy(new LegacyCourtService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetCourt() {
        long id = 1L;
        CourtEntity expectedEntity = new CourtEntity();
        doReturn(expectedEntity).when(legacyCourtService).postToGateway(anyString(), any(), anyLong());

        CourtEntity result = legacyCourtService.getCourt(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchCourts() {
        CourtSearchDto criteria = CourtSearchDto.builder().build();
        List<CourtEntity> expectedEntities = Collections.singletonList(new CourtEntity());
        LegacyCourtSearchResults searchResults = LegacyCourtSearchResults.builder().build();
        searchResults.setCourtEntities(expectedEntities);
        doReturn(searchResults).when(legacyCourtService).postToGateway(anyString(), any(), any());

        List<CourtEntity> result = legacyCourtService.searchCourts(criteria);

        assertEquals(expectedEntities, result);
    }
}
