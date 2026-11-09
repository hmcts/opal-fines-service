package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.service.TestingSupportReferenceDataService;

@ExtendWith(MockitoExtension.class)
class TestingSupportReferenceDataApiControllerTest {

    @Mock
    private TestingSupportReferenceDataService service;

    @InjectMocks
    private TestingSupportReferenceDataApiController controller;

    @Test
    void deleteReferenceData_deletesRequestedReferenceData() {
        ResponseEntity<Void> response = controller.deleteReferenceData("LJA", "0810");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(response.hasBody());
        verify(service).deleteReferenceData("LJA", "0810");
    }

    @Test
    void publishReferenceData_publishesRequestBody() {
        Map<String, Object> requestBody = Map.of("header", Map.of("data_product", "LJA"));

        ResponseEntity<Void> response = controller.publishReferenceData(requestBody);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertFalse(response.hasBody());
        verify(service).publishReferenceData(requestBody);
    }
}
