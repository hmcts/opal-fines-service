package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.reference.CourtReferenceDataResults;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.dto.reference.CourtReferenceData;
import uk.gov.hmcts.opal.service.opal.CourtService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtControllerTest {

    static final String BEARER_TOKEN = "Bearer a_token_here";

    @Mock
    private CourtService courtService;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private CourtController courtController;

    @Test
    void testGetCourt_Success() {
        // Arrange
        CourtEntity entity = CourtEntity.builder().build();

        when(courtService.getCourtById(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<CourtEntity> response = courtController.getCourtById(1L, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(courtService, times(1)).getCourtById(any(Long.class));
    }

    @Test
    void testSearchCourts_Success() {
        // Arrange
        CourtEntity entity = CourtEntity.builder().build();
        List<CourtEntity> courtList = List.of(entity);

        when(courtService.searchCourts(any())).thenReturn(courtList);

        // Act
        CourtSearchDto searchDto = CourtSearchDto.builder().build();
        ResponseEntity<List<CourtEntity>> response = courtController.postCourtsSearch(searchDto, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(courtList, response.getBody());
        verify(courtService, times(1)).searchCourts(any());
    }

    @Test
    void testGetCourtsRefData_Success() {
        // Arrange
        CourtReferenceData entity = createCourtReferenceData();
        List<CourtReferenceData> courtList = List.of(entity);

        when(courtService.getReferenceData(any(), any())).thenReturn(courtList);

        // Act
        Optional<String> filter = Optional.empty();
        Optional<Short> businessUnit = Optional.empty();
        ResponseEntity<CourtReferenceDataResults> response = courtController.getCourtRefData(filter, businessUnit);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CourtReferenceDataResults refDataResults = response.getBody();
        assertEquals(1, refDataResults.getCount());
        assertEquals(courtList, refDataResults.getRefData());
        verify(courtService, times(1)).getReferenceData(any(), any());
    }

    private CourtReferenceData createCourtReferenceData() {
        return new CourtReferenceData(1L, (short)007, (short)2,"Main Court", null,"MM1234");
    }
}
