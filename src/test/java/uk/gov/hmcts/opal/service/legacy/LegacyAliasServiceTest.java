package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAliasSearchResults;
import uk.gov.hmcts.opal.dto.search.AliasSearchDto;
import uk.gov.hmcts.opal.entity.AliasEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyAliasServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyAliasService legacyAliasService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyAliasService = spy(new LegacyAliasService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetAlias() {
        long id = 1L;
        AliasEntity expectedEntity = new AliasEntity();
        doReturn(expectedEntity).when(legacyAliasService).postToGateway(anyString(), any(), anyLong());

        AliasEntity result = legacyAliasService.getAlias(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchAliass() {
        AliasSearchDto criteria = AliasSearchDto.builder().build();
        List<AliasEntity> expectedEntities = Collections.singletonList(new AliasEntity());
        LegacyAliasSearchResults searchResults = LegacyAliasSearchResults.builder().build();
        searchResults.setAliasEntities(expectedEntities);
        doReturn(searchResults).when(legacyAliasService).postToGateway(anyString(), any(), any());

        List<AliasEntity> result = legacyAliasService.searchAliass(criteria);

        assertEquals(expectedEntities, result);
    }

}
