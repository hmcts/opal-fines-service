package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_SUSPENSE;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_SUSPENSE_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildCreatedResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.SuspenseItemsApi;
import uk.gov.hmcts.opal.generated.model.SuspenseItemTransactionRequestSuspenseItems;
import uk.gov.hmcts.opal.generated.model.SuspenseItemTransactionResponseSuspenseItems;
import uk.gov.hmcts.opal.service.opal.SuspenseItemTransactionService;

@RestController
@RequiredArgsConstructor
public class SuspenseItemsApiController implements SuspenseItemsApi {

    private final SuspenseItemTransactionService service;

    @Override
    @FeatureToggle(feature = RELEASE_1C_SUSPENSE, defaultValueProperty = RELEASE_1C_SUSPENSE_ENABLED_PROPERTY)
    public ResponseEntity<SuspenseItemTransactionResponseSuspenseItems> createSuspenseItemTransaction(
        Long id, Short businessUnitId, String ifMatch, SuspenseItemTransactionRequestSuspenseItems request) {

        return buildCreatedResponse(service.createTransaction(id, businessUnitId, ifMatch, request));
    }
}
