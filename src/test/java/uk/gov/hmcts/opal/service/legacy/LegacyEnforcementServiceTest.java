package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyEnforcementSearchResults;
import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyEnforcementServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyEnforcementService legacyEnforcementService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyEnforcementService = spy(new LegacyEnforcementService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetEnforcement() {
        long id = 1L;
        EnforcementEntity expectedEntity = new EnforcementEntity();
        doReturn(expectedEntity).when(legacyEnforcementService).postToGateway(anyString(), any(), anyLong());

        EnforcementEntity result = legacyEnforcementService.getEnforcement(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchEnforcements() {
        EnforcementSearchDto criteria = EnforcementSearchDto.builder().build();
        List<EnforcementEntity> expectedEntities = Collections.singletonList(new EnforcementEntity());
        LegacyEnforcementSearchResults searchResults = LegacyEnforcementSearchResults.builder().build();
        searchResults.setEnforcementEntities(expectedEntities);
        doReturn(searchResults).when(legacyEnforcementService).postToGateway(anyString(), any(), any());

        List<EnforcementEntity> result = legacyEnforcementService.searchEnforcements(criteria);

        assertEquals(expectedEntities, result);
    }
}
