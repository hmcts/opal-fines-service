package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;
import uk.gov.hmcts.opal.disco.opal.MisDebtorService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MisDebtorControllerTest {

    @Mock
    private MisDebtorService misDebtorService;

    @InjectMocks
    private MisDebtorController misDebtorController;

    @Test
    void testGetMisDebtor_Success() {
        // Arrange
        MisDebtorEntity entity = MisDebtorEntity.builder().build();

        when(misDebtorService.getMisDebtor(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<MisDebtorEntity> response = misDebtorController.getMisDebtorById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(misDebtorService, times(1)).getMisDebtor(any(Long.class));
    }

    @Test
    void testSearchMisDebtors_Success() {
        // Arrange
        MisDebtorEntity entity = MisDebtorEntity.builder().build();
        List<MisDebtorEntity> misDebtorList = List.of(entity);

        when(misDebtorService.searchMisDebtors(any())).thenReturn(misDebtorList);

        // Act
        MisDebtorSearchDto searchDto = MisDebtorSearchDto.builder().build();
        ResponseEntity<List<MisDebtorEntity>> response = misDebtorController.postMisDebtorsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(misDebtorList, response.getBody());
        verify(misDebtorService, times(1)).searchMisDebtors(any());
    }

}
