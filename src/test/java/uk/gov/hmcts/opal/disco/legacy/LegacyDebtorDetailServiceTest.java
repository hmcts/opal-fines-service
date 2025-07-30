package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyDebtorDetailSearchResults;
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyDebtorDetailServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyDebtorDetailService legacyDebtorDetailService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyDebtorDetailService = spy(new LegacyDebtorDetailService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetDebtorDetail() {
        long id = 1L;
        DebtorDetailEntity expectedEntity = new DebtorDetailEntity();
        doReturn(expectedEntity).when(legacyDebtorDetailService).postToGateway(anyString(), any(), anyLong());

        DebtorDetailEntity result = legacyDebtorDetailService.getDebtorDetail(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchDebtorDetails() {
        DebtorDetailSearchDto criteria = DebtorDetailSearchDto.builder().build();
        List<DebtorDetailEntity> expectedEntities = Collections.singletonList(new DebtorDetailEntity());
        LegacyDebtorDetailSearchResults searchResults = LegacyDebtorDetailSearchResults.builder().build();
        searchResults.setDebtorDetailEntities(expectedEntities);
        doReturn(searchResults).when(legacyDebtorDetailService).postToGateway(anyString(), any(), any());

        List<DebtorDetailEntity> result = legacyDebtorDetailService.searchDebtorDetails(criteria);

        assertEquals(expectedEntities, result);
    }
}
