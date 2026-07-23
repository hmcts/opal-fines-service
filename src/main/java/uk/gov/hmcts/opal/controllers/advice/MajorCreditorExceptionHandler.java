package uk.gov.hmcts.opal.controllers.advice;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.common.controllers.advice.OpalProblemDetailFactory;
import uk.gov.hmcts.opal.controllers.MajorCreditorApiController;

@Slf4j(topic = "opal.MajorCreditorExceptionHandler")
@ControllerAdvice(assignableTypes = MajorCreditorApiController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MajorCreditorExceptionHandler {

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ProblemDetail> handleHttpServerErrorException(HttpServerErrorException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        ProblemDetail problemDetail = OpalProblemDetailFactory.createProblemDetail(
            status,
            status.getReasonPhrase(),
            Optional.ofNullable(ex.getStatusText()).filter(text -> !text.isBlank()).orElse(ex.getMessage()),
            "http-server-error",
            status == HttpStatus.SERVICE_UNAVAILABLE,
            ex,
            log
        );

        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problemDetail);
    }
}
