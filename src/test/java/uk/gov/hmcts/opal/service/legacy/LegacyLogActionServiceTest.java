package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyLogActionSearchResults;
import uk.gov.hmcts.opal.dto.search.LogActionSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyLogActionServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyLogActionService legacyLogActionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyLogActionService = spy(new LegacyLogActionService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetLogAction() {
        short id = 1;
        LogActionEntity expectedEntity = new LogActionEntity();
        doReturn(expectedEntity).when(legacyLogActionService).postToGateway(anyString(), any(), anyShort());

        LogActionEntity result = legacyLogActionService.getLogAction(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchLogActions() {
        LogActionSearchDto criteria = LogActionSearchDto.builder().build();
        List<LogActionEntity> expectedEntities = Collections.singletonList(new LogActionEntity());
        LegacyLogActionSearchResults searchResults = LegacyLogActionSearchResults.builder().build();
        searchResults.setLogActionEntities(expectedEntities);
        doReturn(searchResults).when(legacyLogActionService).postToGateway(anyString(), any(), any());

        List<LogActionEntity> result = legacyLogActionService.searchLogActions(criteria);

        assertEquals(expectedEntities, result);
    }
}
