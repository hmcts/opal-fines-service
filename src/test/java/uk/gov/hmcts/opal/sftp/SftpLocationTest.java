package uk.gov.hmcts.opal.sftp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.sftp.SftpDirection.INBOUND;

@ExtendWith(MockitoExtension.class)
class SftpLocationTest {

    @Test
    void testGetInboundLocations() {
        List<SftpLocation> inboundLocations = SftpLocation.getInboundLocations();
        assertEquals(15, inboundLocations.size());
    }

    @Test
    void testGetOutboundLocations() {
        List<SftpLocation> outboundLocations = SftpLocation.getOutboundLocations();
        assertEquals(2, outboundLocations.size());
    }

    @Test
    void testGetLocations() {
        List<SftpLocation> locations = SftpLocation.getLocations(INBOUND);
        assertEquals(15, locations.size());
    }
}
