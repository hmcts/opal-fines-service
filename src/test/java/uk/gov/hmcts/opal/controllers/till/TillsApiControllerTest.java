package uk.gov.hmcts.opal.controllers.till;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.generated.model.TillsGetResponse;
import uk.gov.hmcts.opal.generated.model.TillsResponse;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.till.TillSearchService;
import uk.gov.hmcts.opal.service.opal.till.TillsService;

@ExtendWith(MockitoExtension.class)
class TillsApiControllerTest {

    private static final Long TILL_ID = 123L;

    @Mock
    private TillsService tillsService;

    @Mock
    private DynamicConfigService dynamicConfigService;

    @Mock
    private TillSearchService tillSearchService;

    @InjectMocks
    private TillsApiController controller;

    @Test
    void whenCalled_returnsTill_happyPath() {
        TillsGetResponse expected = new TillsGetResponse();
        when(tillsService.getTill(TILL_ID)).thenReturn(expected);

        ResponseEntity<TillsGetResponse> response = controller.getTill(TILL_ID);

        assertAll(
            () -> assertEquals(200, response.getStatusCode().value()),
            () -> assertSame(expected, response.getBody()),
            () -> verify(tillsService).getTill(TILL_ID)
        );
    }

    @Test
    void getTills_returnsServiceResponse() {
        List<String> statuses = List.of("Allocated");
        Boolean autoPayments = true;
        List<Short> businessUnitIds = List.of((short) 78);
        TillsResponse serviceResponse = TillsResponse.builder().tills(List.of()).build();

        when(dynamicConfigService.isLegacyMode()).thenReturn(false);
        when(tillSearchService.getTills(argThat(searchCriteria ->
            statuses.equals(searchCriteria.getStatuses())
                && autoPayments.equals(searchCriteria.getAutoPayments())))).thenReturn(serviceResponse);

        ResponseEntity<TillsResponse> response = controller.getTills(statuses, autoPayments, businessUnitIds);

        assertEquals(OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(tillSearchService).getTills(argThat(searchCriteria ->
            statuses.equals(searchCriteria.getStatuses())
                && autoPayments.equals(searchCriteria.getAutoPayments())));
    }

    @Test
    void getTills_rejectsLegacyMode() {
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);

        assertThrows(FeatureDisabledException.class,
            () -> controller.getTills(List.of("Allocated"), true, List.of((short) 78)));

        verify(dynamicConfigService).isLegacyMode();
        verify(tillSearchService, never()).getTills(any());
    }
}
