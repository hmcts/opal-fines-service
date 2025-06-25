package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyPaymentTermsSearchResults;
import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyPaymentTermsServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyPaymentTermsService legacyPaymentTermsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyPaymentTermsService = spy(new LegacyPaymentTermsService(legacyGatewayProperties, restClient));
    }

    @Test
    void testGetPaymentTerms() {
        long id = 1L;
        PaymentTermsEntity expectedEntity = new PaymentTermsEntity();
        doReturn(expectedEntity).when(legacyPaymentTermsService).postToGateway(anyString(), any(), anyLong());

        PaymentTermsEntity result = legacyPaymentTermsService.getPaymentTerms(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    void testSearchPaymentTermss() {
        PaymentTermsSearchDto criteria = PaymentTermsSearchDto.builder().build();
        List<PaymentTermsEntity> expectedEntities = Collections.singletonList(new PaymentTermsEntity());
        LegacyPaymentTermsSearchResults searchResults = LegacyPaymentTermsSearchResults.builder().build();
        searchResults.setPaymentTermsEntities(expectedEntities);
        doReturn(searchResults).when(legacyPaymentTermsService).postToGateway(anyString(), any(), any());

        List<PaymentTermsEntity> result = legacyPaymentTermsService.searchPaymentTerms(criteria);

        assertEquals(expectedEntities, result);
    }
}
