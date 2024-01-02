package uk.gov.hmcts.opal.authentication.model;

public record Session(String sessionId, String accessToken, long accessTokenExpiresIn) {
}
