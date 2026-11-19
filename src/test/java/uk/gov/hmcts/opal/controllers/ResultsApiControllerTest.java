package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.generated.model.GetResultByIdResponseResults;
import uk.gov.hmcts.opal.generated.model.ResultsRefDataResponse;
import uk.gov.hmcts.opal.service.proxy.ResultsServiceProxy;

@ExtendWith(MockitoExtension.class)
class ResultsApiControllerTest {

    @Mock
    private ResultsServiceProxy resultService;

    @Mock
    private FeatureToggleApi featureToggleApi;

    @Mock
    private HttpServletRequest request;

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

    @Nested
    class GetResults {

        @Test
        void getResults_allowsUnfilteredRequestWithoutCheckingRelease1b() {
            // Given
            ResultsRefDataResponse dto = ResultsRefDataResponse.builder().count(0).refData(List.of()).build();
            when(request.getParameterMap()).thenReturn(Map.of());
            when(resultService.getResultsByIds(null, null, null, null, null, null)).thenReturn(dto);

            // When
            ResponseEntity<ResultsRefDataResponse> response = resultsApiController.getResults(
                null, null, null, null, null, null);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(dto, response.getBody());
            verify(resultService).getResultsByIds(null, null, null, null, null, null);
            verifyNoInteractions(featureToggleApi);
        }

        @Test
        void getResults_allowsResultIdsWithoutCheckingRelease1b() {
            // Given
            ResultsRefDataResponse dto = ResultsRefDataResponse.builder().count(0).refData(List.of()).build();
            List<String> resultIds = List.of("AAAAAA", "BBBBBB");
            when(request.getParameterMap()).thenReturn(Map.of("result_ids", new String[] {"AAAAAA,BBBBBB"}));
            when(resultService.getResultsByIds(resultIds, null, null, null, null, null)).thenReturn(dto);

            // When
            ResponseEntity<ResultsRefDataResponse> response = resultsApiController.getResults(
                resultIds, null, null, null, null, null);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(dto, response.getBody());
            verify(resultService).getResultsByIds(resultIds, null, null, null, null, null);
            verifyNoInteractions(featureToggleApi);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "active",
            "manual_enforcement_only",
            "generates_hearing",
            "enforcement",
            "enforcement_override"
        })
        void getResults_rejectsRelease1bFilterWhenFlagDisabled(String filterParameter) {
            // Given
            when(request.getParameterMap()).thenReturn(Map.of(filterParameter, new String[] {""}));
            when(featureToggleApi.isFeatureEnabled("release-1b")).thenReturn(false);

            // When
            FeatureDisabledException exception = assertThrows(FeatureDisabledException.class,
                () -> resultsApiController.getResults(null, null, null, null, null, null));

            // Then
            assertEquals("Feature release-1b is not enabled for results filtering", exception.getMessage());
            verify(featureToggleApi).isFeatureEnabled("release-1b");
            verifyNoInteractions(resultService);
        }

        @Test
        void getResults_allowsRelease1bFiltersWhenFlagEnabled() {
            // Given
            ResultsRefDataResponse dto = ResultsRefDataResponse.builder().count(0).refData(List.of()).build();
            List<String> resultIds = List.of("AAAAAA", "BBBBBB");
            when(request.getParameterMap()).thenReturn(Map.of("active", new String[] {"true"}));
            when(featureToggleApi.isFeatureEnabled("release-1b")).thenReturn(true);
            when(resultService.getResultsByIds(resultIds, true, true, false, true, false)).thenReturn(dto);

            // When
            ResponseEntity<ResultsRefDataResponse> response = resultsApiController.getResults(
                resultIds, true, true, false, true, false);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(dto, response.getBody());
            verify(featureToggleApi).isFeatureEnabled("release-1b");
            verify(resultService).getResultsByIds(resultIds, true, true, false, true, false);
        }
    }
}
