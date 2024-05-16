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
            SftpLocation.ALL_PAY,
            SftpLocation.ARCHIVE
        );

        List<SftpLocation> result = SftpLocation.getOutboundLocations();
        assertEquals(2, outboundLocations.size());
        assertEquals(outboundLocations, result);
    }

    @Test
    void testGetInboundLocations() {
        List<SftpLocation> inboundLocations = Arrays.asList(
            SftpLocation.AUTO_CHEQUES,
            SftpLocation.AUTO_CHEQUES_SUCCESS,
            SftpLocation.AUTO_CHEQUES_ERROR,
            SftpLocation.AUTO_CASH,
            SftpLocation.AUTO_CASH_SUCCESS,
            SftpLocation.AUTO_CASH_ERROR,
            SftpLocation.NATWEST,
            SftpLocation.NATWEST_SUCCESS,
            SftpLocation.NATWEST_ERROR,
            SftpLocation.ALL_PAY_BT_BARCLAY_CARD,
            SftpLocation.ALL_PAY_BT_BARCLAY_CARD_SUCCESS,
            SftpLocation.ALL_PAY_BT_BARCLAY_CARD_ERROR,
            SftpLocation.DWP_BAILIFFS,
            SftpLocation.DWP_BAILIFFS_SUCCESS,
            SftpLocation.DWP_BAILIFFS_ERROR
        );
        List<SftpLocation> outboundLocations = Arrays.asList(SftpLocation.ALL_PAY, SftpLocation.ARCHIVE);

        assertEquals(15, SftpLocation.getInboundLocations().size());
        assertEquals(inboundLocations, SftpLocation.getInboundLocations());
        assertEquals(outboundLocations, SftpLocation.getOutboundLocations());
    }

    @Test
    void testGetDirection() {
        assertEquals(SftpDirection.INBOUND, SftpLocation.AUTO_CHEQUES.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.AUTO_CHEQUES_SUCCESS.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.AUTO_CHEQUES_ERROR.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.AUTO_CASH.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.AUTO_CASH_SUCCESS.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.AUTO_CASH_ERROR.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.NATWEST.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.NATWEST_SUCCESS.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.NATWEST_ERROR.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.ALL_PAY_BT_BARCLAY_CARD.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.ALL_PAY_BT_BARCLAY_CARD_SUCCESS.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.ALL_PAY_BT_BARCLAY_CARD_ERROR.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.DWP_BAILIFFS.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.DWP_BAILIFFS_SUCCESS.getDirection());
        assertEquals(SftpDirection.INBOUND, SftpLocation.DWP_BAILIFFS_ERROR.getDirection());
        assertEquals(SftpDirection.OUTBOUND, SftpLocation.ALL_PAY.getDirection());
        assertEquals(SftpDirection.OUTBOUND, SftpLocation.ARCHIVE.getDirection());
    }

    @Test
    void testGetPath() {
        assertEquals("auto-cheque", SftpLocation.AUTO_CHEQUES.getPath());
        assertEquals("auto-cheque/success", SftpLocation.AUTO_CHEQUES_SUCCESS.getPath());
        assertEquals("auto-cheque/error", SftpLocation.AUTO_CHEQUES_ERROR.getPath());
        assertEquals("auto-cash", SftpLocation.AUTO_CASH.getPath());
        assertEquals("auto-cash/success", SftpLocation.AUTO_CASH_SUCCESS.getPath());
        assertEquals("auto-cash/error", SftpLocation.AUTO_CASH_ERROR.getPath());
        assertEquals("natwest", SftpLocation.NATWEST.getPath());
        assertEquals("natwest/success", SftpLocation.NATWEST_SUCCESS.getPath());
        assertEquals("natwest/error", SftpLocation.NATWEST_ERROR.getPath());
        assertEquals("allpay", SftpLocation.ALL_PAY_BT_BARCLAY_CARD.getPath());
        assertEquals("allpay/success", SftpLocation.ALL_PAY_BT_BARCLAY_CARD_SUCCESS.getPath());
        assertEquals("allpay/error", SftpLocation.ALL_PAY_BT_BARCLAY_CARD_ERROR.getPath());
        assertEquals("dwp-bailiffs", SftpLocation.DWP_BAILIFFS.getPath());
        assertEquals("dwp-bailiffs/success", SftpLocation.DWP_BAILIFFS_SUCCESS.getPath());
        assertEquals("dwp-bailiffs/error", SftpLocation.DWP_BAILIFFS_ERROR.getPath());
        assertEquals("allpay", SftpLocation.ALL_PAY.getPath());
        assertEquals("allpay-archive", SftpLocation.ARCHIVE.getPath());
    }

    @Test
    void testGetDescription() {
        assertNotNull(SftpLocation.AUTO_CHEQUES.getDescription());
        assertNotNull(SftpLocation.AUTO_CHEQUES_SUCCESS.getDescription());
        assertNotNull(SftpLocation.AUTO_CHEQUES_ERROR.getDescription());
        assertNotNull(SftpLocation.AUTO_CASH.getDescription());
        assertNotNull(SftpLocation.AUTO_CASH_SUCCESS.getDescription());
        assertNotNull(SftpLocation.AUTO_CASH_ERROR.getDescription());
        assertNotNull(SftpLocation.NATWEST.getDescription());
        assertNotNull(SftpLocation.NATWEST_SUCCESS.getDescription());
        assertNotNull(SftpLocation.NATWEST_ERROR.getDescription());
        assertNotNull(SftpLocation.ALL_PAY_BT_BARCLAY_CARD.getDescription());
        assertNotNull(SftpLocation.ALL_PAY_BT_BARCLAY_CARD_SUCCESS.getDescription());
        assertNotNull(SftpLocation.ALL_PAY_BT_BARCLAY_CARD_ERROR.getDescription());
        assertNotNull(SftpLocation.DWP_BAILIFFS.getDescription());
        assertNotNull(SftpLocation.DWP_BAILIFFS_SUCCESS.getDescription());
        assertNotNull(SftpLocation.DWP_BAILIFFS_ERROR.getDescription());
        assertNotNull(SftpLocation.ALL_PAY.getDescription());
        assertNotNull(SftpLocation.ARCHIVE.getDescription());
    }
}
