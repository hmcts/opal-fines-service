package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.generated.model.CollectionOrderCommon;
import uk.gov.hmcts.opal.generated.model.CommentsAndNotesCommon;
import uk.gov.hmcts.opal.generated.model.UpdateDefendantAccountRequestPayload;

class DefendantAccountControlValidatorTest {

    private final DefendantAccountControlValidator validator = new DefendantAccountControlValidator();

    @ParameterizedTest
    @EnumSource(value = DefendantAccountStatus.class, names = {
        "ACCOUNT_CONSOLIDATED",
        "ACCOUNT_WRITTEN_OFF",
        "TRANSFER_OUT_PENDING",
        "TRANSFER_OUT_ACKNOWLEDGED",
        "TRANSFER_OUT_TO_NI_SCOTLAND"
    })
    void validateCanMutateParty_blocksConfiguredAccountStatuses(DefendantAccountStatus status) {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .accountStatus(status)
            .build();

        UnprocessableException exception = assertThrows(
            UnprocessableException.class,
            () -> validator.validateCanMutateParty(account)
        );

        assertEquals("Defendant account update blocked: Account Status Check failed because account_status is "
                         + status.getCode() + ".",
                     exception.getDetailedReason());
    }

    @Test
    void validateCanAddPaymentTerms_collectsAllFailures() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .accountStatus(DefendantAccountStatus.TRANSFER_OUT_ACKNOWLEDGED)
            .lastEnforcement("EO")
            .accountBalance(BigDecimal.ZERO.setScale(2))
            .build();

        UnprocessableException exception = assertThrows(
            UnprocessableException.class,
            () -> validator.validateCanAddPaymentTerms(account)
        );

        assertEquals("Defendant account update blocked: Account Status Check failed because account_status is TA; "
                         + "Payment terms last enforcement check failed because last_enforcement is EO; "
                         + "Zero balance check failed because account_balance is 0.00.",
                     exception.getDetailedReason());
    }

    @Test
    void validateCanAddPaymentCardRequest_usesPaymentCardLastEnforcementList() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .accountStatus(DefendantAccountStatus.LIVE)
            .lastEnforcement("AEO")
            .accountBalance(BigDecimal.TEN)
            .build();

        UnprocessableException exception = assertThrows(
            UnprocessableException.class,
            () -> validator.validateCanAddPaymentCardRequest(account)
        );

        assertEquals("Defendant account update blocked: Payment card request last enforcement check failed because "
                         + "last_enforcement is AEO.",
                     exception.getDetailedReason());
    }

    @Test
    void validateCanUpdateProtectedFields_allowsLiveAccount() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .accountStatus(DefendantAccountStatus.LIVE)
            .lastEnforcement("AEO")
            .accountBalance(BigDecimal.TEN)
            .build();

        assertDoesNotThrow(() -> validator.validateCanUpdateProtectedFields(account));
    }

    @Test
    void isProtectedUpdate_returnsTrueWhenProtectedFieldChanges() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .collectionOrder(false)
            .collectionOrderEffectiveDate(LocalDate.of(2025, 1, 1))
            .build();
        UpdateDefendantAccountRequest request = UpdateDefendantAccountRequest.builder()
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .collectionOrder(CollectionOrderCommon.builder()
                    .collectionOrderFlag(true)
                    .collectionOrderDate(LocalDate.of(2025, 1, 1))
                    .build())
                .build())
            .build();

        assertTrue(validator.isProtectedUpdate(request, account));
    }

    @Test
    void isProtectedUpdate_returnsFalseWhenProtectedFieldIsUnchanged() {
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .enforcingCourt(CourtEntity.builder().courtId(100L).build())
            .collectionOrder(true)
            .collectionOrderEffectiveDate(LocalDate.of(2025, 1, 1))
            .build();
        UpdateDefendantAccountRequest request = UpdateDefendantAccountRequest.builder()
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .collectionOrder(CollectionOrderCommon.builder()
                    .collectionOrderFlag(true)
                    .collectionOrderDate(LocalDate.of(2025, 1, 1))
                    .build())
                .build())
            .build();

        assertFalse(validator.isProtectedUpdate(request, account));
    }

    @Test
    void isProtectedUpdate_returnsFalseWhenOnlyCommentsChange() {
        DefendantAccountEntity account = DefendantAccountEntity.builder().build();
        UpdateDefendantAccountRequest request = UpdateDefendantAccountRequest.builder()
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .commentAndNotes(CommentsAndNotesCommon.builder()
                    .accountComment("comment only")
                    .build())
                .build())
            .build();

        assertFalse(validator.isProtectedUpdate(request, account));
    }
}
