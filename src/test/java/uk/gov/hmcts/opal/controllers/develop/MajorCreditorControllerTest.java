package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;
import uk.gov.hmcts.opal.service.opal.MajorCreditorService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MajorCreditorControllerTest {

    @Mock
    private MajorCreditorService majorCreditorService;

    @InjectMocks
    private MajorCreditorController majorCreditorController;

    @Test
    void testGetMajorCreditor_Success() {
        // Arrange
        MajorCreditorEntity entity = MajorCreditorEntity.builder().build();

        when(majorCreditorService.getMajorCreditor(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<MajorCreditorEntity> response = majorCreditorController.getMajorCreditorById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(majorCreditorService, times(1)).getMajorCreditor(any(Long.class));
    }

    @Test
    void testSearchMajorCreditors_Success() {
        // Arrange
        MajorCreditorEntity entity = MajorCreditorEntity.builder().build();
        List<MajorCreditorEntity> majorCreditorList = List.of(entity);

        when(majorCreditorService.searchMajorCreditors(any())).thenReturn(majorCreditorList);

        // Act
        MajorCreditorSearchDto searchDto = MajorCreditorSearchDto.builder().build();
        ResponseEntity<List<MajorCreditorEntity>> response = majorCreditorController
            .postMajorCreditorsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(majorCreditorList, response.getBody());
        verify(majorCreditorService, times(1)).searchMajorCreditors(any());
    }

}