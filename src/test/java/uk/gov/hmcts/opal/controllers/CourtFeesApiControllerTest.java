package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.CourtFeesResponse;
import uk.gov.hmcts.opal.service.CourtFeesService;

@ExtendWith(MockitoExtension.class)
class CourtFeesApiControllerTest {

    @Mock
    private CourtFeesService courtFeesService;

    @InjectMocks
    private CourtFeesApiController courtFeesApiController;

    @Test
    void getCourtFees_returnsCourtFeesResponse() {
        Short businessUnitId = 77;
        CourtFeesResponse expectedResponse = CourtFeesResponse.builder().build();
        when(courtFeesService.getCourtFeesForBusinessUnit(businessUnitId)).thenReturn(expectedResponse);

        ResponseEntity<CourtFeesResponse> response = courtFeesApiController.getCourtFees(businessUnitId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
        verify(courtFeesService).getCourtFeesForBusinessUnit(businessUnitId);
    }
}
