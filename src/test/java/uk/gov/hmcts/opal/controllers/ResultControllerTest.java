package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
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
import uk.gov.hmcts.opal.generated.model.ResultsRefDataResponse;
import uk.gov.hmcts.opal.service.opal.ResultService;

@ExtendWith(MockitoExtension.class)
class ResultControllerTest {

    @Mock
    private ResultService resultService;

    @Mock
    private FeatureToggleApi featureToggleApi;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ResultsApiController resultsApiController;

    @Test
    void getResults_allowsUnfilteredRequestWithoutCheckingRelease1b() {
        ResultsRefDataResponse dto = ResultsRefDataResponse.builder().count(0).refData(List.of()).build();
        when(request.getParameterMap()).thenReturn(Map.of());
        when(resultService.getResultsByIds(null, null, null, null, null, null)).thenReturn(dto);

        ResponseEntity<ResultsRefDataResponse> response = resultsApiController.getResults(
            null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(resultService).getResultsByIds(null, null, null, null, null, null);
        verifyNoInteractions(featureToggleApi);
    }

    @Test
    void getResults_allowsResultIdsWithoutCheckingRelease1b() {
        ResultsRefDataResponse dto = ResultsRefDataResponse.builder().count(0).refData(List.of()).build();
        List<String> resultIds = List.of("AAAAAA", "BBBBBB");
        when(request.getParameterMap()).thenReturn(Map.of("result_ids", new String[] {"AAAAAA,BBBBBB"}));
        when(resultService.getResultsByIds(resultIds, null, null, null, null, null)).thenReturn(dto);

        ResponseEntity<ResultsRefDataResponse> response = resultsApiController.getResults(
            resultIds, null, null, null, null, null);

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
        when(request.getParameterMap()).thenReturn(Map.of(filterParameter, new String[] {""}));
        when(featureToggleApi.isFeatureEnabled("release-1b")).thenReturn(false);

        FeatureDisabledException exception = assertThrows(FeatureDisabledException.class,
            () -> resultsApiController.getResults(null, null, null, null, null, null));

        assertEquals("Feature release-1b is not enabled for results filtering", exception.getMessage());
        verify(featureToggleApi).isFeatureEnabled("release-1b");
        verifyNoInteractions(resultService);
    }

    @Test
    void getResults_allowsRelease1bFiltersWhenFlagEnabled() {
        ResultsRefDataResponse dto = ResultsRefDataResponse.builder().count(0).refData(List.of()).build();
        List<String> resultIds = List.of("AAAAAA", "BBBBBB");
        when(request.getParameterMap()).thenReturn(Map.of("active", new String[] {"true"}));
        when(featureToggleApi.isFeatureEnabled("release-1b")).thenReturn(true);
        when(resultService.getResultsByIds(resultIds, true, true, false, true, false)).thenReturn(dto);

        ResponseEntity<ResultsRefDataResponse> response = resultsApiController.getResults(
            resultIds, true, true, false, true, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(featureToggleApi).isFeatureEnabled("release-1b");
        verify(resultService).getResultsByIds(resultIds, true, true, false, true, false);
    }
}
