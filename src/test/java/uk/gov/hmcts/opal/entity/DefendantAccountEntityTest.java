package uk.gov.hmcts.opal.entity;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccount;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefendantAccountEntityTest {

    @Test
    public void testGettersAndSetters() {
        DefendantAccount.Lite defendantAccount = new DefendantAccount.Lite();
        final LocalDate now = LocalDate.now();

        // Set values using setters
        defendantAccount.setDefendantAccountId(1L);
        defendantAccount.setBusinessUnitId((short)1);
        defendantAccount.setAccountNumber("123456");
        defendantAccount.setImposedHearingDate(now);
        defendantAccount.setImposingCourtId(1L);
        defendantAccount.setAmountImposed(BigDecimal.valueOf(1.1));
        defendantAccount.setAmountPaid(BigDecimal.valueOf(1.1));
        defendantAccount.setAccountBalance(BigDecimal.valueOf(1.1));
        defendantAccount.setAccountStatus("status");
        defendantAccount.setCompletedDate(now);
        defendantAccount.setEnforcingCourtId(1L);
        defendantAccount.setLastHearingCourtId(1L);
        defendantAccount.setLastHearingDate(now);
        defendantAccount.setLastMovementDate(now);
        defendantAccount.setLastEnforcement("123456");
        defendantAccount.setLastChangedDate(now);
        defendantAccount.setOriginatorName("name");
        defendantAccount.setOriginatorId("reference");
        defendantAccount.setOriginatorType("type");
        defendantAccount.setAllowWriteoffs(true);
        defendantAccount.setAllowCheques(true);
        defendantAccount.setChequeClearancePeriod((short)1);
        defendantAccount.setCreditTransferClearancePeriod((short)1);
        defendantAccount.setEnforcementOverrideResultId("123456");
        defendantAccount.setEnforcementOverrideEnforcerId(1L);
        defendantAccount.setEnforcementOverrideTfoLjaId((short)1);
        defendantAccount.setUnitFineDetail("detail");
        defendantAccount.setUnitFineValue(BigDecimal.valueOf(1.1));
        defendantAccount.setCollectionOrder(true);
        defendantAccount.setCollectionOrderEffectiveDate(now);
        defendantAccount.setFurtherStepsNoticeDate(now);
        defendantAccount.setConfiscationOrderDate(now);
        defendantAccount.setFineRegistrationDate(now);
        defendantAccount.setConsolidatedAccountType("type");
        defendantAccount.setPaymentCardRequested(true);
        defendantAccount.setPaymentCardRequestedDate(now);
        defendantAccount.setPaymentCardRequestedBy("requested");
        defendantAccount.setProsecutorCaseReference("reference");
        defendantAccount.setEnforcementCaseStatus("status");
        defendantAccount.setAccountType(DefendantAccountType.FINES);


        // Test getters
        assertEquals(Long.valueOf(1L), defendantAccount.getDefendantAccountId());
        // assertEquals(new BusinessUnit.Lite(), defendantAccount.getBusinessUnit());
        assertEquals("123456", defendantAccount.getAccountNumber());
        assertEquals(now, defendantAccount.getImposedHearingDate());
        assertEquals(Long.valueOf(1L), defendantAccount.getImposingCourtId());
        assertEquals(new BigDecimal("1.1"), defendantAccount.getAmountImposed());
        assertEquals(new BigDecimal("1.1"), defendantAccount.getAmountPaid());
        assertEquals(new BigDecimal("1.1"), defendantAccount.getAccountBalance());
        assertEquals("status", defendantAccount.getAccountStatus());
        assertEquals(now, defendantAccount.getCompletedDate());
        // assertEquals(CourtEntity.Lite.builder().build(), defendantAccount.getEnforcingCourt());
        // assertEquals(CourtEntity.Lite.builder().build(), defendantAccount.getLastHearingCourt());
        assertEquals(now, defendantAccount.getLastHearingDate());
        assertEquals(now, defendantAccount.getLastMovementDate());
        assertEquals("123456", defendantAccount.getLastEnforcement());
        assertEquals(now, defendantAccount.getLastChangedDate());
        assertEquals("name", defendantAccount.getOriginatorName());
        assertEquals("reference", defendantAccount.getOriginatorId());
        assertEquals("type", defendantAccount.getOriginatorType());
        assertTrue(defendantAccount.isAllowWriteoffs());
        assertTrue(defendantAccount.isAllowCheques());
        assertEquals(Short.valueOf((short) 1), defendantAccount.getChequeClearancePeriod());
        assertEquals(Short.valueOf((short) 1), defendantAccount.getCreditTransferClearancePeriod());
        assertEquals("123456", defendantAccount.getEnforcementOverrideResultId());
        assertEquals(Long.valueOf(1L), defendantAccount.getEnforcementOverrideEnforcerId());
        assertEquals(Short.valueOf((short) 1), defendantAccount.getEnforcementOverrideTfoLjaId());
        assertEquals("detail", defendantAccount.getUnitFineDetail());
        assertEquals(new BigDecimal("1.1"), defendantAccount.getUnitFineValue());
        assertTrue(defendantAccount.isCollectionOrder());
        assertEquals(now, defendantAccount.getCollectionOrderEffectiveDate());
        assertEquals(now, defendantAccount.getFurtherStepsNoticeDate());
        assertEquals(now, defendantAccount.getConfiscationOrderDate());
        assertEquals(now, defendantAccount.getFineRegistrationDate());
        assertEquals("type", defendantAccount.getConsolidatedAccountType());
        assertTrue(defendantAccount.isPaymentCardRequested());
        assertEquals(now, defendantAccount.getPaymentCardRequestedDate());
        assertEquals("requested", defendantAccount.getPaymentCardRequestedBy());
        assertEquals("reference", defendantAccount.getProsecutorCaseReference());
        assertEquals("status", defendantAccount.getEnforcementCaseStatus());
        assertEquals(DefendantAccountType.FINES, defendantAccount.getAccountType());

    }

    @Test
    public void testLombokMethods() {
        DefendantAccount.Lite defendantAccount1 = new DefendantAccount.Lite();
        defendantAccount1.setDefendantAccountId(1L);

        DefendantAccount.Lite defendantAccount2 = new DefendantAccount.Lite();
        defendantAccount2.setDefendantAccountId(1L);

        DefendantAccount.Lite differentDefendantAccount = new DefendantAccount.Lite();
        differentDefendantAccount.setDefendantAccountId(2L);

        // Test equals method
        assertEquals(defendantAccount1, defendantAccount2);
        assertNotEquals(defendantAccount1, differentDefendantAccount);

        // Test hashCode method
        assertEquals(defendantAccount1.hashCode(), defendantAccount2.hashCode());
        assertNotEquals(defendantAccount1.hashCode(), differentDefendantAccount.hashCode());

        // Test toString method
        assertNotNull(defendantAccount1.toString());
    }
}
