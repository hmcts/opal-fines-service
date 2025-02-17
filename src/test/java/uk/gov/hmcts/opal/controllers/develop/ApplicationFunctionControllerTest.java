package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.ApplicationFunctionSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;
import uk.gov.hmcts.opal.service.opal.ApplicationFunctionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationFunctionControllerTest {

    @Mock
    private ApplicationFunctionService applicationFunctionService;

    @InjectMocks
    private ApplicationFunctionController applicationFunctionController;

    @Test
    void testGetApplicationFunction_Success() {
        // Arrange
        ApplicationFunctionEntity entity = ApplicationFunctionEntity.builder().build();

        when(applicationFunctionService.getApplicationFunction(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<ApplicationFunctionEntity> response = applicationFunctionController
            .getApplicationFunctionById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(applicationFunctionService, times(1)).getApplicationFunction(any(Long.class));
    }

    @Test
    void testSearchApplicationFunctions_Success() {
        // Arrange
        ApplicationFunctionEntity entity = ApplicationFunctionEntity.builder().build();
        List<ApplicationFunctionEntity> applicationFunctionList = List.of(entity);

        when(applicationFunctionService.searchApplicationFunctions(any())).thenReturn(applicationFunctionList);

        // Act
        ApplicationFunctionSearchDto searchDto = ApplicationFunctionSearchDto.builder().build();
        ResponseEntity<List<ApplicationFunctionEntity>> response = applicationFunctionController
            .postApplicationFunctionsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(applicationFunctionList, response.getBody());
        verify(applicationFunctionService, times(1)).searchApplicationFunctions(any());
    }

}
