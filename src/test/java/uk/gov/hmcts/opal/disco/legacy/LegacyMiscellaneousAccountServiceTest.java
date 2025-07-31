package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMiscellaneousAccountSearchResults;
import uk.gov.hmcts.opal.dto.search.MiscellaneousAccountSearchDto;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyMiscellaneousAccountServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyMiscellaneousAccountService legacyMiscellaneousAccountService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyMiscellaneousAccountService = spy(new LegacyMiscellaneousAccountService(legacyGatewayProperties,
                                                                                      restClient));
    }

    @Test
    void testGetMiscellaneousAccount() {
        long id = 1L;
        MiscellaneousAccountEntity expectedEntity = new MiscellaneousAccountEntity();
        doReturn(expectedEntity).when(legacyMiscellaneousAccountService).postToGateway(anyString(), any(), anyLong());

        MiscellaneousAccountEntity result = legacyMiscellaneousAccountService.getMiscellaneousAccount(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchMiscellaneousAccounts() {
        MiscellaneousAccountSearchDto criteria = MiscellaneousAccountSearchDto.builder().build();
        List<MiscellaneousAccountEntity> expectedEntities = Collections.singletonList(new MiscellaneousAccountEntity());
        LegacyMiscellaneousAccountSearchResults searchResults = LegacyMiscellaneousAccountSearchResults.builder()
            .build();
        searchResults.setMiscellaneousAccountEntities(expectedEntities);
        doReturn(searchResults).when(legacyMiscellaneousAccountService).postToGateway(anyString(), any(), any());

        List<MiscellaneousAccountEntity> result = legacyMiscellaneousAccountService
            .searchMiscellaneousAccounts(criteria);

        assertEquals(expectedEntities, result);
    }

}
