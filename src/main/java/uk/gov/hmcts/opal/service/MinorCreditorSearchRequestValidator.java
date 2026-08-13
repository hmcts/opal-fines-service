package uk.gov.hmcts.opal.service;

import static uk.gov.hmcts.opal.SchemaPaths.POST_MINOR_CREDITOR_ACCOUNTS_SEARCH_REQUEST;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@Service
@RequiredArgsConstructor
public class MinorCreditorSearchRequestValidator {

    private final JsonSchemaValidationService jsonSchemaValidationService;

    public void validateAndCheckFeature(MinorCreditorSearch request) {
        jsonSchemaValidationService.validateOrError(request.toJson(), POST_MINOR_CREDITOR_ACCOUNTS_SEARCH_REQUEST);
    }
}
