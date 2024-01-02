package uk.gov.hmcts.opal.authentication.model;

public record JwtValidationResult(boolean valid, String reason) {

}
