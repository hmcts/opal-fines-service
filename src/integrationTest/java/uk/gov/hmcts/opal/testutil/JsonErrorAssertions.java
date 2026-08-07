package uk.gov.hmcts.opal.testutil;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultMatcher;

public final class JsonErrorAssertions {

    private JsonErrorAssertions() {
    }

    public static ResultMatcher expectError(HttpStatus status, String title, String detail, String type) {
        return result -> {
            jsonPath("$.type").value(type).match(result);
            jsonPath("$.title").value(title).match(result);
            jsonPath("$.status").value(status.value()).match(result);
            jsonPath("$.detail").value(detail).match(result);
        };
    }

    public static ResultMatcher expectError(HttpStatus status, String title, String detail) {
        return result -> {
            jsonPath("$.title").value(title).match(result);
            jsonPath("$.status").value(status.value()).match(result);
            jsonPath("$.detail").value(detail).match(result);
        };
    }

    public static ResultMatcher expectErrorWithoutStatus(String title, String detail, String type) {
        return result -> {
            jsonPath("$.type").value(type).match(result);
            jsonPath("$.title").value(title).match(result);
            jsonPath("$.detail").value(detail).match(result);
        };
    }

    public static ResultMatcher expectEntityNotFound() {
        return expectError(
            HttpStatus.NOT_FOUND,
            "Entity Not Found",
            "The requested entity could not be found",
            "https://hmcts.gov.uk/problems/entity-not-found"
        );
    }

    public static ResultMatcher expectEntityNotFoundWithoutType() {
        return expectError(
            HttpStatus.NOT_FOUND,
            "Entity Not Found",
            "The requested entity could not be found"
        );
    }

    public static ResultMatcher expectBadRequest(String detail, String type) {
        return expectError(HttpStatus.BAD_REQUEST, "Bad Request", detail, type);
    }

    public static ResultMatcher expectBadRequestWithoutStatus(String detail, String type) {
        return expectErrorWithoutStatus("Bad Request", detail, type);
    }

    public static ResultMatcher expectInternalServerErrorWithoutStatus(String detail) {
        return expectErrorWithoutStatus(
            "Internal Server Error",
            detail,
            "https://hmcts.gov.uk/problems/internal-server-error"
        );
    }
}
