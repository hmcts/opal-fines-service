package uk.gov.hmcts.opal.entity;

import lombok.Getter;

/**
 * Codes persisted for the source of a credit transfer.
 */
@Getter
public enum TransferSource {
    NAT_WEST("NATWEST"),
    ALLPAY("ALLPAY"),
    BARCLAYCARD("BARCLAYCARD"),
    BRITISH_TELECOM("BTECKOH"),
    OTHER("OTHER"),
    DWP("DWP"),
    CDER("CDER"),
    JACOBS("JACOBS"),
    MARSTONS("MARSTON");

    private final String code;

    TransferSource(String code) {
        this.code = code;
    }
}
