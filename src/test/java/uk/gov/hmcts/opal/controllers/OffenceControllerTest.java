package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.reference.OffenceReferenceDataResults;
import uk.gov.hmcts.opal.dto.reference.OffenceSearchDataResults;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity;
import uk.gov.hmcts.opal.entity.projection.OffenceReferenceData;
import uk.gov.hmcts.opal.entity.projection.OffenceSearchData;
import uk.gov.hmcts.opal.service.opal.OffenceService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenceControllerTest {

    @Mock
    private OffenceService offenceService;

    @InjectMocks
    private OffenceController offenceController;

    @Test
    void testGetOffence_Success() {
        // Arrange
        OffenceEntity entity = OffenceEntity.Lite.builder().build();

        when(offenceService.getOffenceById(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<OffenceEntity> response = offenceController.getOffenceById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(offenceService, times(1)).getOffenceById(any(Long.class));
    }

    @Test
    void testSearchOffences_Success() {
        // Arrange
        OffenceSearchData entity = createOffenceSearchData();
        List<OffenceSearchData> offenceList = List.of(entity);

        when(offenceService.searchOffences(any())).thenReturn(offenceList);

        // Act
        OffenceSearchDto searchDto = OffenceSearchDto.builder().build();
        ResponseEntity<OffenceSearchDataResults> response = offenceController.postOffencesSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OffenceSearchDataResults searchDataResults = response.getBody();
        assertEquals(1, searchDataResults.getCount());
        assertEquals(offenceList, searchDataResults.getSearchData());
        verify(offenceService, times(1)).searchOffences(any());
    }

    @Test
    void testGetOffencesRefData_Success() {
        // Arrange
        OffenceReferenceData entity = createOffenceReferenceData();
        List<OffenceReferenceData> offenceList = List.of(entity);

        when(offenceService.getReferenceData(any(), any())).thenReturn(offenceList);

        // Act
        Optional<String> filter = Optional.empty();
        Optional<Short> businessUnit = Optional.empty();
        ResponseEntity<OffenceReferenceDataResults> response = offenceController.getOffenceRefData(filter,
                                                                                                   businessUnit);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        OffenceReferenceDataResults refDataResults = response.getBody();
        assertEquals(1, refDataResults.getCount());
        assertEquals(offenceList, refDataResults.getRefData());
        verify(offenceService, times(1)).getReferenceData(any(), any());
    }

    private OffenceReferenceData createOffenceReferenceData() {
        return new OffenceReferenceData(1L, "TH123456", (short)007,
                                        "Thief of Time", null,
                                        LocalDateTime.of(1909, 3, 3, 3, 30),
                                        null, "", "");
    }

    private OffenceSearchData createOffenceSearchData() {
        return new OffenceSearchData(1L, "TH123456",
                                        "Thief of Time", null,
                                        LocalDateTime.of(1909, 3, 3, 3, 30),
                                        null, "", "");
    }

}
