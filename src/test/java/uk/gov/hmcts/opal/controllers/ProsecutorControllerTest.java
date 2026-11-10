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
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;
import uk.gov.hmcts.opal.generated.model.ProsecutorsReferenceDataResponse;
import uk.gov.hmcts.opal.service.proxy.ProsecutorServiceProxy;

@ExtendWith(MockitoExtension.class)
class ProsecutorControllerTest {

    @Mock
    private ProsecutorServiceProxy prosecutorServiceProxy;

    @InjectMocks
    private ProsecutorController prosecutorController;

    @Test
    void getProsecutorByKey_returnsProsecutor() {
        ProsecutorsObject prosecutor = ProsecutorsObject.builder().name("Prosecutor").build();
        when(prosecutorServiceProxy.getProsecutorByKey(1L)).thenReturn(prosecutor);

        ResponseEntity<ProsecutorsObject> response = prosecutorController.getProsecutorByKey(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(prosecutor, response.getBody());
        verify(prosecutorServiceProxy).getProsecutorByKey(1L);
    }

    @Test
    void getProsecutorsRefData_returnsResponse() {
        ProsecutorsReferenceDataResponse expected = ProsecutorsReferenceDataResponse.builder()
            .count(1)
            .refData(List.of(ProsecutorsObject.builder().name("Prosecutor").build()))
            .build();
        when(prosecutorServiceProxy.getProsecutorsRefData(null)).thenReturn(expected);

        ResponseEntity<ProsecutorsReferenceDataResponse> response = prosecutorController.getProsecutorsRefData(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(prosecutorServiceProxy).getProsecutorsRefData(null);
    }
}
