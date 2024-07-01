package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyBusinessUnitUserSearchResults;
import uk.gov.hmcts.opal.dto.search.BusinessUnitUserSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyBusinessUnitUserServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyBusinessUnitUserService legacyBusinessUnitUserService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyBusinessUnitUserService = spy(new LegacyBusinessUnitUserService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetBusinessUnitUser() {
        String id = "1";
        BusinessUnitUserEntity expectedEntity = new BusinessUnitUserEntity();
        doReturn(expectedEntity).when(legacyBusinessUnitUserService).postToGateway(anyString(), any(), anyString());

        BusinessUnitUserEntity result = legacyBusinessUnitUserService.getBusinessUnitUser(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchBusinessUnitUsers() {
        BusinessUnitUserSearchDto criteria = BusinessUnitUserSearchDto.builder().build();
        List<BusinessUnitUserEntity> expectedEntities = Collections.singletonList(new BusinessUnitUserEntity());
        LegacyBusinessUnitUserSearchResults searchResults = LegacyBusinessUnitUserSearchResults.builder().build();
        searchResults.setBusinessUnitUserEntities(expectedEntities);
        doReturn(searchResults).when(legacyBusinessUnitUserService).postToGateway(anyString(), any(), any());

        List<BusinessUnitUserEntity> result = legacyBusinessUnitUserService.searchBusinessUnitUsers(criteria);

        assertEquals(expectedEntities, result);
    }

}
