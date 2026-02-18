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

@RestController
@Slf4j(topic = "opal.MinorCreditorApiController")
@RequiredArgsConstructor
public class MinorCreditorApiController implements MinorCreditorApi {

    private final MinorCreditorService minorCreditorService;
    private final NativeWebRequest request;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<MinorCreditorAccountResponseMinorCreditor> patchMinorCreditorAccount(
        Long id, Long etag, PatchMinorCreditorAccountRequest patchMinorCreditorAccountRequest
    ) {

        log.debug(":PATCH:patchMinorCreditorAccount: id={}", id);

        String authHeaderValue = getRequest().map(r -> r.getHeader("Authorization")).orElse(null);
        MinorCreditorAccountResponse result =
            minorCreditorService.updateMinorCreditorAccount(id, patchMinorCreditorAccountRequest,
                Optional.ofNullable(etag).map(java.math.BigInteger::valueOf).orElse(null),
                authHeaderValue);

        return buildResponse(result);
    }
}
