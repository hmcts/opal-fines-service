package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyPaymentInSearchResults;
import uk.gov.hmcts.opal.dto.search.PaymentInSearchDto;
import uk.gov.hmcts.opal.entity.PaymentInEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class LegacyPaymentInServiceTest extends LegacyTestsBase {

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private RestClient restClient;

    private LegacyPaymentInService legacyPaymentInService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        legacyPaymentInService = spy(new LegacyPaymentInService(legacyGatewayProperties, restClient));
    }

    @Test
    public void testGetPaymentIn() {
        long id = 1L;
        PaymentInEntity expectedEntity = new PaymentInEntity();
        doReturn(expectedEntity).when(legacyPaymentInService).postToGateway(anyString(), any(), anyLong());

        PaymentInEntity result = legacyPaymentInService.getPaymentIn(id);

        assertEquals(expectedEntity, result);
    }

    @Test
    public void testSearchPaymentIns() {
        PaymentInSearchDto criteria = PaymentInSearchDto.builder().build();
        List<PaymentInEntity> expectedEntities = Collections.singletonList(new PaymentInEntity());
        LegacyPaymentInSearchResults searchResults = LegacyPaymentInSearchResults.builder().build();
        searchResults.setPaymentInEntities(expectedEntities);
        doReturn(searchResults).when(legacyPaymentInService).postToGateway(anyString(), any(), any());

        List<PaymentInEntity> result = legacyPaymentInService.searchPaymentIns(criteria);

        assertEquals(expectedEntities, result);
    }
}
