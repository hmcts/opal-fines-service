package uk.gov.hmcts.opal.spring.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidContentDigestException extends OpalException {

    public InvalidContentDigestException(String title, String detail) {
        super(HttpStatus.BAD_REQUEST, title, detail, true);
    }
}
