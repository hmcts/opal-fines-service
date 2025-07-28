package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.AmendmentEntity;
import uk.gov.hmcts.opal.disco.opal.AmendmentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmendmentControllerTest {

    @Mock
    private AmendmentService amendmentService;

    @InjectMocks
    private AmendmentController amendmentController;

    @Test
    void testGetAmendment_Success() {
        // Arrange
        AmendmentEntity entity = AmendmentEntity.builder().build();

        when(amendmentService.getAmendment(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<AmendmentEntity> response = amendmentController.getAmendmentById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(amendmentService, times(1)).getAmendment(any(Long.class));
    }

    @Test
    void testSearchAmendments_Success() {
        // Arrange
        AmendmentEntity entity = AmendmentEntity.builder().build();
        List<AmendmentEntity> amendmentList = List.of(entity);

        when(amendmentService.searchAmendments(any())).thenReturn(amendmentList);

        // Act
        AmendmentSearchDto searchDto = AmendmentSearchDto.builder().build();
        ResponseEntity<List<AmendmentEntity>> response = amendmentController.postAmendmentsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(amendmentList, response.getBody());
        verify(amendmentService, times(1)).searchAmendments(any());
    }

}
