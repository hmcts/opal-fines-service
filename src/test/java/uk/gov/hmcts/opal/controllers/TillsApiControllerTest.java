package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
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
import uk.gov.hmcts.opal.generated.model.TillsResponse;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.TillService;

@ExtendWith(MockitoExtension.class)
class TillsApiControllerTest {

    @Mock
    private DynamicConfigService dynamicConfigService;

    @Mock
    private TillService tillService;

    @InjectMocks
    private TillsApiController controller;

    @Test
    void getTills_returnsServiceResponse() {
        List<String> statuses = List.of("Allocated");
        Boolean autoPayments = true;
        List<Short> businessUnitIds = List.of((short) 78);
        TillsResponse serviceResponse = TillsResponse.builder().tills(List.of()).build();

        when(dynamicConfigService.isLegacyMode()).thenReturn(false);
        when(tillService.getTills(argThat(searchCriteria ->
            statuses.equals(searchCriteria.getStatuses())
                && autoPayments.equals(searchCriteria.getAutoPayments())))).thenReturn(serviceResponse);

        ResponseEntity<TillsResponse> response = controller.getTills(statuses, autoPayments, businessUnitIds);

        assertEquals(OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(tillService).getTills(argThat(searchCriteria ->
            statuses.equals(searchCriteria.getStatuses())
                && autoPayments.equals(searchCriteria.getAutoPayments())));
    }

    @Test
    void getTills_rejectsLegacyMode() {
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);

        assertThrows(FeatureDisabledException.class,
            () -> controller.getTills(List.of("Allocated"), true, List.of((short) 78)));

        verify(dynamicConfigService).isLegacyMode();
        verify(tillService, never()).getTills(org.mockito.ArgumentMatchers.any());
    }
}
