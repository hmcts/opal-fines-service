package uk.gov.hmcts.opal.sftp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SftpLocationTest {

    @Test
    void testGetOutboundLocations() {
        List<SftpLocation> outboundLocations = Arrays.asList(
            SftpLocation.PRINT_LOCATION
        );

        List<SftpLocation> result = SftpLocation.getOutboundLocations();
        assertEquals(1, outboundLocations.size());
        assertEquals(outboundLocations, result);
    }

    @Test
    void testGetDirection() {
        assertEquals(SftpDirection.OUTBOUND, SftpLocation.PRINT_LOCATION.getDirection());
    }

    @Test
    void testGetPath() {
        assertEquals("print", SftpLocation.PRINT_LOCATION.getPath());
    }

    @Test
    void testGetDescription() {
        assertNotNull(SftpLocation.PRINT_LOCATION.getDescription());
    }
}
