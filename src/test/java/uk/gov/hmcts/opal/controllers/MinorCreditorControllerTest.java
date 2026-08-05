package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.Creditor;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.service.MinorCreditorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinorCreditorControllerTest {

    @Mock
    MinorCreditorService minorCreditorService;

    @InjectMocks
    private MinorCreditorController minorCreditorController;

    @Test
    void testPostMinorCreditorSearch_Success() {
        // Arrange
        PostMinorCreditorAccountsSearchResponse mockResponse = new PostMinorCreditorAccountsSearchResponse();

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of((short) 101, (short) 202, (short) 303))
            .activeAccountsOnly(true)
            .accountNumber("ACC123456")
            .creditor(new Creditor(/* set Creditor fields as needed */))
            .build();

        when(minorCreditorService.searchMinorCreditors(any())).thenReturn(mockResponse);

        // Act
        ResponseEntity<PostMinorCreditorAccountsSearchResponse> responseEntity =
            minorCreditorController.postMinorCreditorsSearch(search);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(minorCreditorService, times(1)).searchMinorCreditors(any());
    }
}
