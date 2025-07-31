package uk.gov.hmcts.opal.disco.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyCreditorAccountSearchResults;
import uk.gov.hmcts.opal.dto.search.CreditorAccountSearchDto;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyCreditorAccountServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyCreditorAccountService legacyCreditorAccountService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyCreditorAccountService = spy(new LegacyCreditorAccountService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetCreditorAccount() {
        long id = 1L;
        CreditorAccountEntity expectedEntity = new CreditorAccountEntity();
        doReturn(expectedEntity).when(legacyCreditorAccountService).postToGateway(anyString(), any(), anyLong());

        CreditorAccountEntity result = legacyCreditorAccountService.getCreditorAccount(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchCreditorAccounts() {
        CreditorAccountSearchDto criteria = CreditorAccountSearchDto.builder().build();
        List<CreditorAccountEntity> expectedEntities = Collections.singletonList(new CreditorAccountEntity());
        LegacyCreditorAccountSearchResults searchResults = LegacyCreditorAccountSearchResults.builder().build();
        searchResults.setCreditorAccountEntities(expectedEntities);
        doReturn(searchResults).when(legacyCreditorAccountService).postToGateway(anyString(), any(), any());

        List<CreditorAccountEntity> result = legacyCreditorAccountService.searchCreditorAccounts(criteria);

        assertEquals(expectedEntities, result);
    }
}
