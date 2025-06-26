package uk.gov.hmcts.opal.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Invokes a check against the HTTP 'Accept' Header to ensure the client can accept the produced response.
 * See the {@link uk.gov.hmcts.opal.interceptor.AcceptHeaderInterceptor#isAcceptableMediaType(String)
 * AcceptHeaderInterceptor} class.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckAcceptHeader {

}
