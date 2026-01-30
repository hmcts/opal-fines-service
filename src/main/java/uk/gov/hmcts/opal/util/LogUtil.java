package uk.gov.hmcts.opal.util;

import com.microsoft.applicationinsights.extensibility.context.OperationContext;
import com.microsoft.applicationinsights.telemetry.BaseTelemetry;
import com.microsoft.applicationinsights.telemetry.TelemetryContext;
import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Slf4j
public final class LogUtil {

    private LogUtil() {

    }

    // Common error messages
    // =====================
    // These can be refactored to an error message mapping module for simplified
    // error messages to be returned to clients

    public static final String ERRMSG_STORED_PROC_FAILURE = "Stored Procedure Failure.";

    public static String getOrCreateOpalOperationId() {
        return getOperationContext()
            .map(OperationContext::getId)
            .orElseGet(LogUtil::createOpalOperation);
    }

    public static String createOpalOperation() {
        String operationId = UUID.randomUUID().toString().replace("-", "");
        log.debug("Created new operation with ID: {}", operationId);

        //Update the operation context if it exists, otherwise use MDC
        Optional<OperationContext> operationContextOpt = getOperationContext();
        if (operationContextOpt.isPresent()) {
            OperationContext operationContext = operationContextOpt.get();
            operationContext.setId(operationId);
            return operationId;
        } else {
            MDC.put("opal-operation-id", operationId);
        }
        return operationId;
    }

    private static Optional<OperationContext> getOperationContext() {
        return Optional.ofNullable(ThreadContext.getRequestTelemetryContext())
            .map(RequestTelemetryContext::getHttpRequestTelemetry)
            .map(BaseTelemetry::getContext)
            .map(TelemetryContext::getOperation);
    }

    public static String getIpAddress() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return null;
        }

        Object details = auth.getDetails();
        if (details instanceof WebAuthenticationDetails) {
            return ((WebAuthenticationDetails) details).getRemoteAddress();
        }

        return null;
    }

    public static OffsetDateTime getCurrentDateTime(Clock clock) {
        return OffsetDateTime.now(clock);
    }
}
