package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.HmrcRequestSearchDto;
import uk.gov.hmcts.opal.entity.HmrcRequestEntity;
import uk.gov.hmcts.opal.disco.opal.HmrcRequestService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HmrcRequestControllerTest {

    @Mock
    private HmrcRequestService hmrcRequestService;

    @InjectMocks
    private HmrcRequestController hmrcRequestController;

    @Test
    void testGetHmrcRequest_Success() {
        // Arrange
        HmrcRequestEntity entity = HmrcRequestEntity.builder().build();

        when(hmrcRequestService.getHmrcRequest(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<HmrcRequestEntity> response = hmrcRequestController.getHmrcRequestById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(hmrcRequestService, times(1)).getHmrcRequest(any(Long.class));
    }

    @Test
    void testSearchHmrcRequests_Success() {
        // Arrange
        HmrcRequestEntity entity = HmrcRequestEntity.builder().build();
        List<HmrcRequestEntity> hmrcRequestList = List.of(entity);

        when(hmrcRequestService.searchHmrcRequests(any())).thenReturn(hmrcRequestList);

        // Act
        HmrcRequestSearchDto searchDto = HmrcRequestSearchDto.builder().build();
        ResponseEntity<List<HmrcRequestEntity>> response = hmrcRequestController.postHmrcRequestsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(hmrcRequestList, response.getBody());
        verify(hmrcRequestService, times(1)).searchHmrcRequests(any());
    }

}
