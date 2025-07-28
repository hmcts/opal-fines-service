package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyFixedPenaltyOffenceSearchResults;
import uk.gov.hmcts.opal.dto.search.FixedPenaltyOffenceSearchDto;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyFixedPenaltyOffenceServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyFixedPenaltyOffenceService legacyFixedPenaltyOffenceService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyFixedPenaltyOffenceService = spy(new LegacyFixedPenaltyOffenceService(legacyGatewayProperties,
                                                                                    restClient));
    }

    @Test
    void testGetFixedPenaltyOffence() {
        long id = 1L;
        FixedPenaltyOffenceEntity expectedEntity = new FixedPenaltyOffenceEntity();
        doReturn(expectedEntity).when(legacyFixedPenaltyOffenceService).postToGateway(anyString(), any(), anyLong());

        FixedPenaltyOffenceEntity result = legacyFixedPenaltyOffenceService.getFixedPenaltyOffence(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchFixedPenaltyOffences() {
        FixedPenaltyOffenceSearchDto criteria = FixedPenaltyOffenceSearchDto.builder().build();
        List<FixedPenaltyOffenceEntity> expectedEntities = Collections.singletonList(new FixedPenaltyOffenceEntity());
        LegacyFixedPenaltyOffenceSearchResults searchResults = LegacyFixedPenaltyOffenceSearchResults.builder().build();
        searchResults.setFixedPenaltyOffenceEntities(expectedEntities);
        doReturn(searchResults).when(legacyFixedPenaltyOffenceService).postToGateway(anyString(), any(), any());

        List<FixedPenaltyOffenceEntity> result = legacyFixedPenaltyOffenceService.searchFixedPenaltyOffences(criteria);

        assertEquals(expectedEntities, result);
    }
}
