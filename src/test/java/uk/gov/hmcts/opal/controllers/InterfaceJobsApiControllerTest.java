package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateRequest;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponse;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryResponse;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessRequest;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService;

@ExtendWith(MockitoExtension.class)
class InterfaceJobsApiControllerTest {

    @Mock
    private InterfaceJobService interfaceJobService;

    @Mock
    private DynamicConfigService dynamicConfigService;
    @InjectMocks
    private InterfaceJobsApiController controller;

    @Test
    void processInterfaceJobs_returnsOkWithEmptyBody() {
        InterfaceJobsProcessRequest request = InterfaceJobsProcessRequest.builder().build();
        when(dynamicConfigService.isLegacyMode()).thenReturn(false);

        ResponseEntity<Void> response = controller.processInterfaceJobs(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(interfaceJobService).process(request);
        verify(dynamicConfigService).isLegacyMode();
    }

    @Test
    void processInterfaceJobs_rejectsLegacyMode() {
        InterfaceJobsProcessRequest request = InterfaceJobsProcessRequest.builder().build();
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);

        assertThrows(FeatureDisabledException.class, () -> controller.processInterfaceJobs(request));

        verify(dynamicConfigService).isLegacyMode();
        verify(interfaceJobService, never()).process(request);
    }

    @Test
    void postInterfaceJobs_returnsCreatedServiceResponse() {
        InterfaceJobsCreateRequest request = InterfaceJobsCreateRequest.builder().build();
        InterfaceJobsCreateResponse serviceResponse = InterfaceJobsCreateResponse.builder()
            .interfaceJobs(List.of())
            .build();

        when(interfaceJobService.create(request)).thenReturn(serviceResponse);

        ResponseEntity<InterfaceJobsCreateResponse> response = controller.postInterfaceJobs(request);

        assertEquals(OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(interfaceJobService).create(request);
    }

    @Test
    void getInterfaceJobsSummary_returnsServiceResponse() {
        List<Short> businessUnitIds = List.of((short) 10, (short) 20);
        List<String> statuses = List.of("COMPLETED");
        LocalDateTime completedDateFrom = LocalDateTime.of(2026, Month.JULY, 1, 10, 0);
        LocalDateTime completedDateTo = LocalDateTime.of(2026, Month.JULY, 2, 10, 0);
        InterfaceJobsSummaryResponse serviceResponse = InterfaceJobsSummaryResponse.builder()
            .interfaceJobs(List.of(new InterfaceJobsSummaryItem()))
            .build();

        when(interfaceJobService.getSummary(any())).thenReturn(serviceResponse);

        ResponseEntity<InterfaceJobsSummaryResponse> response = controller.getInterfaceJobsSummary(
            businessUnitIds, statuses, completedDateFrom, completedDateTo, "Auto Payments In");

        assertEquals(OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(interfaceJobService).getSummary(argThat(searchCriteria ->
            statuses.equals(searchCriteria.getStatuses())
                && completedDateFrom.equals(searchCriteria.getCompletedDateFrom())
                && completedDateTo.equals(searchCriteria.getCompletedDateTo())
                && "Auto Payments In".equals(searchCriteria.getInterfaceName())));
    }
}
