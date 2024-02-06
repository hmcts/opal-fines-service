package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.dto.search.PartySearchDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.service.opal.PartyService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartyControllerTest {

    @Mock
    private PartyService partyService;

    @InjectMocks
    private PartyController partyController;

    @Test
    void testCreateParty_Success() {
        // Arrange
        PartyDto partyDtoRequest = PartyDto.builder().build();
        PartyDto partyDtoResponse = PartyDto.builder().partyId(1L).build(); //some id assigned by db sequence

        when(partyService.saveParty(any(PartyDto.class))).thenReturn(partyDtoResponse);

        // Act
        ResponseEntity<PartyDto> response = partyController.createParty(partyDtoRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(partyDtoResponse, response.getBody());
        verify(partyService, times(1)).saveParty(any(PartyDto.class));
    }

    @Test
    void testGetParty_Success() {
        // Arrange
        PartyDto partyDtoResponse = PartyDto.builder().partyId(1L).build(); //some id assigned by db sequence

        when(partyService.getParty(any(Long.class))).thenReturn(partyDtoResponse);

        // Act
        ResponseEntity<PartyDto> response = partyController.getParty(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(partyDtoResponse, response.getBody());
        verify(partyService, times(1)).getParty(any(Long.class));
    }

    @Test
    void testSearchParties_Success() {
        // Arrange
        PartyEntity entity = PartyEntity.builder().build();
        List<PartyEntity> partyList = List.of(entity);

        when(partyService.searchParties(any())).thenReturn(partyList);

        // Act
        PartySearchDto searchDto = PartySearchDto.builder().build();
        ResponseEntity<List<PartyEntity>> response = partyController.postPartiesSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(partyList, response.getBody());
        verify(partyService, times(1)).searchParties(any());
    }

}
