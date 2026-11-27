package uk.gov.hmcts.opal.controllers;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.generated.http.api.TestingSupportReferenceDataApi;
import uk.gov.hmcts.opal.service.TestingSupportReferenceDataService;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "opal.testing-support-endpoints", name = "enabled", havingValue = "true")
public class TestingSupportReferenceDataApiController implements TestingSupportReferenceDataApi {

    private final TestingSupportReferenceDataService testingSupportReferenceDataService;

    @Override
    public ResponseEntity<Void> deleteReferenceData(String refDataType, String businessKey) {
        testingSupportReferenceDataService.deleteReferenceData(refDataType, businessKey);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> publishReferenceData(Map<String, Object> requestBody) {
        testingSupportReferenceDataService.publishReferenceData(requestBody);
        return ResponseEntity.accepted().build();
    }
}
