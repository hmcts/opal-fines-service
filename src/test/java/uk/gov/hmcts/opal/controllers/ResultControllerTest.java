package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.dto.ResultDto;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResponse;
import uk.gov.hmcts.opal.service.opal.ResultService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultControllerTest {

    @Mock
    private ResultService resultService;

    @Mock
    private FeatureToggleApi featureToggleApi;

    @InjectMocks
    private ResultController resultController;

    @Test
    void testGetResultDto_Success() {
        // Arrange: Build a simple DTO
        ResultDto dto = ResultDto.builder()
            .resultId("ABC")
            .resultTitle("Result AAA-BBB")
            .resultTitleCy("Result AAA-BBB CY")
            .build();

        when(resultService.getResult(anyString())).thenReturn(dto);

        // Act
        ResponseEntity<ResultDto> response = resultController.getResultById("ABC");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(resultService).getResult("ABC");
    }

    @Test
    void testGetResultDto_Success_WithSimpleJsonExpectation() {
        // Arrange
        ResultDto dto = ResultDto.builder()
            .resultId("ABC")
            .resultTitle("Some Title")
            .resultTitleCy("Welsh Title")
            .resultType("TYPE1")
            .active(true)
            .build();

        when(resultService.getResult("ABC")).thenReturn(dto);

        // Act
        ResponseEntity<ResultDto> response = resultController.getResultById("ABC");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ABC", response.getBody().getResultId());
        assertEquals("Some Title", response.getBody().getResultTitle());
        assertEquals("Welsh Title", response.getBody().getResultTitleCy());
        assertEquals("TYPE1", response.getBody().getResultType());
        assertEquals(true, response.getBody().isActive());
    }

    @Test
    void getResults_allowsUnfilteredRequestWithoutCheckingRelease1b() {
        // Arrange
        ResultReferenceDataResponse dto = ResultReferenceDataResponse.builder()
            .refData(List.of())
            .build();
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();

        when(resultService.getResultsByIds(Optional.empty(), null, null, null, null, null)).thenReturn(dto);

        // Act
        ResponseEntity<ResultReferenceDataResponse> response = resultController.getResults(
            requestParams, Optional.empty(), null, null, null, null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(resultService).getResultsByIds(Optional.empty(), null, null, null, null, null);
        verifyNoInteractions(featureToggleApi);
    }

    @Test
    void getResults_allowsResultIdsWithoutCheckingRelease1b() {
        // Arrange
        ResultReferenceDataResponse dto = ResultReferenceDataResponse.builder()
            .refData(List.of())
            .build();
        Optional<List<String>> resultIds = Optional.of(List.of("AAAAAA", "BBBBBB"));
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("result_ids", "AAAAAA,BBBBBB");

        when(resultService.getResultsByIds(resultIds, null, null, null, null, null)).thenReturn(dto);

        // Act
        ResponseEntity<ResultReferenceDataResponse> response = resultController.getResults(
            requestParams, resultIds, null, null, null, null, null);

        // Assert
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
        // Arrange
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add(filterParameter, "");

        when(featureToggleApi.isFeatureEnabled("release-1b")).thenReturn(false);

        // Act
        FeatureDisabledException exception = assertThrows(FeatureDisabledException.class,
            () -> resultController.getResults(
            requestParams, Optional.empty(), null, null, null, null, null));

        // Assert
        assertEquals("Feature release-1b is not enabled for results filtering", exception.getMessage());
        verify(featureToggleApi).isFeatureEnabled("release-1b");
        verifyNoInteractions(resultService);
    }

    @Test
    void getResults_allowsRelease1bFiltersWhenFlagEnabled() {
        // Arrange
        final ResultReferenceDataResponse dto = ResultReferenceDataResponse.builder()
            .refData(List.of())
            .build();
        final Optional<List<String>> resultIds = Optional.of(List.of("AAAAAA", "BBBBBB"));
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("result_ids", "AAAAAA,BBBBBB");
        requestParams.add("active", "true");
        requestParams.add("manual_enforcement_only", "true");
        requestParams.add("generates_hearing", "false");
        requestParams.add("enforcement", "true");
        requestParams.add("enforcement_override", "false");

        when(featureToggleApi.isFeatureEnabled("release-1b")).thenReturn(true);
        when(resultService.getResultsByIds(resultIds, true, true, false, true, false)).thenReturn(dto);

        // Act
        ResponseEntity<ResultReferenceDataResponse> response = resultController.getResults(
            requestParams, resultIds, true, true, false, true, false);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(featureToggleApi).isFeatureEnabled("release-1b");
        verify(resultService).getResultsByIds(resultIds, true, true, false, true, false);
    }

}
