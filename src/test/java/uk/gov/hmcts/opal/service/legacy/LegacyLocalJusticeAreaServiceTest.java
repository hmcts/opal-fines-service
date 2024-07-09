package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyLocalJusticeAreaSearchResults;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyLocalJusticeAreaServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyLocalJusticeAreaService legacyLocalJusticeAreaService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyLocalJusticeAreaService = spy(new LegacyLocalJusticeAreaService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetLocalJusticeArea() {
        short id = 1;
        LocalJusticeAreaEntity expectedEntity = new LocalJusticeAreaEntity();
        doReturn(expectedEntity).when(legacyLocalJusticeAreaService).postToGateway(anyString(), any(), anyShort());

        LocalJusticeAreaEntity result = legacyLocalJusticeAreaService.getLocalJusticeArea(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchLocalJusticeAreas() {
        LocalJusticeAreaSearchDto criteria = LocalJusticeAreaSearchDto.builder().build();
        List<LocalJusticeAreaEntity> expectedEntities = Collections.singletonList(new LocalJusticeAreaEntity());
        LegacyLocalJusticeAreaSearchResults searchResults = LegacyLocalJusticeAreaSearchResults.builder().build();
        searchResults.setLocalJusticeAreaEntities(expectedEntities);
        doReturn(searchResults).when(legacyLocalJusticeAreaService).postToGateway(anyString(), any(), any());

        List<LocalJusticeAreaEntity> result = legacyLocalJusticeAreaService.searchLocalJusticeAreas(criteria);

        assertEquals(expectedEntities, result);
    }
}
