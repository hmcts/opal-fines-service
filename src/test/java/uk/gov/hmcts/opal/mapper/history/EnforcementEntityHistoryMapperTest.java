package uk.gov.hmcts.opal.mapper.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.history.EnforcementDetails;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;

class EnforcementEntityHistoryMapperTest {

    private final EnforcementEntityHistoryMapper mapper = Mappers.getMapper(EnforcementEntityHistoryMapper.class);

    @Test
    void toHistoryItem_mapsHearingCourtName() {
        CourtEntity hearingCourt = CourtEntity.builder()
            .courtId(321L)
            .name("Brent magistrates court")
            .build();
        EnforcementEntity entity = EnforcementEntity.builder()
            .enforcementId(123L)
            .postedDate(LocalDateTime.of(2026, 1, 1, 10, 15))
            .postedBy("opal-user")
            .postedByUsername("Opal User")
            .resultId("BWTD")
            .hearingCourt(hearingCourt)
            .hearingDate(LocalDateTime.of(2025, 10, 23, 9, 30))
            .caseReference("2500000198")
            .build();

        var historyItem = mapper.toHistoryItem(entity);

        assertThat(historyItem.getType()).isEqualTo(HistoryItemType.ENFORCEMENT);
        assertThat(historyItem.getDetails()).isInstanceOf(EnforcementDetails.class);
        EnforcementDetails details = (EnforcementDetails) historyItem.getDetails();
        assertThat(details.getHearingCourt()).isNotNull();
        assertThat(details.getHearingCourt().getCourtId()).isEqualTo(321);
        assertThat(details.getHearingCourt().getCourtName()).isEqualTo("Brent magistrates court");
    }
}
