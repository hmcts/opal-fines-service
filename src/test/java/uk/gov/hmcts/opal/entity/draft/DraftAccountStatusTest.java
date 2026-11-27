package uk.gov.hmcts.opal.entity.draft;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.opal.entity.draft.DraftAccountStatus.PUBLISHING_FAILED;
import static uk.gov.hmcts.opal.entity.draft.DraftAccountStatus.PUBLISHING_FAILURE_RESOLVED;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class DraftAccountStatusTest {

    @Test
    void shouldAllowUpdatingToPublishingFailureResolved() {
        assertThat(PUBLISHING_FAILED.canUpdateTo(PUBLISHING_FAILURE_RESOLVED)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = DraftAccountStatus.class, mode = Mode.EXCLUDE, names = "PUBLISHING_FAILED")
    void shouldNotAllowUpdatingToPublishingFailureResolved(DraftAccountStatus value) {
        assertThat(value.canUpdateTo(PUBLISHING_FAILURE_RESOLVED)).isFalse();
    }
}
