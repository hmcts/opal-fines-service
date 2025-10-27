package uk.gov.hmcts.opal.spring.exceptions;

import java.net.URI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import uk.gov.hmcts.opal.util.LogUtil;

@Slf4j
@Getter
public class OpalException extends RuntimeException {


    private final HttpStatus status;
    private final String title;
    private final String detail;
    private final boolean retryable;

    public OpalException(HttpStatus status, String title, String detail, boolean retryable) {
        super(String.format("%s: %s", title, detail));
        this.status = status;
        this.title = title;
        this.detail = detail;
        this.retryable = retryable;
    }


    protected String getTypeUri() {
        return this.getClass().getSimpleName();
    }

    public ProblemDetail toProblemDetail() {
        String opalOperationId = LogUtil.getOrCreateOpalOperationId();
        log.error("Error ID {}:", opalOperationId, this);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create("https://hmcts.gov.uk/problems/" + getTypeUri()));
        problemDetail.setInstance(URI.create("https://hmcts.gov.uk/problems/instance/" + opalOperationId));
        problemDetail.setProperty("operation_id", opalOperationId);
        problemDetail.setProperty("retriable", retryable);
        return problemDetail;
    }
}
