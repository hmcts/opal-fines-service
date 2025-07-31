package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyCourtFeeSearchResults;
import uk.gov.hmcts.opal.dto.search.CourtFeeSearchDto;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyCourtFeeServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyCourtFeeService legacyCourtFeeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyCourtFeeService = spy(new LegacyCourtFeeService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetCourtFee() {
        long id = 1L;
        CourtFeeEntity expectedEntity = new CourtFeeEntity();
        doReturn(expectedEntity).when(legacyCourtFeeService).postToGateway(anyString(), any(), anyLong());

        CourtFeeEntity result = legacyCourtFeeService.getCourtFee(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchCourtFees() {
        CourtFeeSearchDto criteria = CourtFeeSearchDto.builder().build();
        List<CourtFeeEntity> expectedEntities = Collections.singletonList(new CourtFeeEntity());
        LegacyCourtFeeSearchResults searchResults = LegacyCourtFeeSearchResults.builder().build();
        searchResults.setCourtFeeEntities(expectedEntities);
        doReturn(searchResults).when(legacyCourtFeeService).postToGateway(anyString(), any(), any());

        List<CourtFeeEntity> result = legacyCourtFeeService.searchCourtFees(criteria);

        assertEquals(expectedEntities, result);
    }
}
