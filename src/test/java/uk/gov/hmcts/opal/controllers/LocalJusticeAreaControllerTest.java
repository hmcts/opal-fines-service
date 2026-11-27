package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.LocalJusticeArea;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaType;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreasResponse;
import uk.gov.hmcts.opal.service.proxy.LocalJusticeAreaServiceProxy;

@ExtendWith(MockitoExtension.class)
class LocalJusticeAreaControllerTest {

    @Mock
    private LocalJusticeAreaServiceProxy localJusticeAreaServiceProxy;

    @InjectMocks
    private LocalJusticeAreaController localJusticeAreaController;

    @Test
    void getLocalJusticeAreaById_returnsServiceResponse() {
        LocalJusticeArea localJusticeArea = LocalJusticeArea.builder()
            .localJusticeAreaId((short) 1)
            .build();
        when(localJusticeAreaServiceProxy.getLocalJusticeAreaById((short) 1)).thenReturn(localJusticeArea);

        ResponseEntity<LocalJusticeArea> response = localJusticeAreaController.getLocalJusticeAreaById((short) 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(localJusticeArea, response.getBody());
        verify(localJusticeAreaServiceProxy).getLocalJusticeAreaById((short) 1);
    }

    @Test
    void getLocalJusticeAreaRefData_returnsServiceResponse() {
        List<LocalJusticeAreaType> ljaTypes = List.of(LocalJusticeAreaType.LJA);
        LocalJusticeAreasResponse serviceResponse = LocalJusticeAreasResponse.builder()
            .count(1)
            .refData(List.of())
            .build();
        when(localJusticeAreaServiceProxy.getLocalJusticeAreaRefData("court", ljaTypes))
            .thenReturn(serviceResponse);

        ResponseEntity<LocalJusticeAreasResponse> response = localJusticeAreaController
            .getLocalJusticeAreaRefData("court", ljaTypes);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(localJusticeAreaServiceProxy).getLocalJusticeAreaRefData("court", ljaTypes);
    }
}
