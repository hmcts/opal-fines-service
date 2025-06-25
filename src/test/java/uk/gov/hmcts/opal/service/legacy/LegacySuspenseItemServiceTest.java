package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacySuspenseItemSearchResults;
import uk.gov.hmcts.opal.dto.search.SuspenseItemSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacySuspenseItemServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacySuspenseItemService legacySuspenseItemService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacySuspenseItemService = spy(new LegacySuspenseItemService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetSuspenseItem() {
        long id = 1L;
        SuspenseItemEntity expectedEntity = new SuspenseItemEntity();
        doReturn(expectedEntity).when(legacySuspenseItemService).postToGateway(anyString(), any(), anyLong());

        SuspenseItemEntity result = legacySuspenseItemService.getSuspenseItem(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchSuspenseItems() {
        SuspenseItemSearchDto criteria = SuspenseItemSearchDto.builder().build();
        List<SuspenseItemEntity> expectedEntities = Collections.singletonList(new SuspenseItemEntity());
        LegacySuspenseItemSearchResults searchResults = LegacySuspenseItemSearchResults.builder().build();
        searchResults.setSuspenseItemEntities(expectedEntities);
        doReturn(searchResults).when(legacySuspenseItemService).postToGateway(anyString(), any(), any());

        List<SuspenseItemEntity> result = legacySuspenseItemService.searchSuspenseItems(criteria);

        assertEquals(expectedEntities, result);
    }
}
