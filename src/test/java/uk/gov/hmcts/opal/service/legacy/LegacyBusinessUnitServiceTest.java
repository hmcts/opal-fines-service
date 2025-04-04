package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyBusinessUnitSearchResults;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitCore;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyBusinessUnitServiceTest extends LegacyTestsBase {
    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyBusinessUnitService legacyBusinessUnitService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyBusinessUnitService = spy(new LegacyBusinessUnitService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetBusinessUnit() {
        short id = 1;
        BusinessUnitCore expectedEntity = new BusinessUnitCore();
        doReturn(expectedEntity).when(legacyBusinessUnitService).postToGateway(anyString(), any(), anyShort());

        BusinessUnitCore result = legacyBusinessUnitService.getBusinessUnit(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchBusinessUnits() {
        BusinessUnitSearchDto criteria = BusinessUnitSearchDto.builder().build();
        List<BusinessUnit.Lite> expectedEntities = Collections.singletonList(new BusinessUnit.Lite());
        LegacyBusinessUnitSearchResults searchResults = LegacyBusinessUnitSearchResults.builder().build();
        searchResults.setBusinessUnitEntities(expectedEntities);
        doReturn(searchResults).when(legacyBusinessUnitService).postToGateway(anyString(), any(), any());

        List<BusinessUnit.Lite> result = legacyBusinessUnitService.searchBusinessUnits(criteria);

        assertEquals(expectedEntities, result);
    }
}
