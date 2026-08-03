package uk.gov.hmcts.opal.service;

import static uk.gov.hmcts.opal.SchemaPaths.POST_MINOR_CREDITOR_ACCOUNTS_SEARCH_REQUEST;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@Service
@RequiredArgsConstructor
public class MinorCreditorSearchRequestValidator {

    private final JsonSchemaValidationService jsonSchemaValidationService;

    public void validateAndCheckFeature(MinorCreditorSearch request) {
        jsonSchemaValidationService.validateOrError(toJson(request), POST_MINOR_CREDITOR_ACCOUNTS_SEARCH_REQUEST);
    }

    private String toJson(MinorCreditorSearch request) {
        try {
            return ToJsonString.getObjectMapper().writeValueAsString(request);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Unable to parse minor creditor search request", e);
        }
    }
}
