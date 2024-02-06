package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessUnitControllerTest {

    @Mock
    private BusinessUnitService businessUnitService;

    @InjectMocks
    private BusinessUnitController businessUnitController;

    @Test
    void testGetBusinessUnit_Success() {
        // Arrange
        BusinessUnitEntity entity = BusinessUnitEntity.builder().build(); //some id assigned by db sequence

        when(businessUnitService.getBusinessUnit(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<BusinessUnitEntity> response = businessUnitController.getBusinessUnitById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(businessUnitService, times(1)).getBusinessUnit(any(Long.class));
    }

    @Test
    void testSearchBusinessUnits_Success() {
        // Arrange
        BusinessUnitEntity entity = BusinessUnitEntity.builder().build();
        List<BusinessUnitEntity> businessUnitList = List.of(entity);

        when(businessUnitService.searchBusinessUnits(any())).thenReturn(businessUnitList);

        // Act
        BusinessUnitSearchDto searchDto = BusinessUnitSearchDto.builder().build();
        ResponseEntity<List<BusinessUnitEntity>> response = businessUnitController.postBusinessUnitsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(businessUnitList, response.getBody());
        verify(businessUnitService, times(1)).searchBusinessUnits(any());
    }

}
