package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.generated.http.api.MinorCreditorApi;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditor;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.service.MinorCreditorService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;

@RestController
@Slf4j(topic = "opal.MinorCreditorApiController")
@RequiredArgsConstructor
public class MinorCreditorApiController implements MinorCreditorApi {

    private static final String RELEASE_1B_FEATURE = "release-1b";
    private static final String PATCH_MINOR_CREDITOR_OPERATION = "Update Minor Creditor Account";

    private final MinorCreditorService minorCreditorService;
    private final DynamicConfigService dynamicConfigService;
    private final NativeWebRequest request;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<MinorCreditorAccountResponseMinorCreditor> patchMinorCreditorAccount(
        Long id,
        String businessUnitId,
        Long ifMatch,
        String authHeaderValue,
        PatchMinorCreditorAccountRequest patchMinorCreditorAccountRequest) {

        log.debug(":PATCH:patchMinorCreditorAccount: id={}", id);

        dynamicConfigService.verifyFeatureEnabled(RELEASE_1B_FEATURE, PATCH_MINOR_CREDITOR_OPERATION);

        MinorCreditorAccountResponse result =
            minorCreditorService.updateMinorCreditorAccount(id, patchMinorCreditorAccountRequest,
                Optional.ofNullable(ifMatch).map(java.math.BigInteger::valueOf).orElse(null),
                authHeaderValue, businessUnitId);

        return buildResponse(result);
    }
}
