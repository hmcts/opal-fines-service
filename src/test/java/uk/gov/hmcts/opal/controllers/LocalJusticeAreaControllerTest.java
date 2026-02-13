package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.reference.LjaReferenceData;
import uk.gov.hmcts.opal.dto.reference.LjaReferenceDataResults;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;

@ExtendWith(MockitoExtension.class)
class LocalJusticeAreaControllerTest {

    @Mock
    private LocalJusticeAreaService localJusticeAreaService;

    @InjectMocks
    private LocalJusticeAreaController localJusticeAreaController;

    @Test
    void testGetLocalJusticeArea_Success() {
        // Arrange
        LocalJusticeAreaEntity entity = LocalJusticeAreaEntity.builder().build();

        when(localJusticeAreaService.getLocalJusticeAreaById(anyShort())).thenReturn(entity);

        // Act
        ResponseEntity<LocalJusticeAreaEntity> response = localJusticeAreaController.getLocalJusticeAreaById((short) 1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(localJusticeAreaService, times(1)).getLocalJusticeAreaById(anyShort());
    }

    @Test
    void testSearchLocalJusticeAreas_Success() {
        // Arrange
        LocalJusticeAreaEntity entity = LocalJusticeAreaEntity.builder().build();
        List<LocalJusticeAreaEntity> localJusticeAreaList = List.of(entity);

        when(localJusticeAreaService.searchLocalJusticeAreas(any())).thenReturn(localJusticeAreaList);

        // Act
        LocalJusticeAreaSearchDto searchDto = LocalJusticeAreaSearchDto.builder().build();
        ResponseEntity<List<LocalJusticeAreaEntity>> response = localJusticeAreaController
            .postLocalJusticeAreasSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(localJusticeAreaList, response.getBody());
        verify(localJusticeAreaService, times(1)).searchLocalJusticeAreas(any());
    }


    @Test
    void testGetLocalJusticeAreasRefData_Success() {
        // Arrange
        LjaReferenceData entity = createLjaReferenceData();
        List<LjaReferenceData> localJusticeAreaList = List.of(entity);

        when(localJusticeAreaService.getReferenceData(any(), any())).thenReturn(localJusticeAreaList);

        // Act
        Optional<String> filter = Optional.empty();
        Optional<List<String>> ljaTypes = Optional.empty();
        ResponseEntity<LjaReferenceDataResults> response = localJusticeAreaController
            .getLocalJusticeAreaRefData(filter, ljaTypes);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        LjaReferenceDataResults refDataResults = response.getBody();
        assertEquals(1, refDataResults.getCount());
        assertEquals(localJusticeAreaList, refDataResults.getRefData());
        verify(localJusticeAreaService, times(1)).getReferenceData(any(), any());
    }

    private LjaReferenceData createLjaReferenceData() {
        return new LjaReferenceData() {

            @Override
            public Short getLocalJusticeAreaId() {
                return (short) 1;
            }

            @Override
            public String getLjaCode() {
                return "MAIN";
            }

            @Override
            public String getLjaType() {
                return "LJA_TYPE";
            }

            @Override
            public String getName() {
                return "Local Justice Area Main";
            }

            @Override
            public String getAddressLine1() {
                return "No.1 The Old Bailey";
            }

            @Override
            public String getPostcode() {
                return "BB1 1BB";
            }
        };
    }

}
