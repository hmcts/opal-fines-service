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
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;

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
    void testGetBusinessUnit() {
        short id = 1;
        BusinessUnitEntity expectedEntity = new BusinessUnitEntity();
        doReturn(expectedEntity).when(legacyBusinessUnitService).postToGateway(anyString(), any(), anyShort());

        BusinessUnitEntity result = legacyBusinessUnitService.getBusinessUnit(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchBusinessUnits() {
        BusinessUnitSearchDto criteria = BusinessUnitSearchDto.builder().build();
        List<BusinessUnitEntity> expectedEntities = Collections.singletonList(new BusinessUnitEntity());
        LegacyBusinessUnitSearchResults searchResults = LegacyBusinessUnitSearchResults.builder().build();
        searchResults.setBusinessUnitEntities(expectedEntities);
        doReturn(searchResults).when(legacyBusinessUnitService).postToGateway(anyString(), any(), any());

        List<BusinessUnitEntity> result = legacyBusinessUnitService.searchBusinessUnits(criteria);

        assertEquals(expectedEntities, result);
    }
}
