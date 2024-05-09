package uk.gov.hmcts.opal.sftp;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.opal.sftp.SftpDirection.INBOUND;
import static uk.gov.hmcts.opal.sftp.SftpDirection.OUTBOUND;

@Getter
public enum SftpLocation {

    AUTO_CHEQUES(INBOUND, "auto-cheque", "Comes from OAGS (pushed)"),
    AUTO_CASH(INBOUND, "auto-cash", "Comes from OAGS (pushed)"),
    NATWEST(INBOUND, "natwest", "Comes from BAIS (pulled)"),
    ALL_PAY_BT_BARCLAY_CARD(INBOUND, "allpay", "Comes from BAIS (pulled)"),
    DWP_BAILIFFS(INBOUND, "dwp-bailiffs", "Comes from BAIS (pulled)"),

    ALL_PAY(OUTBOUND, "allpay", "Goes to BAIS (pushed)"),
    ARCHIVE(OUTBOUND, "allpay-archive", "Goes to OAGS (pushed)");

    private final SftpDirection direction;
    private final String path;
    private final String description;

    SftpLocation(SftpDirection direction, String path, String description) {
        this.direction = direction;
        this.path = path;
        this.description = description;
    }

    public static List<SftpLocation> getLocations(SftpDirection direction) {
        return Arrays.stream(SftpLocation.values())
            .filter(location -> location.getDirection() == direction)
            .collect(Collectors.toList());
    }

    public static List<SftpLocation> getInboundLocations() {
        return getLocations(INBOUND);
    }

    public static List<SftpLocation> getOutboundLocations() {
        return getLocations(OUTBOUND);
    }

}
