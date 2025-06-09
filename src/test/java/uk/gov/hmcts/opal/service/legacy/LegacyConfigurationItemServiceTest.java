package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyConfigurationItemSearchResults;
import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyConfigurationItemServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyConfigurationItemService legacyConfigurationItemService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyConfigurationItemService = spy(new LegacyConfigurationItemService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetConfigurationItem() {
        long id = 1L;
        ConfigurationItemEntity expectedEntity = new ConfigurationItemEntity();
        doReturn(expectedEntity).when(legacyConfigurationItemService).postToGateway(anyString(), any(), anyLong());

        ConfigurationItemEntity result = legacyConfigurationItemService.getConfigurationItem(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchConfigurationItems() {
        ConfigurationItemSearchDto criteria = ConfigurationItemSearchDto.builder().build();
        List<ConfigurationItemEntity> expectedEntities = Collections.singletonList(new ConfigurationItemEntity());
        LegacyConfigurationItemSearchResults searchResults = LegacyConfigurationItemSearchResults.builder().build();
        searchResults.setConfigurationItemEntities(expectedEntities);
        doReturn(searchResults).when(legacyConfigurationItemService).postToGateway(anyString(), any(), any());

        List<ConfigurationItemEntity> result = legacyConfigurationItemService.searchConfigurationItems(criteria);

        assertEquals(expectedEntities, result);
    }
}
