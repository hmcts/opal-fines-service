package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.LogAuditDetailSearchDto;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;
import uk.gov.hmcts.opal.service.opal.LogAuditDetailService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogAuditDetailControllerTest {

    @Mock
    private LogAuditDetailService logAuditDetailService;

    @InjectMocks
    private LogAuditDetailController logAuditDetailController;

    @Test
    void testGetLogAuditDetail_Success() {
        // Arrange
        LogAuditDetailEntity entity = LogAuditDetailEntity.builder().build();

        when(logAuditDetailService.getLogAuditDetail(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<LogAuditDetailEntity> response = logAuditDetailController.getLogAuditDetailById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(logAuditDetailService, times(1)).getLogAuditDetail(any(Long.class));
    }

    @Test
    void testSearchLogAuditDetails_Success() {
        // Arrange
        LogAuditDetailEntity entity = LogAuditDetailEntity.builder().build();
        List<LogAuditDetailEntity> logAuditDetailList = List.of(entity);

        when(logAuditDetailService.searchLogAuditDetails(any())).thenReturn(logAuditDetailList);

        // Act
        LogAuditDetailSearchDto searchDto = LogAuditDetailSearchDto.builder().build();
        ResponseEntity<List<LogAuditDetailEntity>> response = logAuditDetailController
            .postLogAuditDetailsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(logAuditDetailList, response.getBody());
        verify(logAuditDetailService, times(1)).searchLogAuditDetails(any());
    }

}
