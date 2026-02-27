package uk.gov.hmcts.opal.controllers;

import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.generated.http.api.VersioningTestApi;
import uk.gov.hmcts.opal.generated.model.TestSchemaResponseV1VersioningTest;
import uk.gov.hmcts.opal.generated.model.TestSchemaResponseV2VersioningTest;

@RestController
public class VersioningTestAPIController implements VersioningTestApi {

    @Override
    public ResponseEntity<TestSchemaResponseV1VersioningTest> getVersionedEndpointsV1_0(String apiVersion,
        @Nullable String authorization) {
        TestSchemaResponseV1VersioningTest body = new TestSchemaResponseV1VersioningTest();
        System.out.println("\ngetVersionedEndpointsV1_0\n");
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<TestSchemaResponseV2VersioningTest> getVersionedEndpointsV2_0(String apiVersion,
        @Nullable String authorization) {
        TestSchemaResponseV2VersioningTest body = new TestSchemaResponseV2VersioningTest();
        System.out.println("\ngetVersionedEndpointsV2_0\n");
        return ResponseEntity.ok(body);
    }
}
