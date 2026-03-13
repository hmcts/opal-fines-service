package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;

class LegacyDefAccServiceMappingHelperTest extends AbstractLegacyDefAccServiceTest {

    @Test
    void mapLegacyPostedDetails_null_returnsNull() {
        Object result = legacyDefendantAccountService.mapLegacyPostedDetails(null);

        assertNull(result);
    }

    @Test
    void mapLegacyPostedDetails_mapsFieldsCorrectly() {
        PostedDetails pd = new PostedDetails();
        pd.setPostedBy("tester");
        pd.setPostedByName("Test User");
        pd.setPostedDate(LocalDateTime.of(2024, 1, 1, 10, 31, 45));

        LegacyPostedDetails out = legacyDefendantAccountService.mapLegacyPostedDetails(pd);

        assertNotNull(out);
        assertEquals(pd.getPostedBy(), out.getPostedBy());
        assertEquals(pd.getPostedByName(), out.getPostedByName());
        assertEquals(pd.getPostedDate(), out.getPostedDate());
    }

    @Test
    void mapLegacyPaymentTermsType_nullOrMissingCode_returnsNull() {
        assertNull(legacyDefendantAccountService.mapLegacyPaymentTermsType(null));

        PaymentTermsType pt = mock(PaymentTermsType.class);
        when(pt.getPaymentTermsTypeCode()).thenReturn(null);
        assertNull(legacyDefendantAccountService.mapLegacyPaymentTermsType(pt));
    }

    @Test
    void mapLegacyPaymentTermsType_validCode_mapsCorrectEnum() {
        PaymentTermsType pt = mock(PaymentTermsType.class);
        when(pt.getPaymentTermsTypeCode()).thenReturn(PaymentTermsType.PaymentTermsTypeCode.B);

        LegacyPaymentTermsType out = legacyDefendantAccountService.mapLegacyPaymentTermsType(pt);

        assertNotNull(out);
        assertEquals(LegacyPaymentTermsType.PaymentTermsTypeCode.B, out.getPaymentTermsTypeCode());
    }

    @Test
    void mapLegacyInstalmentPeriod_nullOrMissingCode_returnsNull() {
        assertNull(legacyDefendantAccountService.mapLegacyInstalmentPeriod(null));

        InstalmentPeriod ip = mock(InstalmentPeriod.class);
        when(ip.getInstalmentPeriodCode()).thenReturn(null);
        assertNull(legacyDefendantAccountService.mapLegacyInstalmentPeriod(ip));
    }

    @Test
    void mapLegacyInstalmentPeriod_validCode_mapsCorrectEnum() {
        InstalmentPeriod ip = mock(InstalmentPeriod.class);
        when(ip.getInstalmentPeriodCode()).thenReturn(InstalmentPeriod.InstalmentPeriodCode.W);

        LegacyInstalmentPeriod out = legacyDefendantAccountService.mapLegacyInstalmentPeriod(ip);

        assertNotNull(out);
        assertEquals(LegacyInstalmentPeriod.InstalmentPeriodCode.W, out.getInstalmentPeriodCode());
    }

    @Test
    void mapPaymentTermsTypeCodeEnum_validAndInvalid() {
        assertEquals(LegacyPaymentTermsType.PaymentTermsTypeCode.B,
            legacyDefendantAccountService.mapPaymentTermsTypeCodeEnum("B"));
        assertEquals(LegacyPaymentTermsType.PaymentTermsTypeCode.P,
            legacyDefendantAccountService.mapPaymentTermsTypeCodeEnum("p"));
        assertNull(legacyDefendantAccountService.mapPaymentTermsTypeCodeEnum(null));

        assertThrows(IllegalArgumentException.class,
            () -> legacyDefendantAccountService.mapPaymentTermsTypeCodeEnum("X"));
    }

    @Test
    void mapInstalmentPeriodCodeEnum_validAndInvalid() {
        assertEquals(LegacyInstalmentPeriod.InstalmentPeriodCode.W,
            legacyDefendantAccountService.mapInstalmentPeriodCodeEnum("W"));
        assertEquals(LegacyInstalmentPeriod.InstalmentPeriodCode.M,
            legacyDefendantAccountService.mapInstalmentPeriodCodeEnum("m"));
        assertNull(legacyDefendantAccountService.mapInstalmentPeriodCodeEnum(null));

        assertThrows(IllegalArgumentException.class,
            () -> legacyDefendantAccountService.mapInstalmentPeriodCodeEnum("Z"));
    }
}
