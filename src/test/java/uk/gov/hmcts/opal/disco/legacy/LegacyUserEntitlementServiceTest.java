package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyUserEntitlementSearchResults;
import uk.gov.hmcts.opal.dto.search.UserEntitlementSearchDto;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyUserEntitlementServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyUserEntitlementService legacyUserEntitlementService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyUserEntitlementService = spy(new LegacyUserEntitlementService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetUserEntitlement() {
        long id = 1L;
        UserEntitlementEntity expectedEntity = new UserEntitlementEntity();
        doReturn(expectedEntity).when(legacyUserEntitlementService).postToGateway(anyString(), any(), anyLong());

        UserEntitlementEntity result = legacyUserEntitlementService.getUserEntitlement(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchUserEntitlements() {
        UserEntitlementSearchDto criteria = UserEntitlementSearchDto.builder().build();
        List<UserEntitlementEntity> expectedEntities = Collections.singletonList(new UserEntitlementEntity());
        LegacyUserEntitlementSearchResults searchResults = LegacyUserEntitlementSearchResults.builder().build();
        searchResults.setUserEntitlementEntities(expectedEntities);
        doReturn(searchResults).when(legacyUserEntitlementService).postToGateway(anyString(), any(), any());

        List<UserEntitlementEntity> result = legacyUserEntitlementService.searchUserEntitlements(criteria);

        assertEquals(expectedEntities, result);
    }
}
