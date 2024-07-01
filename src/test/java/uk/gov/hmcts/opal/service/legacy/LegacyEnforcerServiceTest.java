package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyEnforcerSearchResults;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyEnforcerServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyEnforcerService legacyEnforcerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyEnforcerService = spy(new LegacyEnforcerService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetEnforcer() {
        long id = 1L;
        EnforcerEntity expectedEntity = new EnforcerEntity();
        doReturn(expectedEntity).when(legacyEnforcerService).postToGateway(anyString(), any(), anyLong());

        EnforcerEntity result = legacyEnforcerService.getEnforcer(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchEnforcers() {
        EnforcerSearchDto criteria = EnforcerSearchDto.builder().build();
        List<EnforcerEntity> expectedEntities = Collections.singletonList(new EnforcerEntity());
        LegacyEnforcerSearchResults searchResults = LegacyEnforcerSearchResults.builder().build();
        searchResults.setEnforcerEntities(expectedEntities);
        doReturn(searchResults).when(legacyEnforcerService).postToGateway(anyString(), any(), any());

        List<EnforcerEntity> result = legacyEnforcerService.searchEnforcers(criteria);

        assertEquals(expectedEntities, result);
    }
}
