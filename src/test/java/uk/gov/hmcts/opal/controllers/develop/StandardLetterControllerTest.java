package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.StandardLetterSearchDto;
import uk.gov.hmcts.opal.entity.StandardLetterEntity;
import uk.gov.hmcts.opal.service.opal.StandardLetterService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandardLetterControllerTest {

    @Mock
    private StandardLetterService standardLetterService;

    @InjectMocks
    private StandardLetterController standardLetterController;

    @Test
    void testGetStandardLetter_Success() {
        // Arrange
        StandardLetterEntity entity = StandardLetterEntity.builder().build();

        when(standardLetterService.getStandardLetter(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<StandardLetterEntity> response = standardLetterController.getStandardLetterById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(standardLetterService, times(1)).getStandardLetter(any(Long.class));
    }

    @Test
    void testSearchStandardLetters_Success() {
        // Arrange
        StandardLetterEntity entity = StandardLetterEntity.builder().build();
        List<StandardLetterEntity> standardLetterList = List.of(entity);

        when(standardLetterService.searchStandardLetters(any())).thenReturn(standardLetterList);

        // Act
        StandardLetterSearchDto searchDto = StandardLetterSearchDto.builder().build();
        ResponseEntity<List<StandardLetterEntity>> response = standardLetterController
            .postStandardLettersSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(standardLetterList, response.getBody());
        verify(standardLetterService, times(1)).searchStandardLetters(any());
    }

}
