package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateRequest;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponse;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryResponse;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService;

@ExtendWith(MockitoExtension.class)
class InterfaceJobsApiControllerTest {

    @Mock
    private InterfaceJobService interfaceJobService;

    @InjectMocks
    private InterfaceJobsApiController controller;

    @Test
    void postInterfaceJobs_returnsCreatedServiceResponse() {
        InterfaceJobsCreateRequest request = InterfaceJobsCreateRequest.builder().build();
        InterfaceJobsCreateResponse serviceResponse = InterfaceJobsCreateResponse.builder()
            .interfaceJobs(List.of())
            .build();

        when(interfaceJobService.create(request)).thenReturn(serviceResponse);

        ResponseEntity<InterfaceJobsCreateResponse> response = controller.postInterfaceJobs(request);

        assertEquals(CREATED, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(interfaceJobService).create(request);
    }

    @Test
    void getInterfaceJobsSummary_returnsServiceResponse() {
        List<Short> businessUnitIds = List.of((short) 10, (short) 20);
        List<String> statuses = List.of("COMPLETED");
        LocalDateTime completedDateFrom = LocalDateTime.of(2026, 7, 1, 10, 0);
        LocalDateTime completedDateTo = LocalDateTime.of(2026, 7, 2, 10, 0);
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
