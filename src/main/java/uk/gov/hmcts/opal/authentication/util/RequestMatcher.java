package uk.gov.hmcts.opal.authentication.util;

import jakarta.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface RequestMatcher {

    String internalUrlMatcher = "/internal-user";

    String externalUrlMatcher = "/external-user";

    RequestMatcher URL_MAPPER_INTERNAL = (req) -> req.getRequestURL().toString().contains(internalUrlMatcher);


    RequestMatcher URL_MAPPER_EXTERNAL = (req) -> req.getRequestURL().toString().contains(externalUrlMatcher);

    boolean doesMatch(HttpServletRequest req);
}
