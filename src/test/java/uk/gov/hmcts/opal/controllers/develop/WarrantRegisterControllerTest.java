package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.WarrantRegisterSearchDto;
import uk.gov.hmcts.opal.entity.WarrantRegisterEntity;
import uk.gov.hmcts.opal.service.opal.WarrantRegisterService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarrantRegisterControllerTest {

    @Mock
    private WarrantRegisterService warrantRegisterService;

    @InjectMocks
    private WarrantRegisterController warrantRegisterController;

    @Test
    void testGetWarrantRegister_Success() {
        // Arrange
        WarrantRegisterEntity entity = WarrantRegisterEntity.builder().build();

        when(warrantRegisterService.getWarrantRegister(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<WarrantRegisterEntity> response = warrantRegisterController.getWarrantRegisterById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(warrantRegisterService, times(1)).getWarrantRegister(any(Long.class));
    }

    @Test
    void testSearchWarrantRegisters_Success() {
        // Arrange
        WarrantRegisterEntity entity = WarrantRegisterEntity.builder().build();
        List<WarrantRegisterEntity> warrantRegisterList = List.of(entity);

        when(warrantRegisterService.searchWarrantRegisters(any())).thenReturn(warrantRegisterList);

        // Act
        WarrantRegisterSearchDto searchDto = WarrantRegisterSearchDto.builder().build();
        ResponseEntity<List<WarrantRegisterEntity>> response = warrantRegisterController
            .postWarrantRegistersSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(warrantRegisterList, response.getBody());
        verify(warrantRegisterService, times(1)).searchWarrantRegisters(any());
    }

}
