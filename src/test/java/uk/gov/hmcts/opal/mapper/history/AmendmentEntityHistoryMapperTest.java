package uk.gov.hmcts.opal.mapper.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;

class AmendmentEntityHistoryMapperTest {

    private final AmendmentEntityHistoryMapper mapper = Mappers.getMapper(AmendmentEntityHistoryMapper.class);

    @Test
    void toHistoryItem_mapsPostedByNameFromAmendmentEntity() {
        AmendmentEntity entity = AmendmentEntity.builder()
            .amendmentId(123L)
            .businessUnitId((short) 78)
            .associatedRecordType("DEFENDANT_ACCOUNTS")
            .associatedRecordId("262200")
            .amendedDate(LocalDateTime.of(2026, 1, 1, 10, 15))
            .amendedBy("opal-user")
            .amendedByName("Opal User")
            .fieldCode((short) 1)
            .oldValue("old")
            .newValue("new")
            .build();

        var historyItem = mapper.toHistoryItem(entity);

        assertThat(historyItem.getType()).isEqualTo(HistoryItemType.AMENDMENT);
        assertThat(historyItem.getPostedDetails()).isNotNull();
        assertThat(historyItem.getPostedDetails().getPostedBy()).isEqualTo("opal-user");
        assertThat(historyItem.getPostedDetails().getPostedByName()).isEqualTo("Opal User");
    }
}
