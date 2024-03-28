package uk.gov.hmcts.opal.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.service.opal.CourtService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtControllerTest {

    @Mock
    private CourtService courtService;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private CourtController courtController;

    @Test
    void testGetCourt_Success() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        CourtEntity entity = CourtEntity.builder().build();

        when(courtService.getCourt(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<CourtEntity> response = courtController.getCourtById(1L, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(courtService, times(1)).getCourt(any(Long.class));
    }

    @Test
    void testSearchCourts_Success() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        CourtEntity entity = CourtEntity.builder().build();
        List<CourtEntity> courtList = List.of(entity);

        when(courtService.searchCourts(any())).thenReturn(courtList);

        // Act
        CourtSearchDto searchDto = CourtSearchDto.builder().build();
        ResponseEntity<List<CourtEntity>> response = courtController.postCourtsSearch(searchDto, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(courtList, response.getBody());
        verify(courtService, times(1)).searchCourts(any());
    }

}
