package uk.gov.hmcts.opal.mapper.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionPaymentMethod;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionStatus;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionType;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionWriteOffCode;

class DefendantTransactionEntityHistoryMapperTest {

    private final DefendantTransactionEntityHistoryMapper mapper =
        Mappers.getMapper(DefendantTransactionEntityHistoryMapper.class);

    @Test
    void toHistoryItem_mapsTransactionTypePaymentMethodAndStatusCodeAndDisplayName() {
        DefendantTransactionEntity entity = DefendantTransactionEntity.builder()
            .defendantTransactionId(123L)
            .defendantAccountId(262200L)
            .postedDate(LocalDate.of(2026, 1, 6))
            .postedBy("opal-user")
            .postedByUsername("Opal User")
            .transactionType(DefendantTransactionType.PAYMNT)
            .transactionAmount(BigDecimal.TEN)
            .paymentMethod(DefendantTransactionPaymentMethod.NC)
            .status(DefendantTransactionStatus.P)
            .statusDate(LocalDateTime.of(2026, 1, 7, 11, 0))
            .writeOffCode(DefendantTransactionWriteOffCode.TRNOUT)
            .text("detail")
            .build();

        var historyItem = mapper.toHistoryItem(entity);
        var details = (uk.gov.hmcts.opal.dto.history.DefendantTransactionDetails) historyItem.getDetails();

        assertThat(details.getTransactionType().getTransactionType()).isEqualTo("PAYMNT");
        assertThat(details.getTransactionType().getTransactionTypeDisplayName()).isEqualTo("Payments");
        assertThat(details.getPaymentMethod().getPaymentMethod()).isEqualTo("NC");
        assertThat(details.getPaymentMethod().getPaymentMethodDisplayName()).isEqualTo("Notes & Coins");
        assertThat(details.getWriteOff().getWriteOffType()).isEqualTo("TRNOUT");
        assertThat(details.getWriteOff().getWriteOffTypeDisplayName()).isEqualTo("Transferred out");
        assertThat(details.getStatus().getDefendantTransactionStatus()).isEqualTo("P");
        assertThat(details.getStatus().getDefendantTransactionStatusDisplayName()).isEqualTo("Partially-reversed");
    }

    @Test
    void toHistoryItem_mapsTfoInToExpectedApiCodeAndDisplayName() {
        DefendantTransactionEntity entity = DefendantTransactionEntity.builder()
            .defendantTransactionId(124L)
            .defendantAccountId(262200L)
            .postedDate(LocalDate.of(2026, 1, 6))
            .transactionType(DefendantTransactionType.TFO_IN)
            .status(DefendantTransactionStatus.C)
            .statusDate(LocalDateTime.of(2026, 1, 7, 11, 0))
            .build();

        var historyItem = mapper.toHistoryItem(entity);
        var details = (uk.gov.hmcts.opal.dto.history.DefendantTransactionDetails) historyItem.getDetails();

        assertThat(details.getTransactionType().getTransactionType()).isEqualTo("TFOIN");
        assertThat(details.getTransactionType().getTransactionTypeDisplayName()).isEqualTo("TFO In");
    }
}
