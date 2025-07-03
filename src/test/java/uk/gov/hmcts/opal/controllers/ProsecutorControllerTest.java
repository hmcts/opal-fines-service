package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.reference.ProsecutorReferenceData;
import uk.gov.hmcts.opal.dto.response.RefDataResponse;
import uk.gov.hmcts.opal.entity.ProsecutorEntity;
import uk.gov.hmcts.opal.service.opal.ProsecutorService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProsecutorControllerTest {

    @Mock
    private ProsecutorService prosecutorService;

    @InjectMocks
    private ProscutorController prosecutorController;

    @Test
    void testGetProsecutor_Success() {
        // Arrange
        ProsecutorEntity entity = ProsecutorEntity.builder().build();

        when(prosecutorService.getProsecutorById(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<ProsecutorEntity> response = prosecutorController.getProsecutorById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(prosecutorService, times(1)).getProsecutorById(any(Long.class));
    }

    @Test
    void testGetProsecutorsRefData_Success() {
        // Arrange
        ProsecutorReferenceData refData = ProsecutorReferenceData.builder().build();
        List<ProsecutorReferenceData> refDataList = List.of(refData);

        when(prosecutorService.getReferenceData(any())).thenReturn(refDataList);

        // Act
        Optional<String> filter = Optional.empty();
        ResponseEntity<RefDataResponse<ProsecutorReferenceData>> response = prosecutorController
            .getProsecutorsRefData(filter);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        RefDataResponse<ProsecutorReferenceData> refDataResults = response.getBody();
        assertEquals(1, refDataResults.getCount());
        assertEquals(refDataList, refDataResults.getRefData());
        verify(prosecutorService, times(1)).getReferenceData(any());
    }
}
