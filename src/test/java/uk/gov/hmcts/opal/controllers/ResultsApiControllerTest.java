package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.GetResultByIdResponseResults;
import uk.gov.hmcts.opal.service.opal.ResultService;

@ExtendWith(MockitoExtension.class)
class ResultsApiControllerTest {

    @Mock
    private ResultService resultService;

    @InjectMocks
    private ResultsApiController resultsApiController;

    @Nested
    class GetResultById {

        @Test
        void whenResultExists_returnsGeneratedResponse() {
            GetResultByIdResponseResults response = GetResultByIdResponseResults.builder()
                .resultId("ABC")
                .resultTitle("Result AAA-BBB")
                .resultTitleCy("Result AAA-BBB CY")
                .build();
            when(resultService.getResult("ABC", false)).thenReturn(response);

            ResponseEntity<GetResultByIdResponseResults> result = resultsApiController.getResultById("ABC", false);

            assertAll(
                () -> assertEquals(HttpStatus.OK, result.getStatusCode()),
                () -> assertEquals(response, result.getBody()),
                () -> verify(resultService).getResult("ABC", false)
            );
        }

        @Test
        void whenResultExists_returnsMappedResponseFields() {
            GetResultByIdResponseResults response = GetResultByIdResponseResults.builder()
                .resultId("ABC")
                .resultTitle("Some Title")
                .resultTitleCy("Welsh Title")
                .resultType("Action")
                .active(true)
                .allowAdditionalAction(true)
                .allowPaymentTerms(false)
                .generatesWarrant(true)
                .requiresLja(false)
                .manualEnforcement(true)
                .build();
            when(resultService.getResult("ABC", false)).thenReturn(response);

            ResponseEntity<GetResultByIdResponseResults> result = resultsApiController.getResultById("ABC", false);

            assertAll(
                () -> assertEquals(HttpStatus.OK, result.getStatusCode()),
                () -> assertEquals("ABC", result.getBody().getResultId()),
                () -> assertEquals("Some Title", result.getBody().getResultTitle()),
                () -> assertEquals("Welsh Title", result.getBody().getResultTitleCy()),
                () -> assertEquals("Action", result.getBody().getResultType()),
                () -> assertEquals(true, result.getBody().getActive()),
                () -> assertEquals(true, result.getBody().getAllowAdditionalAction()),
                () -> assertEquals(false, result.getBody().getAllowPaymentTerms()),
                () -> assertEquals(true, result.getBody().getGeneratesWarrant()),
                () -> assertEquals(false, result.getBody().getRequiresLja()),
                () -> assertEquals(true, result.getBody().getManualEnforcement())
            );
        }

        @Test
        void whenWelshIsRequested_passesFlagToService() {
            GetResultByIdResponseResults response = GetResultByIdResponseResults.builder()
                .resultId("ABC")
                .build();
            when(resultService.getResult("ABC", true)).thenReturn(response);

            ResponseEntity<GetResultByIdResponseResults> result = resultsApiController.getResultById("ABC", true);

            assertAll(
                () -> assertEquals(HttpStatus.OK, result.getStatusCode()),
                () -> assertEquals(response, result.getBody()),
                () -> verify(resultService).getResult("ABC", true)
            );
        }
    }
}
