package uk.gov.hmcts.opal.entity.draft;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.opal.entity.draft.DraftAccountStatus.APPROVED;
import static uk.gov.hmcts.opal.entity.draft.DraftAccountStatus.SUBMITTED;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class DraftAccountEntityTest {
    @Test
    void changeStatus() {
        // Arrange
        Clock clock = Clock.fixed(Instant.parse("2026-07-20T00:00:00Z"), ZoneOffset.UTC);
        DraftAccountEntity draftAccount = DraftAccountEntity.builder()
            .accountStatus(SUBMITTED)
            .accountStatusDate(LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0))
            .build();

        // Act
        draftAccount.changeStatus(APPROVED, clock);

        // Assert
        assertThat(draftAccount.getAccountStatus()).isEqualTo(APPROVED);
        assertThat(draftAccount.getAccountStatusDate()).isEqualTo(LocalDateTime.of(2026, Month.JULY, 20, 0, 0 ));
    }
}
