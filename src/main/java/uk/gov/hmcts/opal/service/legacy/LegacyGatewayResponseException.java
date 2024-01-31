package uk.gov.hmcts.opal.service.legacy;

public class LegacyGatewayResponseException extends RuntimeException {

    public LegacyGatewayResponseException(String msg) {
        super(msg);
    }

    public LegacyGatewayResponseException(Exception e) {
        super(e);
    }
}
