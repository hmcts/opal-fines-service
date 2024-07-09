package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyResultSearchResults;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.ResultEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyResultServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyResultService legacyResultService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyResultService = spy(new LegacyResultService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetResult() {
        long id = 1L;
        ResultEntity expectedEntity = new ResultEntity();
        doReturn(expectedEntity).when(legacyResultService).postToGateway(anyString(), any(), anyLong());

        ResultEntity result = legacyResultService.getResult(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchResults() {
        ResultSearchDto criteria = ResultSearchDto.builder().build();
        List<ResultEntity> expectedEntities = Collections.singletonList(new ResultEntity());
        LegacyResultSearchResults searchResults = LegacyResultSearchResults.builder().build();
        searchResults.setResultEntities(expectedEntities);
        doReturn(searchResults).when(legacyResultService).postToGateway(anyString(), any(), any());

        List<ResultEntity> result = legacyResultService.searchResults(criteria);

        assertEquals(expectedEntities, result);
    }
}
