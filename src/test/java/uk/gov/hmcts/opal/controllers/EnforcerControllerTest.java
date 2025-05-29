package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.reference.EnforcerReferenceDataResults;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.projection.EnforcerReferenceData;
import uk.gov.hmcts.opal.service.opal.EnforcerService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnforcerControllerTest {

    @Mock
    private EnforcerService enforcerService;

    @InjectMocks
    private EnforcerController enforcerController;

    @Test
    void testGetEnforcer_Success() {
        // Arrange
        EnforcerEntity entity = EnforcerEntity.builder().build();

        when(enforcerService.getEnforcerById(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<EnforcerEntity> response = enforcerController.getEnforcerById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(enforcerService, times(1)).getEnforcerById(any(Long.class));
    }

    @Test
    void testSearchEnforcers_Success() {
        // Arrange
        EnforcerEntity entity = EnforcerEntity.builder().build();
        List<EnforcerEntity> enforcerList = List.of(entity);

        when(enforcerService.searchEnforcers(any())).thenReturn(enforcerList);

        // Act
        EnforcerSearchDto searchDto = EnforcerSearchDto.builder().build();
        ResponseEntity<List<EnforcerEntity>> response = enforcerController.postEnforcersSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(enforcerList, response.getBody());
        verify(enforcerService, times(1)).searchEnforcers(any());
    }

    @Test
    void testGetEnforcersRefData_Success() {
        // Arrange
        EnforcerReferenceData refData = new EnforcerReferenceData(1L, (short)2,
                                                                  "Enforcers Ltd", "Enforcers Wales Ltd");
        List<EnforcerReferenceData> refDataList = List.of(refData);

        when(enforcerService.getReferenceData(any())).thenReturn(refDataList);

        // Act
        Optional<String> filter = Optional.empty();
        ResponseEntity<EnforcerReferenceDataResults> response = enforcerController
            .getEnforcersRefData(filter);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        EnforcerReferenceDataResults refDataResults = response.getBody();
        assertEquals(1, refDataResults.getCount());
        assertEquals(refDataList, refDataResults.getRefData());
        verify(enforcerService, times(1)).getReferenceData(any());
    }
}
