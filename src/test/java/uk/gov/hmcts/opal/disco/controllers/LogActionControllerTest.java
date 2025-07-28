package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.LogActionSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;
import uk.gov.hmcts.opal.disco.opal.LogActionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogActionControllerTest {

    @Mock
    private LogActionService logActionService;

    @InjectMocks
    private LogActionController logActionController;

    @Test
    void testGetLogAction_Success() {
        // Arrange
        LogActionEntity entity = LogActionEntity.builder().build();

        when(logActionService.getLogAction(any(Short.class))).thenReturn(entity);

        // Act
        ResponseEntity<LogActionEntity> response = logActionController.getLogActionById((short)1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(logActionService, times(1)).getLogAction(any(Short.class));
    }

    @Test
    void testSearchLogActions_Success() {
        // Arrange
        LogActionEntity entity = LogActionEntity.builder().build();
        List<LogActionEntity> logActionList = List.of(entity);

        when(logActionService.searchLogActions(any())).thenReturn(logActionList);

        // Act
        LogActionSearchDto searchDto = LogActionSearchDto.builder().build();
        ResponseEntity<List<LogActionEntity>> response = logActionController.postLogActionsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(logActionList, response.getBody());
        verify(logActionService, times(1)).searchLogActions(any());
    }

}
