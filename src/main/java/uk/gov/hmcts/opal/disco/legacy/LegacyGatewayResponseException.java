package uk.gov.hmcts.opal.disco.legacy;

import org.springframework.http.HttpStatusCode;

public class LegacyGatewayResponseException extends RuntimeException {

    public final HttpStatusCode code;

    public LegacyGatewayResponseException(String msg) {
        this(msg, HttpStatusCode.valueOf(999));
    }

    public LegacyGatewayResponseException(String msg, HttpStatusCode code) {
        super(msg);
        this.code = code;
    }

    public LegacyGatewayResponseException(Exception e, HttpStatusCode code) {
        super(e);
        this.code = code;
    }
}
